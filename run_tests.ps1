$env:JAVA_HOME = "C:\claudecode\CLAUDECODE\jdk17\jdk-17.0.2"
Set-Location "C:\claudecode\CLAUDECODE\WidgetTodo"
Write-Host "Running Unit Tests..."
& .\gradlew.bat test --no-daemon
Write-Host "Unit Test exit code: $LASTEXITCODE"
