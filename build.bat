@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
cd /d C:\claudecode\CLAUDECODE\WidgetTodo
call gradlew.bat assembleDebug --no-daemon
