#!/bin/bash

# ============================================
# Remix Wings - Release APK Build Script
# ============================================

echo "================================"
echo "   Remix Wings APK Builder"
echo "================================"
echo ""

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: build.gradle.kts not found!"
    echo "Please run this script from the project root directory."
    exit 1
fi

echo "📋 Build Information:"
echo "   • App Name: Remix Wings"
echo "   • Application ID: com.aistudio.wings.chats"
echo "   • Version: 1.0"
echo "   • Min SDK: 24"
echo "   • Target SDK: 36"
echo ""

# Check if keystore exists
if [ -z "$KEYSTORE_PATH" ]; then
    echo "⚠️  Warning: KEYSTORE_PATH environment variable not set"
    echo "   Using default: ./my-upload-key.jks"
    export KEYSTORE_PATH="./my-upload-key.jks"
fi

if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "❌ Error: Keystore file not found at $KEYSTORE_PATH"
    echo ""
    echo "To create a keystore, run:"
    echo "  keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload"
    exit 1
fi

echo "✅ Keystore found: $KEYSTORE_PATH"
echo ""

# Check for required environment variables
if [ -z "$STORE_PASSWORD" ] || [ -z "$KEY_PASSWORD" ]; then
    echo "⚠️  Warning: STORE_PASSWORD or KEY_PASSWORD not set"
    echo "   Please ensure these environment variables are configured."
    exit 1
fi

echo "🔨 Starting build process..."
echo ""

# Clean build
echo "• Step 1: Cleaning previous builds..."
./gradlew clean

if [ $? -ne 0 ]; then
    echo "❌ Clean failed!"
    exit 1
fi

echo ""
echo "• Step 2: Building release APK..."
./gradlew assembleRelease

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✅ Build completed successfully!"
echo ""

# Check if APK was generated
APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo "📦 APK Generated Successfully!"
    echo "   Location: $APK_PATH"
    echo "   Size: $APK_SIZE"
    echo ""
    echo "✨ Next steps:"
    echo "   1. Test on device: adb install $APK_PATH"
    echo "   2. Upload to Google Play Store"
    echo "   3. Or share the APK file directly"
else
    echo "❌ APK file not found at expected location!"
    exit 1
fi

echo ""
echo "================================"
echo "   Build Complete!"
echo "================================"
