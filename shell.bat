@echo off

java -classpath .\build;.\conf;.\include\* -ea -Djruby.home=.\include\ruby -Djava.library.path=.\include ed.js.Shell %*

