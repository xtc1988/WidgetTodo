$env:JAVA_HOME = "C:\claudecode\CLAUDECODE\jdk17\jdk-17.0.2"
Set-Location "C:\claudecode\CLAUDECODE\WidgetTodo"
Write-Host "Running E2E/Instrumented Tests..."
& .\gradlew.bat connectedAndroidTest --no-daemon
Write-Host "E2E Test exit code: $LASTEXITCODE"
