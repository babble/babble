#! /bin/bash
source `dirname "$0"`/common.bash

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
pushd "$1" > /dev/null
FULLSITE=`pwd`
popd > /dev/null
SITE=`basename $FULLSITE`


if [ ! -d "$FULLSITE/test" ]
    then
        echo "Site '$1' does not contain a test directory, aborting."
        exit 1
fi

# Start the servers
if ! run_db $SITE
    then      
        exit 1
fi
db_pid=$PID

if ! run_ed $FULLSITE
    then
        kill_db $db_pid
        exit 1
fi
http_pid=$PID

# Use the _config.js in the test directory if there is one.
if [ -z $NO_TEST_CONFIG ]
    then
        cp $FULLSITE/_config.js $FULLSITE/test/_config.js.backup
        if [ -f $FULLSITE/test/_config.js ]
            then
                echo "Using test version of _config.js: $FULLSITE/test/_config.js"
                cp $FULLSITE/test/_config.js $FULLSITE/_config.js
        fi
fi

# Populate the db with setup data.
if [ -f $FULLSITE/test/setup.js ]
    then
        pushd $EDROOT >> /dev/null

        if [ -x $FULLSITE/test/setup.js ]
            then
                # if the setup script needs $GITROOT, make it an executable script
                $FULLSITE/test/setup.js $GITROOT
            else
                # otherwise it'll just be run in the shell
                ./runLight.bash ed.js.Shell --exit $FULLSITE/test/setup.js
        fi

        popd >> /dev/null
fi

# Copy test resources into test directory.
cp "$TESTDIR/resources/build.xml" "$FULLSITE/test/build.xml"
echo "done"

cp "$TESTDIR/resources/buildReal.xml" "$FULLSITE/test/buildReal.xml"
if [ ! -d $FULLSITE/test/definitions ]
    then
        mkdir $FULLSITE/test/definitions
fi
ln -s $TESTDIR/resources/definitions $FULLSITE/test/definitions/_10gen_default_defs


# Run webtest.
cd $FULLSITE/test
export WTPATH=$EDROOT/include/webtest
export LOCAL_TEST_10GEN="true"
$WTPATH/bin/webtest.sh $WTPARAMS
STATUS=$?

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
if [ -z $NO_TEST_CONFIG ]
    then
        if [ -f $FULLSITE/test/_config.js.backup ]
            then
                cp $FULLSITE/test/_config.js.backup $FULLSITE/_config.js
                rm $FULLSITE/test/_config.js.backup
            else
                rm $FULLSITE/_config.js
        fi
fi

kill_ed $http_pid
kill_db $db_pid

exit $STATUS
