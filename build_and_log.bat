@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
cd /d C:\claudecode\CLAUDECODE\WidgetTodo
echo JAVA_HOME is: %JAVA_HOME%
echo Current dir is: %CD%
call gradlew.bat assembleDebug --no-daemon > build_output.txt 2>&1
echo Build exit code: %ERRORLEVEL%
