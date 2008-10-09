
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
    
    if [ ! -d $PROOT ]
        then
            echo "Environment variable PROOT is not set correctly"
            return 0
    fi
    
    if [ -z $db_port ]
        then
            echo "Environment variable db_port is not set correctly"
            return 0
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
    local SITE=`basename $FULLSITE`
    
    if [ ! -d $EDROOT ]
        then
            echo "The environment variable EDROOT is not set correctly"
            return 0
    fi
    
    if [ -z $http_port ]
        then
            echo "Environment variable http_port is not set correctly"
            return 0
    fi
    
    mkdir -p /tmp/$SITE/logs
    
    # Bring up the app server.
    pushd "$EDROOT" >> /dev/null
    ant compile
    ./runAnt.bash ed.appserver.AppServer --port $http_port $FULLSITE | tee /tmp/$SITE/logs/ed &
    local script_pid=$!
    popd >> /dev/null

    for ((i=0;i<60;i+=1)); do
        #started successfully?
        grep -q "^Listening on port" /tmp/$SITE/logs/ed
        if [ $? -eq 0 ]
        then
            ed_listening=1
            break
        fi
        
        #died a painful death?
        ps -x -o pid | grep -q "$script_pid"
        if [ $? -ne 0 ]
        then
            break
        fi 
        
        sleep 5
    done
    
    if [ $ed_listening ]
        then
            echo "Failed to start ed"
            return
    fi
    
    unset PID
    for ((i=0;i<10;i+=1)); do
        PID=`ps -a -x -e -o pid,command | grep java | grep -v "runAnt" | grep "ed.appserver.AppServer" |  awk '{ print $1 }'`
        
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

