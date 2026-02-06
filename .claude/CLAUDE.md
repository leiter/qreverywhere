# QrEveryWhere Project Context

## Project Overview
QR code scanner/generator app built with Kotlin Multiplatform (KMP), supporting Android, iOS, Desktop (JVM), and Web (Wasm).

## Project Structure
```
QrEveryWhere/
├── androidApp/          # Android app module
├── shared/              # KMP shared module (commonMain, androidMain, iosMain, desktopMain, wasmJsMain)
├── desktopApp/          # Standalone desktop Compose application
├── webApp/              # Standalone Kotlin/Wasm web app (QR generation only)
├── qr_repository/       # Room database for QR history (legacy, merged into shared)
├── contacts/            # Contacts integration module
└── settings.gradle      # Groovy, not .kts
```

## Branch
- **Current:** `migrate_to_kmp`
- **Main branch:** `migrate_to_kmp`

## KMP Implementation Status

### Android: Complete ✅
- Camera with CameraX + ZXing
- QR generation/scanning with ZXing
- Image picker with PickVisualMedia
- Full permissions handling
- Database with Room

### iOS: Complete ✅
| Component | Status | Implementation |
|-----------|--------|----------------|
| QR Generation | Done | CIFilter (CIQRCodeGenerator) |
| QR Scanning (image) | Done | Vision framework (VNDetectBarcodesRequest) |
| QR Scanning (camera) | Done | AVCaptureMetadataOutput |
| Live Camera | Done | AVCaptureSession + UIKitViewController |
| Permissions (Camera) | Done | AVCaptureDevice |
| Permissions (Photos) | Done | PHPhotoLibrary |
| Image Picker | Done | PHPickerViewController + fallback |
| Clipboard | Done | UIPasteboard |
| Database | Done | Room + SQLite |
| User Preferences | Done | NSUserDefaults |
| Image Saving | Done | UIImageWriteToSavedPhotosAlbum |
| Share Sheet | Partial | Clipboard fallback only, needs UIActivityViewController |

### Desktop: Mostly Complete (~85%)
| Component | Status | Implementation |
|-----------|--------|----------------|
| QR Generation | Done | ZXing |
| QR Detection (image) | Done | ZXing |
| Image Picker | Done | JFileChooser |
| Database | Done | Room + SQLite |
| Image Resources | Done | Skia decoding |
| User Preferences | Done | File-based storage |
| Clipboard | Done | AWT Toolkit |
| File Saving | Done | java.nio.file API |
| App Entry Point | Done | desktopApp module with Compose Desktop |
| Live Camera | Stub | Shows message directing to file picker |

### Web (Wasm): Generator Only (~50%)
| Component | Status | Implementation |
|-----------|--------|----------------|
| QR Generation | Done | qrcode.js via JsFun interop |
| Image Download | Done | Blob + download trigger |
| Clipboard | Done | Navigator.clipboard API |
| User Preferences | Done | localStorage |
| Image Picker | Done | File input element |
| QR Scanning | Stub | Not implemented |
| Database | N/A | Stateless (no history) |
| Live Camera | Stub | Shows unavailable message |

**Note:** webApp is a standalone module (not using shared) due to Napier/Navigation dependency conflicts.

## QR Code Types Implementation
| Type | Creation | Detection | Notes |
|------|----------|-----------|-------|
| Text | Done | Done | Basic text QR via CreateTextScreen |
| URL | Done | Done | Auto-adds https:// via CreateTextScreen |
| Phone | Done | Done | tel: protocol via CreatePhoneScreen |
| SMS | Hidden | Done | Fully implemented but card hidden in CreateScreen.kt:85-90 |
| Email | Done | Done | mailto: with subject/body via CreateEmailScreen |
| vCard/Contact | Done | Done | Full creation UI via CreateVcardScreen |
| WiFi | Done | Done | WIFI: format via CreateWiFiScreen |

### Protocol Prefixes
- `tel:` - Phone numbers
- `mailto:` - Email addresses
- `https://` / `http://` - URLs
- `sms:` / `smsto:` - SMS messages
- `WIFI:T:...;S:...;P:...;;` - WiFi credentials
- `BEGIN:VCARD ... END:VCARD` - Contact cards

## Key Files

