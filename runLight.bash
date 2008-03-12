#!/bin/bash

export CLASSPATH=.:build:conf:/opt/java/lib/tools.jar:$CLASSPATH

export TZ=America/New_York

for j in `ls include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export headless="-Djava.awt.headless=true"

java -ea -Djava.library.path=include $headless -ea -Xmx1000m "$@"

