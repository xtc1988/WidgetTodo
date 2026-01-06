# Clear Gradle Cache Script
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
Set-Location "C:\claudecode\CLAUDECODE\WidgetTodo"

Write-Host "=== 1. Stopping Gradle Daemon ===" -ForegroundColor Yellow
& .\gradlew.bat --stop

Write-Host ""
Write-Host "=== 2. Clearing Gradle transforms cache ===" -ForegroundColor Yellow
$transformsPath = "$env:USERPROFILE\.gradle\caches\transforms-3"
if (Test-Path $transformsPath) {
    Remove-Item -Path "$transformsPath\*" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "Cleared: $transformsPath" -ForegroundColor Green
} else {
    Write-Host "Not found: $transformsPath" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== 3. Clearing local .gradle folder ===" -ForegroundColor Yellow
$localGradle = "C:\claudecode\CLAUDECODE\WidgetTodo\.gradle"
if (Test-Path $localGradle) {
    Remove-Item -Path $localGradle -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "Cleared: $localGradle" -ForegroundColor Green
} else {
    Write-Host "Not found: $localGradle" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== 4. Running Gradle Sync (assembleDebug) ===" -ForegroundColor Yellow
& .\gradlew.bat assembleDebug --no-daemon

Write-Host ""
Write-Host "=== CACHE CLEAR COMPLETE ===" -ForegroundColor Cyan
Write-Host "Now open Android Studio and run: File -> Sync Project with Gradle Files" -ForegroundColor Cyan
