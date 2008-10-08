
#load config
pushd `dirname $0` > /dev/null
TESTDIR=`pwd`
popd > /dev/null

source $TESTDIR/webtest-config.bash

# Do local configuration if we have a webtest-local.bash.
if [ -f $TESTDIR/webtest-local.bash ]
    then
        echo "Using webtest-local: $TESTDIR/webtest-local.bash"
        source $TESTDIR/webtest-local.bash
fi

# Set EDROOT and PROOT if they aren't already set
if [ -z $EDROOT ]
    then
        EDROOT=$GITROOT/ed
fi
if [ -z $PROOT ]
    then
        PROOT=$GITROOT/p
fi


function run_db {
    local DBNAME=$1
    
    if [ ! -d $EDROOT ]
        then
            echo "Environment variable EDROOT is not set correctly"
            return 0
    fi
    
    if [ -z $db_port ]
        then
            echo "Environment variable db_port is not set correctly"
            return 0
    fi
    
    if [ -z $EDROOT ]
        then
            EDROOT=$GITROOT/ed
    fi
       
    
    
    mkdir -p /tmp/$DBNAME/logs /tmp/$DBNAME/db
    rm /tmp/$DBNAME/db/*
    
    
    pushd $PROOT/db > /dev/null
    nohup ./db --port $db_port --dbpath /tmp/$DBNAME/db/ run > /tmp/$DBNAME/logs/db&
    PID=$!
    popd > /dev/null
}

#returns pid in a variable PID
function run_ed {
    local FULLSITE=$1
    
    if [ ! -d $PROOT -a ! -d $GITROOT ]
        then
            echo "The environment variable PROOT is not set correctly"
            return 0
    fi
    
    if [ -z $http_port ]
        then
            echo "Environment variable http_port is not set correctly"
            return 0
    fi
    
    if [ -z $PROOT ]
        then
            PROOT=$GITROOT/p
    fi
    
    # Bring up the app server.
    pushd "$EDROOT" >> /dev/null
    ./runAnt.bash ed.appserver.AppServer --port $http_port $FULLSITE&
    popd >> /dev/null
    
    sleep 5
    
    unset PID
    for ((i=0;i<10;i+=1)); do
        PID=`ps -a -x -e -o pid,command | grep java | grep "ed.appserver.AppServer" |  awk '{ print $1 }'`
        
        if [ -n "$PID" ]
            then
                break
        fi
        sleep 1
    done
}

function kill_db {
    local db_pid=$1
    kill -9 $db_pid || echo "failed to kill database"
}
function kill_ed {
    local http_pid=$1
    kill -9 $http_pid || echo "failed to kill appserver"
}

