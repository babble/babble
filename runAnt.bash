#!/bin/bash

export CLASSPATH=.:build:conf:$CLASSPATH

export TZ=America/New_York

for j in `ls include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

for j in `ls include/jython/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

for j in `ls include/jython/javalib/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export CLASSPATH=$CLASSPATH:/opt/java/lib/tools.jar

export headless="-Djava.awt.headless=true"
#if [ $?gui ]; then
    #export headless="" #was having issue so commented this out
#fi

ant && java -Djava.library.path=include -ea $headless -Xmx1000m  -XX:MaxDirectMemorySize=600M "$@"

