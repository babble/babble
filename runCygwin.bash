#!/bin/bash



export TZ=America/New_York

export CLASSPATH=".;build;conf"
for j in `ls include/*.jar`; do
    export CLASSPATH="$CLASSPATH;$j"
done

for j in `ls include/jython/*.jar`; do
    export CLASSPATH="$CLASSPATH;$j"
done

for j in `ls include/jython/javalib/*.jar`; do
    export CLASSPATH="$CLASSPATH;$j"
done


export CLASSPATH=$CLASSPATH:/opt/java/lib/tools.jar

export headless="-Djava.awt.headless=true"
export jruby_home="-Djruby.home=`dirname $0`/include/ruby"

java -ea -Djava.library.path=include $headless $jruby_home -ea -Xmx1000m -XX:MaxDirectMemorySize=600M "$@"

