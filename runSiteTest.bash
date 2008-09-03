#!/bin/bash

export CLASSPATH=.:build/corejstest/:conf:$CLASSPATH

export TZ=America/New_York

for j in `ls include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export headless="-Djava.awt.headless=true"
#if [ $?gui ]; then
    #export headless="" #was having issue so commented this out
#fi
export jruby_home="-Djruby.home=`dirname $0`/build/ruby"

ant && java -Djava.library.path=include -ea $headless $jruby_home -Xmx1000m  -XX:MaxDirectMemorySize=600M "$@"

