# You can (and probably should) override these values in webtest-local.bash
export WTSITE=http://localhost:8080
# Uncomment to run headless
# export WTPARAMS="-Dwt.headless=true"

# You can set GITROOT as the parent dir to ed/ and p/, 
# alternatively set EDROOT and PROOT individually
GITROOT=~/10gen
#PROOT=$GITROOT/p
#EDROOT=$GITROOT/ed

export db_ip=127.0.0.1                # fetch from a site
export db_port=27017
export http_port=8080