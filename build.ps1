# Use Android Studio's embedded JBR for consistency
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
Set-Location "C:\claudecode\CLAUDECODE\WidgetTodo"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Java version:"
& "$env:JAVA_HOME\bin\java.exe" -version
Write-Host ""
Write-Host "Building..."
& .\gradlew.bat assembleDebug --no-daemon
Write-Host "Build exit code: $LASTEXITCODE"
