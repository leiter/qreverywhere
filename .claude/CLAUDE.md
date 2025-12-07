# QrEveryWhere Project Context

## Project Overview
QR code scanner/generator app migrating from Android-only to Kotlin Multiplatform (KMP).

## Project Structure
```
QrEveryWhere/
├── androidApp/          # Android app module (renamed from 'app')
├── shared/              # KMP shared module (commonMain, androidMain, iosMain, desktopMain)
├── qr_repository/       # Room database for QR history
├── contacts/            # Contacts integration module
└── settings.gradle      # Groovy, not .kts
```

## Branch
- **Current:** `migrate_to_kmp`
- **Main branch:** (not set)

## KMP Implementation Status

### Android: Complete
- Camera with CameraX + ZXing
- QR generation/scanning
- Image picker with PickVisualMedia

### iOS: Partial (~30%)
| Component | Status | Implementation |
|-----------|--------|----------------|
| QR Generation | Done | CIFilter |
| QR Scanning | Done | Vision framework |
| Permissions | Done | AVCaptureDevice |
| Image Picker | Done | PHPickerViewController |
| Live Camera | TODO | Needs AVCaptureSession |

### Desktop: Partial
- QR detection from images: Done (ZXing)
- Image picker: Done (JFileChooser)

## Key Files

### Shared Module
- `shared/src/commonMain/kotlin/.../camera/` - Camera abstractions
- `shared/src/commonMain/kotlin/.../presentation/screens/ScanScreen.kt` - Main scan UI
- `shared/src/iosMain/kotlin/.../platform/IosQrCodeGenerator.kt` - iOS QR impl

### Resources
- Compose resources: `shared/src/commonMain/composeResources/`
- Strings: `shared/src/commonMain/composeResources/values/strings.xml`

## Known Issues & Solutions

### Compose Multiplatform Drawables
**Problem:** `@android:color/white` crashes app
**Solution:** Use hex colors (`#000000`) in vector drawables in shared module

```xml
<!-- WRONG -->
<path android:fillColor="@android:color/white" .../>

<!-- CORRECT -->
<path android:fillColor="#000000" .../>
```

### Multiple ADB Devices
Use `-d` for physical device, `-e` for emulator:
```bash
adb -d install app.apk
adb -d logcat
```

### iOS Build (requires Mac)
```bash
./gradlew :shared:compileKotlinIosArm64
./gradlew :shared:compileKotlinIosSimulatorArm64
```

## Common Commands

```bash
# Build Android
./gradlew :androidApp:assembleDebug

# Install to physical device
adb -d install -r androidApp/build/outputs/apk/debug/app-debug.apk

# View crash logs
adb -d logcat | grep -i "AndroidRuntime\|FATAL"

# Compile shared for Android
./gradlew :shared:compileDebugKotlinAndroid
```

## Dependencies
- Compose Multiplatform
- Koin (DI)
- ZXing (QR codes on Android/Desktop)
- CameraX (Android camera)
- Napier (logging)
- kotlinx-datetime

## Info.plist Keys (for iOS)
```xml
<key>NSCameraUsageDescription</key>
<string>Used to scan QR codes</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>Used to select images containing QR codes</string>
```

## UI Coordinates
<!-- Populated during self-controlling loop testing -->
| Screen | Element | Coordinates | Device/Resolution |
|--------|---------|-------------|-------------------|
| | | | |

## Session Notes
- Legacy code was deleted in commit b1d68a6
- FAB for "Scan from file" follows legacy pattern (ExtendedFloatingActionButton)
