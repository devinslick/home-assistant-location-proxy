#!/bin/bash
set -e

# ==================================================================================
# Script to build an Official Google Android Emulator Docker Image
# 
# PREREQUISITES:
# 1. Linux environment (Ubuntu/Debian recommended) or WSL2 on Windows.
# 2. Docker installed and running.
# 3. Python 3 installed.
# 4. wget installed.
#
# USAGE:
#   ./build_emulator_image.sh [API_LEVEL]
#   Example: ./build_emulator_image.sh 34
# ==================================================================================

# Default to Android 12L (API 32) as it's the latest available in the tool's default list
API_LEVEL="${1:-32}"
ARCH="x86_64"
VARIANT="google_apis_playstore" # We need Play Store for Location Sharing
EMULATOR_CHANNEL="stable"       # 'stable' or 'canary'

WORK_DIR="google-emulator-build"
REPO_URL="https://github.com/google/android-emulator-container-scripts.git"
IMAGE_TAG="android-emulator-32-playstore-vlc:latest"

echo ">>> Starting build process for Android API ${API_LEVEL} (${VARIANT})..."

# 0. Check dependencies
command -v docker >/dev/null 2>&1 || { echo >&2 "Docker is required but not installed. Aborting."; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo >&2 "Python3 is required but not installed. Aborting."; exit 1; }
command -v wget >/dev/null 2>&1 || { echo >&2 "wget is required but not installed. Aborting."; exit 1; }

# 0b. Check Docker Daemon Status
echo ">>> Checking Docker Daemon status..."
if ! docker info > /dev/null 2>&1; then
    echo ">>> Docker daemon is not running. Attempting to start..."
    # Try starting the service (works for native WSL docker)
    if command -v service >/dev/null 2>&1; then
        sudo service docker start
        sleep 3
    fi
fi

if ! docker info > /dev/null 2>&1; then
    echo "ERROR: Docker daemon is not reachable."
    echo "----------------------------------------------------------------"
    echo "Possible fixes:"
    echo "1. If using Docker Desktop: Ensure 'Use WSL 2 based engine' is ON"
    echo "   and your distro is enabled in 'Resources > WSL Integration'."
    echo "2. If using native Docker in WSL: Run 'sudo service docker start'"
    echo "----------------------------------------------------------------"
    exit 1
fi

# 1. Prepare Working Directory
if [ -d "$WORK_DIR" ]; then
    echo ">>> Directory '$WORK_DIR' exists. Updating..."
    cd "$WORK_DIR"
    git pull
else
    echo ">>> Cloning Google Emulator Scripts..."
    git clone "$REPO_URL" "$WORK_DIR"
    cd "$WORK_DIR"
fi

# 2. Configure Python Environment
echo ">>> Configuring Python environment..."
if [ ! -d "emu-venv" ]; then
    python3 -m venv emu-venv
fi

source emu-venv/bin/activate
# requirements.txt might not exist in newer versions of the repo, dependencies are in setup.cfg
if [ -f "requirements.txt" ]; then
    pip install -r requirements.txt
fi
pip install -e . # Install the emu-docker tool in the venv

EMU_CMD="emu-docker"

# 3. Accept Licenses
mkdir -p ~/.android
touch ~/.android/repositories.cfg

# 4. Find Download URLs
echo ">>> Querying available system images..."

# Capture the full list first
ALL_IMAGES=$($EMU_CMD list)

# Fetch Emulator URL (Linux Stable)
# Output format example: EMU stable 33.1.23 linux https://...
EMU_URL=$(echo "$ALL_IMAGES" | grep "EMU $EMULATOR_CHANNEL" | grep "linux" | head -n 1 | awk '{print $NF}')

if [ -z "$EMU_URL" ]; then
    echo "ERROR: Could not find a stable linux emulator URL."
    echo "Debug: Full list output:"
    echo "$ALL_IMAGES"
    exit 1
fi

# Fetch System Image URL
# Output format example: SYSIMG S google_apis_playstore x86_64 32 https://...
# We grep for VARIANT, ARCH, and API_LEVEL separately to handle column ordering
SYS_URL=$(echo "$ALL_IMAGES" | grep "SYSIMG" | grep "$VARIANT" | grep "$ARCH" | grep " $API_LEVEL " | head -n 1 | awk '{print $NF}')

if [ -z "$SYS_URL" ]; then
    echo "ERROR: Could not find system image for $VARIANT API $API_LEVEL $ARCH."
    echo "---------------------------------------------------------------"
    echo "Debug: Available images for API $API_LEVEL:"
    echo "$ALL_IMAGES" | grep "SYSIMG" | grep " $API_LEVEL " || echo "No images found for API $API_LEVEL"
    echo "---------------------------------------------------------------"
    echo "Debug: All 'google_apis_playstore' images:"
    echo "$ALL_IMAGES" | grep "google_apis_playstore"
    echo "---------------------------------------------------------------"
    exit 1
fi

echo "    Found Emulator: $EMU_URL"
echo "    Found System Image: $SYS_URL"

# 5. Download Files
echo ">>> Downloading Emulator Zip..."
wget -c "$EMU_URL" -O emulator.zip

echo ">>> Downloading System Image Zip..."
wget -c "$SYS_URL" -O system-image.zip

# 6. Create Docker Context (MANUAL MODE)
echo ">>> Creating Docker Context manually..."

# Check for unzip
command -v unzip >/dev/null 2>&1 || { echo >&2 "unzip is required but not installed. Aborting."; exit 1; }

# Download Platform Tools
echo ">>> Downloading Platform Tools..."
wget -c "https://dl.google.com/android/repository/platform-tools-latest-linux.zip" -O platform-tools.zip

