@rem
@rem Minimal Gradle wrapper launcher for Windows.
@rem
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

set APP_HOME=%DIRNAME%

set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto execute
set JAVA_EXE=java.exe

:execute
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar;%APP_HOME%\gradle\wrapper\gradle-wrapper-shared.jar;%APP_HOME%\gradle\wrapper\gradle-cli.jar;%APP_HOME%\gradle\wrapper\gradle-files.jar;%APP_HOME%\gradle\wrapper\gradle-stdlib-java-extensions.jar;%APP_HOME%\gradle\wrapper\failureaccess.jar

"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
