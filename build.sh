#!/bin/bash

# Toyota Corolla E210 OBD2 Application Build Script
# ================================================

set -e

echo "🔧 Building Toyota Corolla E210 OBD2 Application..."
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found! Please install JDK 17+"
    exit 1
fi

# Check for Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "⚠️  ANDROID_HOME or ANDROID_SDK_ROOT not set"
    echo "   Attempting to detect Android SDK..."
    
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
        echo "   Found Android SDK at: $ANDROID_HOME"
    elif [ -d "/opt/android-sdk" ]; then
        export ANDROID_HOME="/opt/android-sdk"
        echo "   Found Android SDK at: $ANDROID_HOME"
    fi
fi

# Create gradle wrapper if not exists
if [ ! -f "gradlew" ]; then
    echo "📦 Creating Gradle Wrapper..."
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.5
    else
        echo "❌ Gradle not found! Please install Gradle or create gradlew manually"
        exit 1
    fi
fi

# Make gradlew executable
chmod +x gradlew

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean --quiet

# Build debug APK
echo ""
echo "📱 Building debug APK..."
./gradlew assembleDebug --stacktrace

# Check if APK was created
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📍 APK Location: $(pwd)/$APK_PATH"
    echo ""
    ls -lh "$APK_PATH"
else
    echo ""
    echo "❌ Build failed - APK not found"
    exit 1
fi

echo ""
echo "🎉 Done!"