#!/bin/bash



export TZ=America/New_York

export ED_HOME=`echo $0 | sed -e 's/runLight.bash//p' | head -n 1`

export CLASSPATH=.:${ED_HOME}/build:${ED_HOME}/conf:$CLASSPATH
for j in `ls ${ED_HOME}include/*.jar`; do
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
export jruby_home="-Djruby.home=`dirname $0`/build/ruby"

java -ea -Djava.library.path=include $headless $jruby_home -ea -Xmx1000m -XX:MaxDirectMemorySize=600M "$@"

