# You can (and probably should) override these values in webtest-local.bash

export WTSITE=http://localhost:8080
export db_ip=127.0.0.1                # fetch from a site
export db_port=27017
export http_port=8080

## Uncomment to run headless
#export WTPARAMS="-Dwt.headless=true"

# Set GITROOT as the parent dir to ed/ and p/
# Alternatively, set EDROOT and PROOT individually
GITROOT=~/10gen
#PROOT=$GITROOT/p
#EDROOT=$GITROOT/ed

##Uncomment to use the regular _config.js instead of the test version:
##e.g. uncomment to use site-name/_config.js instead of site-name/test/_config.js
#NO_TEST_CONFIG="true"