### QR Code Generation (per platform)
- `shared/src/commonMain/kotlin/.../usecase/QrCodeGenerator.kt` - Interface
- `shared/src/androidMain/kotlin/.../platform/AndroidQrCodeGenerator.kt` - ZXing
- `shared/src/iosMain/kotlin/.../platform/IosQrCodeGenerator.kt` - CIFilter
- `shared/src/desktopMain/kotlin/.../platform/DesktopQrCodeGenerator.kt` - ZXing
- `shared/src/wasmJsMain/kotlin/.../platform/WebQrCodeGenerator.kt` - qrcode.js

### Camera Views
- `shared/src/commonMain/kotlin/.../camera/` - Camera abstractions
- `shared/src/androidMain/kotlin/.../camera/CameraView.android.kt` - CameraX
- `shared/src/iosMain/kotlin/.../camera/CameraView.ios.kt` - AVCaptureSession
- `shared/src/desktopMain/kotlin/.../camera/CameraView.desktop.kt` - Stub

### Creation Screens
- `shared/src/commonMain/kotlin/.../presentation/screens/CreateScreen.kt` - Navigation hub
- `shared/src/commonMain/kotlin/.../presentation/screens/CreateTextScreen.kt` - Text/URL
- `shared/src/commonMain/kotlin/.../presentation/screens/CreatePhoneScreen.kt` - Phone
- `shared/src/commonMain/kotlin/.../presentation/screens/CreateEmailScreen.kt` - Email
- `shared/src/commonMain/kotlin/.../presentation/screens/CreateWiFiScreen.kt` - WiFi
- `shared/src/commonMain/kotlin/.../presentation/screens/CreateVcardScreen.kt` - vCard

### App Entry Points
- `androidApp/src/main/java/.../MainActivity.kt` - Android
- `desktopApp/src/desktopMain/kotlin/.../desktop/Main.kt` - Desktop
- `webApp/src/wasmJsMain/kotlin/.../web/Main.kt` - Web (standalone)

### Database
- `shared/src/commonMain/kotlin/.../data/db/QrDatabase.kt` - Room database
- `shared/src/commonMain/kotlin/.../data/repository/RoomQrRepository.kt` - Repository

### View Model
- `shared/src/commonMain/kotlin/.../presentation/viewmodel/MainViewModel.kt`

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

# Run Desktop app
./gradlew :desktopApp:run

# Build Desktop distribution
./gradlew :desktopApp:packageDistributionForCurrentOS

