# Print usage info if necessary.
if [ -z "$1" ]
    then
        echo "Usage: `basename $0` path-to-sdk-directory"
        exit 1
fi

# Check to see if given sdk directory exists
if [ -d "$1" ]
    then
        echo "Testing SDK directory: '$1'"
    else
        echo "SDK directory '$1' does not exist. Be sure to use the full path to the SDK."
        exit 1
fi

# TESTDIR is the webtest directory. SDKDIR is the root directory of the SDK
pushd `dirname $0` >/dev/null
TESTDIR=`pwd`
popd >/dev/null
cd $1
SDKDIR=`pwd`

# Configure webtest
export db_port="5586"
export http_port="2008"
export WTSITE=http://localhost:$http_port
export WTPATH=$SDKDIR/appserver/include/webtest

# Bring up the test database.
mkdir -p /tmp/10genSDKtests/db
rm /tmp/10genSDKtests/db/*
./dbctrl.sh start /tmp/10genSDKtests/db&

sleep 5

# Test each site in the sites directory
cd ./sites
SITES_LIST=`ls -d1 */`
cd ..
for site in $SITES_LIST; do
    # See if the site has tests
    if [ -d "./sites/${site}test/" ]
        then
            echo "testing: $site"
        else
            echo "skipping: $site, no test directory"
            continue
    fi

    # Bring up the app server.
    ./runserver.sh ./sites/$site&

	sleep 5

    # Populate the db with setup data.
    if [ -f ./sites/${site}test/setup.js ]
        then
            echo "populating the database using setup.js"
            ./appserver/runLight.bash ed.js.Shell -exit ./sites/${site}test/setup.js
    fi

    # Copy test resources into test directory.
    cp $TESTDIR/resources/build.xml ./sites/${site}test/build.xml
    cp $TESTDIR/resources/buildReal.xml ./sites/${site}test/buildReal.xml
    if [ ! -d ./sites/${site}test/definitions ]
        then
            mkdir ./sites/${site}test/definitions
    fi
    ln -s $TESTDIR/resources/definitions ./sites/${site}test/definitions/_10gen_default_defs

    # Use the _config.js in the test directory if there is one.
    cp ./sites/${site}_config.js ./sites/${site}test/_config.js.backup
    if [ -f ./sites/${site}test/_config.js ]
        then
            echo "Using test version of _config.js: ./sites/${site}test/_config.js"
            cp ./sites/${site}test/_config.js ./sites/${site}_config.js
    fi

    # Run webtest. Exit on a test failure.
    pushd ./sites/${site}test
    $WTPATH/bin/webtest.sh -Dwt.headless=true
    STATUS=$?
    popd

    # Removed copied resources and auto-generated cruft.
    # This step probably isn't necessary but will hopefully prevent people from
    # making changes to files that get copied over automatically.
    rm ./sites/${site}test/build.xml
    rm ./sites/${site}test/buildReal.xml
    rm ./sites/${site}test/definitions/_10gen_default_defs
    rmdir ./sites/${site}test/definitions
    rm ./sites/${site}test/definitions.xml
    rm -r ./sites/${site}test/dtd

    # Figure out the pid of the appserver we just brought up.
    http_pid=`ps -e -o pid,command | grep java | grep "port $http_port" | awk '{ print $1 }'`
    # Bring down the appserver
	echo "ps -e -o pid,command | grep java | grep 'port $http_port':"
	echo `ps -e -o pid,command | grep java | grep "port $http_port"`
	echo "Bringing down appserver at pid: ${http_pid}"
    kill $http_pid

    # If the webtests failed then bring down the db and exit w/ an error
    if [ $STATUS != "0" ]
        then
            # Bring down the db
            ./dbctrl.sh stop
            echo "TEST FAILURE FOR SITE: ${site}"
            exit $STATUS
    fi

	sleep 5
done

# Bring down the db
./dbctrl.sh stop
echo "ALL TESTS SUCCESSFUL"
