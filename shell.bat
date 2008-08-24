@echo off

java -classpath .\build;.\conf;.\include\* -ea -Djava.library.path=.\include ed.js.Shell %*

