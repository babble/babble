@echo off

REM Simple start-up script for webtest.

@setlocal

:: don't make any assumption concerning current dir
:: %~dp0 is expanded pathname of the current script under NT
set WEBTEST_HOME=%~dp0%..

set JAVA_OPTS=-Xms64M -Xmx256M

set RT_LIB=%WEBTEST_HOME%\lib
set BUILD_LIB=%WEBTEST_HOME%\lib\build

rem clover is only required to build webtest
set CLOVER_LIB=%BUILD_LIB%;%BUILD_LIB%\clover.jar

set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
if exist %JAVA_CMD% goto hasJavaHome
set JAVA_CMD="java.exe"
:hasJavaHome
rem echo Will use %JAVA_CMD%

set LOG4J=-Dlog4j.configuration=file:/%RT_LIB%/log4j.properties

set EXEC=%JAVA_CMD% %JAVA_OPTS% -cp "%RT_LIB%\ant-launcher-1.7.0.jar" -Dant.library.dir="%RT_LIB%" "%LOG4J%" org.apache.tools.ant.launch.Launcher -nouserlib -lib "%CLOVER_LIB%" %*
echo %EXEC%

%EXEC%
