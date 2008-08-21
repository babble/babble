#!/bin/bash



export TZ=America/New_York

export CLASSPATH=".;build;conf"
for j in `ls include/*.jar`; do
    export CLASSPATH="$CLASSPATH;$j"
done

for j in `ls include/jython/current/*.jar`; do
    export CLASSPATH="$CLASSPATH;$j"
done

for j in `ls include/jython/current/javalib/*.jar`; do
    export CLASSPATH="$CLASSPATH;$j"
done


export CLASSPATH=$CLASSPATH:/opt/java/lib/tools.jar

export headless="-Djava.awt.headless=true"
java -ea -Djava.library.path=include $headless -ea -Xmx1000m -XX:MaxDirectMemorySize=600M "$@"

