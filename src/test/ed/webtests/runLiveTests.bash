# Print usage info if necessary.
if [ -z "$1" || -z "$2"]
    then
        echo "Usage: `basename $0` path-to-site URL-to-test"
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

# FULLSITE is the full path to the site's directory. TESTDIR is the webtest directory.
pushd $1
FULLSITE=`pwd`
popd
cd `dirname $0`
TESTDIR=`pwd`

if [ ! -d "$FULLSITE/test" ]
    then
        echo "Site '$1' does not contain a test directory, aborting."
        exit 1
fi

# Do configuration steps.
source $TESTDIR/webtest-config.bash

# Do local configuration if we have a webtest-local.bash.
if [ -f $TESTDIR/webtest-local.bash ]
    then
        echo "Using webtest-local: $TESTDIR/webtest-local.bash"
        source $TESTDIR/webtest-local.bash
fi

# Set url to run against
export WTSITE=$2

# Set EDROOT an if it isn't already set
if [ -z $EDROOT ]
    then
        EDROOT=$GITROOT/ed
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
export WTPATH=$EDROOT/include/webtest
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

exit $STATUS
