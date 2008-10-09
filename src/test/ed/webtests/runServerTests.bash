source `dirname "$0"`/common.bash

FULLSITE="/tmp/serverTestsSite/serverTestsSite"
SITE=`basename $FULLSITE`

mkdir -p "$FULLSITE"
rm -r "$FULLSITE/*"

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

#create an admin user
pushd $EDROOT > /dev/null
$EDROOT/bin/scripts/adduser $SITE admin "admin@10gen.com" "password" "admin"
popd > /dev/null

tests=( "python $GITROOT/corejs/test/webdav_integration.py --host localhost --port $http_port --user admin --password password" )

#Run tests
RC=0
for (( i = 0 ; i < ${#tests[@]} ; i++ ))
do
        ${tests[$i]}
        if [ $? -ne 0 ]
            then
                echo "Test ${tests[$i]} failed"
                RC=$(( $RC + 1 ))
        fi
done


kill_ed $http_pid
kill_db $db_pid

exit $RC
