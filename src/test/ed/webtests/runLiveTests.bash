# Print usage info if necessary.
if [ -z "$1" ]
    then
        echo "Usage: `basename $0` URL-to-site-git-repository URL-to-site"
        exit 1
fi
if [ -z "$2" ]
    then
        echo "Usage: `basename $0` URL-to-site-git-repository URL-to-test"
        exit 1
fi

# TESTDIR is the webtest directory.
cd `dirname $0`
TESTDIR=`pwd`

# Clone git repo to a temporary directory.
rm -rf /tmp/10gen-test-src
git clone $1 /tmp/10gen-test-src

# Get the tag running in production
GIT_TAG=`curl -I http://www.10gen.com 2>/dev/null | grep X-git | awk '{sub(/\r$/,"");print $2}'`
if [ -z $GIT_TAG ]
    then
        echo "Couldn't find git tag header for $2"
        exit 1
fi

# Get the right version of the tests
cd /tmp/10gen-test-src/
git checkout $GIT_TAG
if [ ! -d /tmp/10gen-test-src/test ]
    then
        echo "No test directory found on this tag, aborting"
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
cp $TESTDIR/resources/build.xml /tmp/10gen-test-src/test/build.xml
cp $TESTDIR/resources/buildReal.xml /tmp/10gen-test-src/test/buildReal.xml
if [ ! -d /tmp/10gen-test-src/test/definitions ]
    then
        mkdir /tmp/10gen-test-src/test/definitions
fi
ln -s $TESTDIR/resources/definitions /tmp/10gen-test-src/test/definitions/_10gen_default_defs

# Run webtest.
cd /tmp/10gen-test-src/test
export WTPATH=$EDROOT/include/webtest
$WTPATH/bin/webtest.sh $WTPARAMS
STATUS=$?

# Removed copied resources and auto-generated cruft.
# This step probably isn't necessary but will hopefully prevent people from
# making changes to files that get copied over automatically.
rm /tmp/10gen-test-src/test/build.xml
rm /tmp/10gen-test-src/test/buildReal.xml
rm /tmp/10gen-test-src/test/definitions/_10gen_default_defs
rmdir /tmp/10gen-test-src/test/definitions
rm /tmp/10gen-test-src/test/definitions.xml
rm -r /tmp/10gen-test-src/test/dtd

exit $STATUS
