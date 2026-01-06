# Full Build and Test Verification Script
# Uses Android Studio's JBR for consistency

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
Set-Location "C:\claudecode\CLAUDECODE\WidgetTodo"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
& "$env:JAVA_HOME\bin\java.exe" -version
Write-Host ""

# 1. Clean build
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "1. Cleaning project..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
& .\gradlew.bat clean --no-daemon
Write-Host ""

# 2. Debug Build
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "2. Building Debug APK..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
& .\gradlew.bat assembleDebug --no-daemon
$debugBuildResult = $LASTEXITCODE
Write-Host "Debug Build Exit Code: $debugBuildResult" -ForegroundColor $(if ($debugBuildResult -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 3. Release Build
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "3. Building Release APK..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
& .\gradlew.bat assembleRelease --no-daemon
$releaseBuildResult = $LASTEXITCODE
Write-Host "Release Build Exit Code: $releaseBuildResult" -ForegroundColor $(if ($releaseBuildResult -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 4. Unit Tests
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "4. Running Unit Tests..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
& .\gradlew.bat test --no-daemon
$unitTestResult = $LASTEXITCODE
Write-Host "Unit Test Exit Code: $unitTestResult" -ForegroundColor $(if ($unitTestResult -eq 0) { "Green" } else { "Red" })
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Debug Build:   $(if ($debugBuildResult -eq 0) { 'PASS' } else { 'FAIL' })" -ForegroundColor $(if ($debugBuildResult -eq 0) { "Green" } else { "Red" })
Write-Host "Release Build: $(if ($releaseBuildResult -eq 0) { 'PASS' } else { 'FAIL' })" -ForegroundColor $(if ($releaseBuildResult -eq 0) { "Green" } else { "Red" })
Write-Host "Unit Tests:    $(if ($unitTestResult -eq 0) { 'PASS' } else { 'FAIL' })" -ForegroundColor $(if ($unitTestResult -eq 0) { "Green" } else { "Red" })
Write-Host "========================================" -ForegroundColor Cyan

# Overall result
$allPassed = ($debugBuildResult -eq 0) -and ($releaseBuildResult -eq 0) -and ($unitTestResult -eq 0)
if ($allPassed) {
    Write-Host "ALL VERIFICATIONS PASSED!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "SOME VERIFICATIONS FAILED!" -ForegroundColor Red
    exit 1
}
