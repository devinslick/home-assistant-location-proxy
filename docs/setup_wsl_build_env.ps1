<#
.SYNOPSIS
    Sets up the Windows Subsystem for Linux (WSL) environment required to build the Android Emulator Docker image.
.DESCRIPTION
    This script checks for WSL installation, ensures a Linux distribution is available, and installs
    the necessary dependencies (Python, Docker, wget) inside the Linux environment.
    
    Run this script from PowerShell as Administrator if WSL is not yet installed.
#>

$ErrorActionPreference = "Stop"

function Test-CommandExists {
    param ($command)
    $oldPreference = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    $exists = (Get-Command $command)
    $ErrorActionPreference = $oldPreference
    return $exists
}

Write-Host ">>> Checking WSL Status..." -ForegroundColor Cyan

if (-not (Test-CommandExists wsl)) {
    Write-Host "WSL is not installed. Installing WSL..." -ForegroundColor Yellow
    # This command requires admin privileges and a reboot
    Start-Process "wsl" -ArgumentList "--install" -Verb RunAs -Wait
    Write-Host "WSL installation started. Please restart your computer when prompted, then run this script again." -ForegroundColor Red
    exit
}

$wslStatus = wsl --status
if ($LASTEXITCODE -ne 0) {
    Write-Host "WSL is installed but may not be configured correctly. Attempting to update..." -ForegroundColor Yellow
    wsl --update
}

# Check if any distro is installed
$list = wsl --list --quiet
if (-not $list) {
    Write-Host "No WSL distribution found. Installing Ubuntu..." -ForegroundColor Yellow
    wsl --install -d Ubuntu
    Write-Host "Ubuntu installed. Please complete the initial setup in the new terminal window, then run this script again." -ForegroundColor Yellow
    exit
}

Write-Host ">>> Configuring Default WSL Distribution..." -ForegroundColor Cyan

# Define the setup script to run inside Linux
$linuxScript = @"
set -e
echo '>>> [WSL] Updating package lists...'
sudo apt-get update

echo '>>> [WSL] Installing dependencies...'
sudo apt-get install -y python3 python3-pip python3-venv wget git unzip dos2unix

# Check for Docker
if ! command -v docker &> /dev/null; then
    echo '>>> [WSL] Docker not found. Installing docker.io...'
    sudo apt-get install -y docker.io
    
    # Add user to docker group to avoid sudo for docker commands
    if ! groups | grep -q docker; then
        echo '>>> [WSL] Adding user to docker group...'
        sudo usermod -aG docker `$USER
        echo '>>> [WSL] NOTE: You may need to log out and back in (or restart WSL) for docker group changes to apply.'
    fi
else
    echo '>>> [WSL] Docker is already installed.'
fi

# Check if KVM is accessible (optional but good for info)
if [ -e /dev/kvm ]; then
    echo '>>> [WSL] KVM device found (/dev/kvm). Hardware acceleration should work.'
    # Ensure user has access to kvm
    if ! groups | grep -q kvm; then
         sudo usermod -aG kvm `$USER
    fi
else
    echo '>>> [WSL] WARNING: /dev/kvm not found. Hardware acceleration might not be available.'
    echo '    Ensure Virtualization is enabled in BIOS and Nested Virtualization is enabled for WSL.'
fi
"@

# Execute the script inside WSL
# We write the script to a temp file in the current directory (which is mounted in WSL)
$currentDir = Get-Location
$tempScriptName = "wsl_bootstrap_temp.sh"
$tempScriptPath = Join-Path $currentDir $tempScriptName

# Write with LF line endings to ensure Bash accepts it
[IO.File]::WriteAllText($tempScriptPath, $linuxScript.Replace("`r`n", "`n"))

Write-Host ">>> Executing setup script inside WSL..." -ForegroundColor Cyan

# Convert Windows path to WSL path
# e.g. C:\Users\Devin... -> /mnt/c/Users/Devin...
# We assume the standard mount point /mnt/c for C: drive
$driveLetter = $tempScriptPath.Substring(0, 1).ToLower()
$pathWithoutDrive = $tempScriptPath.Substring(2).Replace("\", "/")
$wslPath = "/mnt/$driveLetter$pathWithoutDrive"

try {
    # Run the script inside WSL
    wsl bash -c "chmod +x '$wslPath' && '$wslPath'"
}
catch {
    Write-Error "Failed to execute script in WSL. Ensure WSL is running."
}
finally {
    # Cleanup
    Remove-Item $tempScriptPath -ErrorAction SilentlyContinue
}

Write-Host ">>> Setup Complete!" -ForegroundColor Green
Write-Host "You can now build the emulator image."
Write-Host "1. Open your WSL terminal (type 'wsl' in PowerShell)."
Write-Host "2. Navigate to this repository."
Write-Host "3. Run: bash scripts/build_emulator_image.sh"
