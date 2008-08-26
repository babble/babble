# Print usage info if necessary.
if [ -z "$1" ]
    then
        echo "Usage: `basename $0` path-to-site"
        exit 1
fi

# Check to see if given site exists
if [ -d "$1" ]
    then
        echo "Testing '$1'."
    else
        echo "Site '$1' does not exist. Be sure to use the full path to the site."
        exit 1
fi

# SITE is just the name of the site, like 'alleyinsider'. FULLSITE is the full
# path to the site's directory. TESTDIR is the webtest directory.
SITE=`basename $1`
pushd $1
FULLSITE=`pwd`
popd
cd `dirname $0`
TESTDIR=`pwd`

# Do configuration steps.
source $TESTDIR/webtest-config.bash

# Do local configuration if we have a webtest-local.bash.
if [ -f $TESTDIR/webtest-local.bash ]
    then
        echo "Using webtest-local: $TESTDIR/webtest-local.bash"
        source $TESTDIR/webtest-local.bash
fi

# Bring up the test database.
mkdir -p /tmp/$SITE/logs /tmp/$SITE/db
rm /tmp/$SITE/db/*
cd $GITROOT/p/db
nohup ./db --port $db_port --dbpath /tmp/alleyinsider/db/ run > /tmp/alleyinsider/logs/db&
db_pid=$!

# Use the _config.js in the test directory if there is one.
cp $FULLSITE/_config.js $FULLSITE/test/_config.js.backup
if [ -f $FULLSITE/test/_config.js ]
    then
        echo "Using test version of _config.js: $FULLSITE/test/_config.js"
        cp $FULLSITE/test/_config.js $FULLSITE/_config.js
fi

# Bring up the app server.
cd $GITROOT/ed
./runAnt.bash ed.appserver.AppServer --port $http_port $FULLSITE&
http_pid=$!

# Populate the db with setup data.
if [ -f $FULLSITE/test/setup.js ]
    then
        ./runLight.bash ed.js.Shell -exit $FULLSITE/test/setup.js
fi

# Copy test resources into test directory.
cp $TESTDIR/resources/build.xml $FULLSITE/test/build.xml
cp $TESTDIR/resources/buildReal.xml $FULLSITE/test/buildReal.xml
if [ ! -d $FULLSITE/test/definitions ]
    then
        mkdir $FULLSITE/test/definitions
fi
ln -s $TESTDIR/resources/definitions $FULLSITE/test/definitions/_10gen_default_defs

# Run webtest.
cd $FULLSITE/test
export WTPATH=$GITROOT/ed/include/webtest
$WTPATH/bin/webtest.sh

# Removed copied resources and auto-generated cruft.
# This step probably isn't necessary but will hopefully prevent people from
# making changes to files that get copied over automatically.
rm $FULLSITE/test/build.xml
rm $FULLSITE/test/buildReal.xml
rm $FULLSITE/test/definitions/_10gen_default_defs
rmdir $FULLSITE/test/definitions
rm $FULLSITE/test/definitions.xml
rm -r $FULLSITE/test/dtd


# Clean up from the _config.js shuffling.
cp $FULLSITE/test/_config.js.backup $FULLSITE/_config.js
rm $FULLSITE/test/_config.js.backup

# Bring down the db and appserver.
kill -9 $db_pid
kill -9 $http_pid