# Build Web app
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Build Web production
./gradlew :webApp:wasmJsBrowserProductionWebpack
```

## Dependencies
- Compose Multiplatform (UI across all platforms)
- Koin (dependency injection)
- ZXing (QR codes on Android/Desktop)
- CameraX (Android camera)
- Napier (logging - not available in webApp)
- kotlinx-datetime
- Room (database - KMP version)
- lifecycle-viewmodel (KMP version)
- navigation-compose (KMP version)

### Web-specific (webApp standalone)
- qrcode.js (external JS library for QR generation)

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

## Platform Completeness Matrix

| Feature | Android | iOS | Desktop | Web |
|---------|---------|-----|---------|-----|
| QR Generation | ✅ | ✅ | ✅ | ✅ |
| QR Scanning (image) | ✅ | ✅ | ✅ | ❌ |
| Live Camera Scanning | ✅ | ✅ | ❌ | ❌ |
| Image Picker | ✅ | ✅ | ✅ | ✅ |
| Permissions | ✅ | ✅ | N/A | N/A |
| Database/History | ✅ | ✅ | ✅ | ❌ |
| User Preferences | ✅ | ✅ | ✅ | ✅ |
| Image Saving | ✅ | ✅ | ✅ | ✅ |
| Share Sheet | ⏳ | ⏳ | Clipboard | ❌ |

## Missing Implementations & Improvements

### High Priority
1. **Desktop Live Camera** - Currently shows stub message; needs webcam integration (OpenCV or similar)
2. **iOS Share Sheet** - Uses clipboard fallback; needs UIActivityViewController implementation
3. **SMS Creation UI** - Fully implemented in CreateTextScreen.kt but card hidden in CreateScreen.kt:85-90; just uncomment to enable
4. **Web QR Scanning** - webApp only generates QR codes; scanning not implemented

### Medium Priority
1. **Web Database/History** - webApp is stateless; could add IndexedDB for history
2. **Android/iOS Share Sheet Improvements** - Full native share sheet vs clipboard fallback
3. **Batch QR Generation** - Generate multiple QR codes at once
4. **QR Code Customization** - Colors, logos, error correction levels

### Low Priority / Future Enhancements
1. **Web Integration with Shared Module** - Currently webApp is standalone due to Napier/Navigation conflicts
2. **vCard Photo Support** - Add photo field to contact QR codes
3. **WiFi QR Import** - Auto-connect to WiFi from scanned QR (platform-specific)
4. **Calendar Event QR** - VEVENT format support
5. **Location QR** - geo: URI format support

### Technical Debt
1. **qr_repository module** - Legacy module; functionality merged into shared
2. **contacts module** - Evaluate if still needed or can be merged

## Session Notes
- Legacy code was deleted in commit b1d68a6
- FAB for "Scan from file" follows legacy pattern (ExtendedFloatingActionButton)
- webApp added in commit 4e69c05 (Feb 5, 2026)
- Desktop platform completed in commit 5a7dc2e (Feb 5, 2026)
- WiFi/vCard creation screens added in commit 5a7dc2e

---

## Roadmap

### Phase 1: Architecture & Code Quality
| Feature | Priority | Description |
|---------|----------|-------------|
| Unit Tests | High | Test coverage for shared module (QR generation, parsing, repository) |
| UI Tests | High | Compose UI tests for critical flows (scan, create, history) |
| Error Handling | High | Centralized error handling with user-friendly messages |
| Offline-First | Medium | Queue operations when offline, sync when back online |

### Phase 2: UX Improvements
| Feature | Priority | Description |
|---------|----------|-------------|
| Dark Mode | High | Follow system theme preference across all platforms |
| Haptic Feedback | Medium | Vibrate on successful QR scan (Android/iOS) |
| QR Preview Animation | Low | Animate QR code generation |
| History Search/Filter | High | Search through scanned/created QR codes |
| Favorites/Pinning | Medium | Pin frequently used QR codes |
| QR Categories | Medium | Organize history by type (URL, WiFi, Contact, etc.) |
| Bulk Delete | Medium | Select multiple history items for deletion |
| Undo Delete | Medium | Snackbar with undo option after deleting |

### Phase 3: Security
| Feature | Priority | Description |
|---------|----------|-------------|
| Malicious URL Detection | High | Warn users before opening suspicious URLs from scanned QR |
| WiFi Password Masking | Medium | Hide password by default when displaying scanned WiFi QR |

### Phase 4: Platform-Specific Enhancements
| Feature | Platform | Description |
|---------|----------|-------------|
| Home Screen Widget | Android | Quick scan or show saved QR from home screen |
| Home Screen Widget | iOS | WidgetKit implementation |
| App Shortcuts | Android | Shortcuts for "New Scan" and "Create QR" |
| Wear OS Companion | Android | Display QR codes on smartwatch |
| Menu Bar App | macOS | Quick access from menu bar |

### Phase 5: Distribution & DevOps
| Feature | Priority | Description |
|---------|----------|-------------|
| CI/CD Pipeline | High | GitHub Actions for automated builds and testing |
| Fastlane | Medium | Automated iOS/Android store deployment |
| Crash Reporting | High | Firebase Crashlytics or Sentry integration |
| Analytics | Medium | Track feature usage to prioritize improvements |

### Phase 6: New QR Types
| Type | Format | Description |
|------|--------|-------------|
| Calendar Event | VEVENT | Create meeting/event QR codes |
| Location | geo: | Share GPS coordinates |
| App Store Links | URL | Deep links to app stores |
| PayPal/Venmo | URL | Payment request QR codes |
| Bitcoin/Crypto | bitcoin: | Wallet address QR codes |
| MeCard | MECARD: | Alternative contact format (simpler than vCard) |

### Phase 7: Performance (Future)
| Feature | Priority | Description |
|---------|----------|-------------|
| QR Generation Caching | Medium | Cache generated QR images to avoid regeneration |
| Lazy Loading History | Medium | Paginate history list for large datasets |
| Image Compression | Low | Compress saved QR images to reduce storage |

---

## Implementation Branches

Each phase will be implemented on a separate branch, branching from the previous:

```
main (migrate_to_kmp)
  └── feature/phase1-architecture-quality
        └── feature/phase2-ux-improvements
              └── feature/phase3-security
                    └── feature/phase4-platform-specific
                          └── feature/phase5-devops
                                └── feature/phase6-new-qr-types
```