# Prepare src directory
echo ">>> Preparing build context in 'src'..."
rm -rf src
mkdir -p src
mkdir -p src/emu
mkdir -p src/platform-tools

# Create the correct system image structure
# Structure: system-images/android-<api>/<variant>/<arch>
SYSIMG_REL_PATH="system-images/android-${API_LEVEL}/${VARIANT}/${ARCH}"
mkdir -p "src/sysimg/${SYSIMG_REL_PATH}"

# Unzip files
echo ">>> Unzipping files (this may take a moment)..."
unzip -q -o emulator.zip -d src/emu
unzip -q -o platform-tools.zip -d src/platform-tools
unzip -q -o system-image.zip -d "src/sysimg/${SYSIMG_REL_PATH}/.." 
# Note: system-image.zip usually contains the x86_64 folder. 
# So we unzip to the parent of ${ARCH} which is ${VARIANT}.
# Wait, if I unzip to .../${VARIANT}, and zip has x86_64, it creates .../${VARIANT}/x86_64.
# My previous mkdir created .../${VARIANT}/${ARCH}.
# So I should unzip to "src/sysimg/system-images/android-${API_LEVEL}/${VARIANT}"

# Correct unzip path for system image
SYSIMG_PARENT="src/sysimg/system-images/android-${API_LEVEL}/${VARIANT}"
mkdir -p "$SYSIMG_PARENT"
unzip -q -o system-image.zip -d "$SYSIMG_PARENT"

# Copy templates
echo ">>> Copying templates..."
cp emu/templates/launch-emulator.sh src/
cp emu/templates/default.pa src/

# Patch templates (replace placeholders)
sed -i 's/{{version}}/0.1.0/g' src/launch-emulator.sh
sed -i 's/{{extra}}//g' src/launch-emulator.sh

# Create Dockerfile
echo ">>> Creating Dockerfile..."
cat <<EOF > src/Dockerfile
FROM ubuntu:22.04

# Install dependencies
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \\
    libc6 libdbus-1-3 libfontconfig1 libgcc1 \\
    libpulse0 libtinfo5 libx11-6 libxcb1 libxdamage1 \\
    libnss3 libxcomposite1 libxcursor1 libxi6 \\
    libxext6 libxfixes3 zlib1g libgl1 pulseaudio socat \\
    curl ca-certificates unzip \\
    libxkbfile1 \\
    openjdk-17-jre-headless && \\
    apt-get clean && \\
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Configure user and directories
RUN mkdir -p /android/sdk/platforms && \\
    mkdir -p /android/sdk/platform-tools && \\
    mkdir -p /android/sdk/system-images && \\
    mkdir -p /android-home

# Copy files
COPY launch-emulator.sh /android/sdk/
COPY default.pa /etc/pulse/default.pa
COPY emu/ /android/sdk/
COPY sysimg/ /android/sdk/
COPY platform-tools/ /android/sdk/

# Permissions
RUN gpasswd -a root audio && \\
    chmod +x /android/sdk/launch-emulator.sh && \\
    chmod +x /android/sdk/platform-tools/adb

# Environment variables
ENV ANDROID_SDK_ROOT /android/sdk
ENV ANDROID_HOME /android/sdk
ENV PATH "\$PATH:/android/sdk/platform-tools"

# Create AVD Manually (Bypass avdmanager)
RUN mkdir -p /android-home/avd/Pixel2.avd
RUN echo "avd.ini.encoding=UTF-8" > /android-home/avd/Pixel2.ini && \\
    echo "path=/android-home/avd/Pixel2.avd" >> /android-home/avd/Pixel2.ini && \\
    echo "path.rel=avd/Pixel2.avd" >> /android-home/avd/Pixel2.ini && \\
    echo "target=android-${API_LEVEL}" >> /android-home/avd/Pixel2.ini

RUN echo "PlayStore.enabled=true" > /android-home/avd/Pixel2.avd/config.ini && \\
    echo "abi.type=${ARCH}" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "avd.ini.encoding=UTF-8" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.cpu.arch=${ARCH}" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.device.manufacturer=Google" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.device.name=pixel_2" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.lcd.density=420" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.lcd.height=1920" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.lcd.width=1080" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.mainKeys=no" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.sdCard=yes" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.sensors.orientation=yes" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "hw.sensors.proximity=yes" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "image.sysdir.1=system-images/android-${API_LEVEL}/${VARIANT}/${ARCH}/" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "tag.display=Google Play" >> /android-home/avd/Pixel2.avd/config.ini && \\
    echo "tag.id=${VARIANT}" >> /android-home/avd/Pixel2.avd/config.ini

# Ensure /root/.android exists for launch-emulator.sh symlinks
RUN mkdir -p /root/.android

# Ports
EXPOSE 5554
EXPOSE 5555
EXPOSE 8554
EXPOSE 5900

WORKDIR /android/sdk

# Entrypoint
CMD ["/android/sdk/launch-emulator.sh"]
EOF

BUILD_CONTEXT="src"
DOCKERFILE_PATH="src/Dockerfile"

# 7. Build the Docker Image
echo ">>> Building Docker Image: $IMAGE_TAG"
# Disable BuildKit since it is missing/broken in this environment
export DOCKER_BUILDKIT=0
# We build manually to ensure we control the tag and context
docker build -t "$IMAGE_TAG" "$BUILD_CONTEXT"

echo "=================================================================="
echo "SUCCESS!"
echo "Image built: $IMAGE_TAG"
echo ""
echo "Next steps:"
echo "1. Tag this image for your registry:"
echo "   docker tag $IMAGE_TAG my-registry.example.com/$IMAGE_TAG"
echo ""
echo "2. Push the image:"
echo "   docker push my-registry.example.com/$IMAGE_TAG"
echo ""
echo "3. Update your Kubernetes deployment yaml to use this image."
echo "=================================================================="
