#!/bin/bash

export CLASSPATH=.:build:conf:/opt/java/lib/tools.jar

export TZ=America/New_York

for j in `ls include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export headless="-Djava.awt.headless=true"
if [ $?gui ]; then
    export headless=""
fi

ant && java -Xprof -ea $headless -ea -Xmx2000m "$@"

