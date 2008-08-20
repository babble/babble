#!/bin/bash
# -x

# Simple start-up script for webtest.

# Look out, don't forget to adapt the path in webtest.bat!

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$WEBTEST_HOME" ] &&
    WEBTEST_HOME=`cygpath --unix "$WEBTEST_HOME"`
fi

if [ -z "$WEBTEST_HOME" -o ! -d "$WEBTEST_HOME" ] ; then
	# don't make any assumption concerning current dir
	# (Code from ant shell script)
	WEBTEST_HOME="$0"

	# need this for relative symlinks
	while [ -h "$WEBTEST_HOME" ] ; do
	  ls=`ls -ld "$WEBTEST_HOME"`
	  link=`expr "$ls" : '.*-> \(.*\)$'`
	  if expr "$link" : '/.*' > /dev/null; then
	    WEBTEST_HOME="$link"
	  else
	    WEBTEST_HOME=`dirname "$WEBTEST_HOME"`"/$link"
	  fi
	done
	WEBTEST_HOME=`dirname "$WEBTEST_HOME"`/..

	# make it fully qualified
	WEBTEST_HOME=`cd "$WEBTEST_HOME" && pwd`
fi

JAVA_OPTS="-Xms64M -Xmx256M"

RT_LIB=$WEBTEST_HOME/lib
BUILD_LIB=$WEBTEST_HOME/lib/build

CLOVER_LIB=$BUILD_LIB:$BUILD_LIB/clover.jar

JAVA_CMD="$JAVA_HOME/bin/java"
if [ ! -x "$JAVA_CMD" ] ; then
    JAVA_CMD=java
fi
echo Will use $JAVA_CMD

# For Cygwin, switch paths to appropriate format before running java
if $cygwin ; then
    JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
    CLOVER_LIB=`cygpath --windows --path "$CLOVER_LIB"`
    RT_LIB=`cygpath --windows "$RT_LIB"`
    WEBTEST_HOME=`cygpath --windows "$WEBTEST_HOME"`
fi

LOG4J=-Dlog4j.configuration="file://${cygwin:+/}$RT_LIB/log4j.properties"

# Let webtest know its location
export WEBTEST_HOME

exec $JAVA_CMD $JAVA_OPTS -cp "$RT_LIB/ant-launcher-1.7.0.jar" -Dant.library.dir="$RT_LIB" $LOG4J org.apache.tools.ant.launch.Launcher -nouserlib -lib "$CLOVER_LIB" "$@"
