#! /bin/bash

# Print usage info if necessary.
if [ -z "$1" ]
then
    echo "Usage: `basename $0` path-to-site [path-to-site [path-to-site [...]]]"
    exit 1
fi

EXIT_STATUS=0
export WTPARAMS="-Dwt.headless=true"
TESTED_ONE=""

for ARG in $@
do
    # Check to see if given site exists
    if [ -d "$ARG/test" ]
    then
        pushd $ARG > /dev/null
        SITE=`pwd`
        popd > /dev/null
        echo "Testing '$SITE'."
        pushd `dirname "$0"` > /dev/null
        ./runSiteTests.bash $SITE &> /dev/null
        if [ $? != "0" ]
        then
            echo "FAILED"
            EXIT_STATUS=1
        fi
        TESTED_ONE="true"
        popd > /dev/null
    fi
done

if [ -z $TESTED_ONE ]
then
    echo "No testable sites found."
fi

exit $EXIT_STATUS
