#!/bin/bash

export CLASSPATH=.:build:conf:$CLASSPATH

export TZ=America/New_York

for j in `ls include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export headless="-Djava.awt.headless=true"
#if [ $?gui ]; then
    #export headless="" #was having issue so commented this out
#fi

ant && java -Djava.library.path=include -ea $headless -Xmx1000m  -XX:MaxDirectMemorySize=1024M "$@"

