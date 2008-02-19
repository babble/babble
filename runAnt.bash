#!/bin/bash

export CLASSPATH=.:build:conf:/opt/java/lib/tools.jar:$CLASSPATH

export TZ=America/New_York

for j in `ls include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export headless="-Djava.awt.headless=true"
#if [ $?gui ]; then
    #export headless="" #was having issue so commented this out
#fi

ant && java -ea $headless -Xmx2000m "$@"

