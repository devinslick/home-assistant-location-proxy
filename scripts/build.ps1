Write-Host "Starting build process..."

# Set JAVA_HOME to the JBR we found (using the junction to avoid spaces)
$env:JAVA_HOME = "C:\Users\Devin\jbr"

# Check if JAVA_HOME exists
if (-not (Test-Path $env:JAVA_HOME)) {
    Write-Host "Error: JAVA_HOME not found at $env:JAVA_HOME" -ForegroundColor Red
    Write-Host "Please ensure Android Studio is installed or update the path in this script."
    exit 1
}

Write-Host "Using JAVA_HOME: $env:JAVA_HOME"

# Run the build
./gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
    Write-Host "APK location: app\build\outputs\apk\debug\app-debug.apk"
} else {
    Write-Host "Build failed." -ForegroundColor Red
}
