#!/bin/bash
# WARNING: this script is only experimental now

# IMPORTANT: any modification in this script file must be reflected in the windows version

# Simple runner script for webtest.

# don't make any assumption concerning current dir
WEBTEST_HOME=`dirname "$0"`/..

WEBTEST_RUNNER=$WEBTEST_HOME/resources/webtestsRunner.xml

EXEC="$WEBTEST_HOME/bin/webtest.sh -f $WEBTEST_RUNNER -Dwt.headless=$(wt.headless)  -Dwebtest.testdir=$(pwd) ${1:+-Dwebtest.testfile=$1}"
echo $EXEC
exec $EXEC
