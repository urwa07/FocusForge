@ECHO OFF
SET DIRNAME=%~dp0
IF "%DIRNAME%"=="" SET DIRNAME=.
SET APP_HOME=%DIRNAME%

IF NOT "%JAVA_HOME%"=="" GOTO useJavaHome
SET JAVA_EXE=java.exe
GOTO execute

:useJavaHome
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

:execute
"%JAVA_EXE%" -Xmx64m -Xms64m -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
