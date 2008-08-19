if [ -z "$1" ]
    then
        echo "Usage: `basename $0` name-of-site-to-test"
        exit 1
fi
SITE=$1

# You can (and probably should) override these values in webtest-local.bash
export WTPATH=/usr/local/webtest
export WTSITE=http://local.alleyinsider.com:8080
GITROOT=~/10gen
SITESROOT=~/10gen/sites
export db_ip=127.0.0.1                # fetch from a site
export db_port=1337
export http_port=1338

if [ -f `dirname $0`/webtest-local.bash ]
    then
        echo "Using webtest-local: `dirname $0`/webtest-local.bash"
        source `dirname $0`/webtest-local.bash
fi

# bring up the test database
mkdir -p /tmp/$SITE/logs /tmp/$SITE/db
rm /tmp/$SITE/db/*

cd $GITROOT/p/db
nohup ./db --port $db_port --dbpath /tmp/alleyinsider/db/ run > /tmp/alleyinsider/logs/db&
db_pid=$?

sleep 2

# use the _config.js in the test directory if there is one.
cp $SITESROOT/$SITE/_config.js $SITESROOT/$SITE/test/_config.js.backup
if [ -f $SITESROOT/$SITE/test/_config.js ]
    then
        echo "Using test version of _config.js: $SITESROOT/$SITE/test/_config.js"
        cp $SITESROOT/$SITE/test/_config.js $SITESROOT/$SITE/_config.js
fi

# bring up the app server
cd $GITROOT/ed
./runAnt.bash ed.appserver.AppServer --port $http_port $SITESROOT/$SITE&
http_pid=$?

# populate it with setup data
./runLight.bash ed.js.Shell -exit $SITESROOT/$SITE/test/setup.js

cd $SITESROOT/$SITE/test
webtest.sh

# clean up from the _config.js shuffling
cp $SITESROOT/$SITE/test/_config.js.backup $SITESROOT/$SITE/_config.js
rm $SITESROOT/$SITE/test/_config.js.backup

kill -9 $db_pid
kill -9 $http_pid
