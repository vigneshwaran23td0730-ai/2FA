@echo off
setlocal

:: ----------------------------------------------------------------------------
:: Maven Start Up Batch script
::
:: Required ENV vars:
::   JAVA_HOME - location of a JDK home dir
::
:: Optional ENV vars
::   M2_HOME - location of maven2's installed home dir
::   MAVEN_OPTS - parameters passed to the Java VM when running Maven
::   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
:: ----------------------------------------------------------------------------

set MAVEN_SKIP_RC=
if defined M2_HOME goto copy
if defined JAVA_HOME goto copy

:: Try to locate java on PATH if JAVA_HOME is not set
for %%I in (java.exe) do set _JAVA_CMD=%%~$PATH:I
if defined _JAVA_CMD goto copy

echo Error: JAVA_HOME is not set and java could not be found in your PATH.
exit /b 1

:copy
setlocal enableDelayedExpansion
set "scriptDir=%~dp0"
set "scriptDir=%scriptDir:~0,-1%"

set "MAVEN_PROJECTBASEDIR=%scriptDir%"
set "WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
set "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo Error: The Maven wrapper jar is missing: "%WRAPPER_JAR%"
  exit /b 1
)

if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
  ) else if exist "%JAVA_HOME%\java.exe" (
    set "JAVACMD=%JAVA_HOME%\java.exe"
  ) else (
    echo Error: JAVA_HOME does not point to a valid JDK home: %JAVA_HOME%
    exit /b 1
  )
) else (
  set "JAVACMD=%_JAVA_CMD%"
)

"%JAVACMD%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
