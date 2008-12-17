
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
if [ -z $COREJSROOT ]
    then
        COREJSROOT=$GITROOT/corejs
fi

#make paths absolute
function make_dir_path_abs {
    if [ ! -d "$1" ]
        then
            echo "The directory $1, doesn't exist"
            return 1
    fi
    
    if ! pushd "$1" > /dev/null
        then
            echo "Failed to cd to $1"
            return 1
    fi
    
    pwd
    popd > /dev/null
}

GITROOT=`make_dir_path_abs "$GITROOT"`
EDROOT=`make_dir_path_abs "$EDROOT"`
PROOT=`make_dir_path_abs "$PROOT"`
COREJSROOT=`make_dir_path_abs "$COREJSROOT"`


function run_db {
    local DBNAME=$1
    
    if [ ! -d $PROOT ]
        then
            echo "Environment variable PROOT is not set correctly"
            return 1
    fi
    
    if [ -z $db_port ]
        then
            echo "Environment variable db_port is not set correctly"
            return 1
    fi
       
    
    
    mkdir -p /tmp/$DBNAME/logs /tmp/$DBNAME/db
    rm /tmp/$DBNAME/db/*
    
    
    pushd $PROOT/db > /dev/null

    if ! nohup ./db --port $db_port --appsrvpath $EDROOT --dbpath /tmp/$DBNAME/db/ run > /tmp/$DBNAME/logs/db&
        then
            echo "db failed to start"
            return 1
    fi
    
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
            return 1
    fi
    
    if [ -z $http_port ]
        then
            echo "Environment variable http_port is not set correctly"
            return 1
    fi
    
    mkdir -p /tmp/$SITE/logs
    rm /tmp/$SITE/logs/ed
    
    # Bring up the app server.
    pushd "$EDROOT" >> /dev/null
    
    if ! ant compile
        then
            echo "failed to compile"
            return 1
    fi

    ./runAnt.bash ed.appserver.AppServer --port $http_port $FULLSITE | tee /tmp/$SITE/logs/ed &
    local script_pid=$!
    popd >> /dev/null

    local ed_listening=0

    for ((i=0;i<60;i+=1)); do
        #started successfully?
        
        if grep -q "^Listening on port" /tmp/$SITE/logs/ed
        then
            ed_listening=1
            break
        fi
        
        #died a painful death?
        if ! ps -x -o pid | grep -q "$script_pid"
        then
            echo "ed unexpectedly died"
            return 1
        fi 
        
        sleep 5
    done
    
    if [ $ed_listening -eq 0 ]
        then
            echo "Timed out waiting for ed to start"
            return 1
    fi
    
    PID=`ps -a -x -e -o pid,command | grep java | grep -v "runAnt" | grep "ed.appserver.AppServer" |  awk '{ print $1 }'`
}


function kill_db {
    local db_pid=$1
    kill -9 $db_pid || echo "failed to kill database"
}
function kill_ed {
    local http_pid=$1
    kill -9 $http_pid || echo "failed to kill appserver"
}

