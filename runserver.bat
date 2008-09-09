@echo off

java -classpath .\build;.\conf;.\include\* -ea -Djruby.home=.\build\ruby -Djava.library.path=.\include ed.appserver.AppServer %*

