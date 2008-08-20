:: WARNING: this script is only experimental now

@echo off
:: IMPORTANT: any modification in this script file must be reflected in the unix version

@setlocal

::Simple runner script for webtest.

:: don't make any assumption concerning current dir
:: %~dp0 is expanded pathname of the current script under NT
set WEBTEST_HOME=%~dp0%..

set WEBTEST_RUNNER="%WEBTEST_HOME%\resources\webtestsRunner.xml"

set ARGS=-Dwt.headless=%wt.headless% -Dwebtest.testdir=%cd%
if "%1" == "" goto testFileSet
set ARGS=%ARGS% -Dwebtest.testfile=%1
shift

:testFileSet

set EXEC=%WEBTEST_HOME%\bin\webtest.bat -f %WEBTEST_RUNNER% %ARGS%
echo %EXEC%
%EXEC%
