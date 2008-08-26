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
export WTSITE=http://localhost:8080
export WTPATH=$SDKDIR/appserver/include/webtest

# Bring up the test database.
./dbctrl.sh start&

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

    # This is a bit magical. We need to pass the PID from runserver.sh. We
    # do it through this tmp file.
    http_pid=`cat /tmp/10genAppServerPID`
    # Bring down the appserver
    kill $http_pid

    # If the webtests failed then bring down the db and exit w/ an error
    if [ $STATUS != "0" ]
        then
            # Bring down the db
            ./dbctrl.sh stop
            exit $STATUS
    fi
done

# Bring down the db
./dbctrl.sh stop
