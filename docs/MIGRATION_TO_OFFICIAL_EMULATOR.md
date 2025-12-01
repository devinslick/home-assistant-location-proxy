# Migrating to Official Google Android Emulator Containers

This guide outlines how to migrate from the current `budtmo/docker-android` setup to the official **Google Android Emulator Container Scripts**. This approach uses the exact same system images as Android Studio, ensuring better compatibility with Google Play Services, Location Sharing, and modern Android versions.

## Why this alternative?

While **Cuttlefish** is excellent for AOSP development, it often lacks out-of-the-box support for Google Play Services (GMS) and the Play Store, which are strictly required for Location Sharing.

The **Google Android Emulator Container** approach:
1.  Uses official Android Studio system images (including `google_apis_playstore`).
2.  Supports the latest Android APIs (API 34, 35, etc.).
3.  Is maintained by Google for CI/CD and cloud usage.

## Prerequisites

- Linux environment with KVM enabled (required for performance).
- Docker installed.
- Python 3.

## Step 1: Build the Docker Image

You will need to build the image on a machine with Docker and Python.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/google/android-emulator-container-scripts.git
    cd android-emulator-container-scripts
    ```

2.  **Configure the environment:**
    ```bash
    source ./configure.sh
    ```

3.  **Select and Create the Docker Context:**
    Find the latest Play Store image (e.g., API 34 or 35):
    ```bash
    emu-docker list | grep "google_apis_playstore"
    ```
    
    Create the Dockerfile for Android 14 (API 34) or 15 (API 35):
    ```bash
    # Example for Android 14 (API 34)
    emu-docker create stable "android-34;google_apis_playstore;x86_64"
    ```

4.  **Build the Image:**
    The previous command will output a `docker build` command. Run it.
    ```bash
    docker build -t my-registry/android-emulator:34-playstore .
    ```

5.  **Push to your Registry:**
    ```bash
    docker push my-registry/android-emulator:34-playstore
    ```

## Step 2: Kubernetes Deployment

Create a new deployment file (e.g., `k8s/official-emulator.yaml`) using the image you just built.

### Key Differences from Previous Setup
- **Ports**: Exposes 5555 (ADB) and 8554 (gRPC).
- **KVM**: Must mount `/dev/kvm`.
- **Web Access**: The official container uses a different mechanism (Envoy/WebRTC) for web access, or you can simply use ADB forwarding to view it locally via `scrcpy` or Android Studio's Device Mirroring.

### Example Deployment Manifest

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: location-proxy-official
  namespace: homeautomation
  labels:
    app: location-proxy-official
spec:
  replicas: 1
  selector:
    matchLabels:
      app: location-proxy-official
  template:
    metadata:
      labels:
        app: location-proxy-official
    spec:
      containers:
      - name: emulator
        image: my-registry/android-emulator:34-playstore  # REPLACE THIS
        imagePullPolicy: Always
        resources:
          limits:
            memory: "8Gi"
            cpu: "4"
        securityContext:
          privileged: true  # Required for KVM access
        ports:
        - containerPort: 5555
          name: adb
        - containerPort: 8554
          name: grpc
        env:
        - name: ADBKEY
          valueFrom:
            secretKeyRef:
              name: adb-keys
              key: private_key
        volumeMounts:
        - name: dev-kvm
          mountPath: /dev/kvm
        - name: android-data
          mountPath: /data
      volumes:
      - name: dev-kvm
        hostPath:
          path: /dev/kvm
      - name: android-data
        persistentVolumeClaim:
          claimName: android-emulator-pvc
```

## Step 3: Connecting & Configuring

Once running:

1.  **Connect via ADB:**
    ```bash
    kubectl port-forward deploy/location-proxy-official 5555:5555
    adb connect localhost:5555
    ```

2.  **Install the Proxy App:**
    ```bash
    ./gradlew installDebug
    ```

3.  **Configure Location:**
    The official emulator supports the same `adb geo fix` and mock location commands.
    ```bash
    adb shell appops set com.devinslick.homeassistantlocationproxy android:mock_location allow
    ```

## Troubleshooting Location Sharing

Since this image is a certified "Play Store" image (unlike the open-source AOSP ones), the "Location Sharing isn't enabled" error should be resolved natively. Google signs these images specifically to allow GMS features to work correctly.
