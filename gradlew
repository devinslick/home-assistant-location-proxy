#!/usr/bin/env sh
# ------------------------------------------------------------------------------
# Gradle start up script for UN*X
# ------------------------------------------------------------------------------

# Attempt to find java in standard locations
if [ -z "$JAVA_HOME" ]; then
  JAVACMD=java
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

# Determine the location of the wrapper jar
APP_HOME=$(dirname "$0")
APP_HOME=$(cd "$APP_HOME" && pwd)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVACMD" -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
