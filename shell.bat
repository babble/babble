@echo off

java -classpath .\build;.\conf;.\include\*;.\include\jython\*;.\include\jython\javalib\* -ea -Djruby.home=.\include\ruby -Djava.library.path=.\include ed.js.Shell %*

