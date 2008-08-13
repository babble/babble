SITE=$1
GITROOT=~/
SITESROOT=~/h

source webtest-config.bash

export db_ip=127.0.0.1                # fetch from a site
export db_port=1337
export http_port=1338

# bring up the test database
mkdir -p /tmp/$SITE/logs /tmp/$SITE/db
rm /tmp/$SITE/db/*

cd $GITROOT/p/db
nohup ./db --port $db_port --dbpath /tmp/alleyinsider/db/ run > /tmp/alleyinsider/logs/db&
db_pid=$?

sleep 6

# bring up the app server
cd $GITROOT/ed
./runAnt.bash ed.appserver.AppServer --port $http_port $SITESROOT/$SITE&
http_pid=$?

# populate it with setup data
./runLight.bash ed.js.Shell -exit $SITESROOT/$SITE/test/setup.js

cd $SITESROOT/$SITE/test
ant

kill -9 $db_pid
kill -9 $http_pid
