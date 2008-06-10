#!/bin/bash



export TZ=America/New_York

export ED_HOME=`echo $0 | sed -e 's/runShark.bash//p' | head -n 1`

export CLASSPATH=.:${ED_HOME}/build:${ED_HOME}/conf:$CLASSPATH
for j in `ls ${ED_HOME}include/*.jar`; do
    export CLASSPATH=$CLASSPATH:$j
done

export headless="-Djava.awt.headless=true"

ant && java -ea -XrunShark -Djava.library.path=include $headless -ea -Xmx200m -XX:MaxDirectMemorySize=200M "$@"

