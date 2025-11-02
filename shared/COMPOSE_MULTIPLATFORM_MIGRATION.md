# Compose Multiplatform Migration Summary

## ✅ Completed Tasks

### 1. Compose Multiplatform Infrastructure
- **Added Compose Multiplatform plugin** (version 1.7.1) to the shared module
- **Updated Kotlin** to version 2.1.0 to support the latest KMP libraries
- **Added Koin 4.0.1** with BOM for dependency injection in KMP
- **Configured build.gradle.kts** with all necessary dependencies:
  - Compose Multiplatform (runtime, foundation, material3, ui)
  - androidx.lifecycle.ViewModel 2.8.0 for commonMain
  - Navigation Compose 2.8.0-alpha10 for multiplatform
  - kotlinx-datetime for cross-platform date/time
  - Koin for dependency injection

### 2. Shared ViewModel Migration
- **MainViewModel** now extends `androidx.lifecycle.ViewModel` from commonMain
- Uses `viewModelScope` for coroutine management
- **Koin configuration** updated to use `factory` DSL for ViewModel creation
- All business logic is now in the shared module:
  - QR history management
  - QR item creation and deletion
  - Image generation integration
  - User preferences integration

### 3. Shared UI Components
Created initial Compose Multiplatform screens in `shared/src/commonMain/kotlin/`:

- **HistoryScreen.kt** - Displays list of QR codes
  - Uses shared MainViewModel
  - Shows QR history with text content and type
  - Handles empty state
  - Platform-independent implementation

### 4. Shared Navigation
- **Screen.kt** - Sealed class defining all navigation destinations
- **AppNavHost.kt** - Navigation host using Compose Multiplatform Navigation
  - Configured for History, Scan, Create, Detail screens
  - Uses NavHostController from `androidx.navigation.compose`
  - Platform-independent navigation structure

### 5. Domain Model Updates
- **QrItem** converted to `data class` for proper `copy()` support
- Custom `equals()` and `hashCode()` for ByteArray handling
- Fully shared across all platforms

## 📋 Platform-Specific Code Requirements

The following features require `expect/actual` declarations or platform-specific implementations:

### High Priority - Camera/Scanner
**Location**: `ComposeScanQrScreen.kt`
- **CameraX APIs** - Android-specific camera preview and QR scanning
- **Permissions** - Camera and storage permissions handling
- **Solution**: Create expect/actual for camera scanning functionality

### Medium Priority - Detail View
**Location**: `ComposeDetailViewScreen.kt`
- **Clipboard operations** - Copy QR text to clipboard
- **Share functionality** - Share QR codes via Intent
- **Storage permissions** - Save QR images to device storage
- **Solution**: Create expect/actual for:
  - `expect fun copyToClipboard(text: String)`
  - `expect fun shareQrCode(text: String, imageData: ByteArray?)`
  - `expect fun requestStoragePermission()`

### Medium Priority - Image Rendering
**Affected**: All screens displaying QR codes
- **Current**: Using `android.graphics.Bitmap` and `BitmapFactory`
- **Solution**: Use Compose Multiplatform's `ImageBitmap`
  ```kotlin
  // Shared code
  expect fun ByteArray.toImageBitmap(): ImageBitmap

  // Android actual
  actual fun ByteArray.toImageBitmap(): ImageBitmap {
      return BitmapFactory.decodeByteArray(this, 0, this.size).asImageBitmap()
  }

  // iOS actual
  actual fun ByteArray.toImageBitmap(): ImageBitmap {
      // Use UIImage from NSData
  }
  ```

### Low Priority - String Resources
**Current**: Using Android's `R.string.*` resources
**Solution**: Use Compose Multiplatform's resources or create expect/actual for strings

### Low Priority - Intent Handling
**Location**: `ComposeDetailViewScreen.kt`, `IntentGenerator.kt`
- **Opening URLs** - Email, phone, SMS, web links
- **Solution**: Create expect/actual for platform-specific URL handling

## 📂 File Structure

```
shared/
├── src/
│   ├── commonMain/kotlin/cut/the/crap/qreverywhere/shared/
│   │   ├── data/
│   │   │   ├── AndroidQrRepository.kt
│   │   │   └── DesktopQrRepository.kt
│   │   ├── di/
│   │   │   └── CommonModule.kt               # Koin DI configuration
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── QrItem.kt                 # Shared data model
│   │   │   ├── repository/
│   │   │   │   └── QrRepository.kt
│   │   │   └── usecase/
│   │   │       ├── QrCodeGenerator.kt
│   │   │       ├── SaveImageToFileUseCase.kt
│   │   │       └── UserPreferences.kt
│   │   ├── presentation/
│   │   │   ├── navigation/
│   │   │   │   ├── AppNavHost.kt             # Shared navigation host
│   │   │   │   └── Screen.kt                 # Navigation destinations
│   │   │   ├── screens/
│   │   │   │   └── HistoryScreen.kt          # Shared History screen
│   │   │   └── viewmodel/
│   │   │       └── MainViewModel.kt          # Shared ViewModel
│   │   └── utils/
│   │       └── Logger.kt
│   ├── androidMain/kotlin/cut/the/crap/qreverywhere/shared/
│   │   ├── data/
│   │   │   └── AndroidQrRepository.kt        # Android Room implementation
│   │   ├── di/
│   │   │   └── AndroidModule.kt              # Android-specific DI
│   │   └── domain/usecase/
│   │       ├── AndroidQrCodeGenerator.kt
│   │       ├── AndroidSaveImageToFileUseCase.kt
│   │       └── AndroidUserPreferences.kt
│   ├── desktopMain/kotlin/
│   │   └── ... (Desktop-specific implementations)
│   └── iosMain/kotlin/
│       └── ... (iOS-specific implementations)
└── build.gradle.kts
```

## 🚀 Next Steps

### Phase 1: Image Handling (Recommended First)
1. Create `expect fun ByteArray.toImageBitmap(): ImageBitmap`
2. Implement `actual` for Android, iOS, Desktop
3. Update HistoryScreen to display QR code images
4. Update DetailViewScreen to show full QR code image

### Phase 2: Platform-Specific APIs
1. Create expect/actual for clipboard operations
2. Create expect/actual for sharing functionality
3. Create expect/actual for file saving
4. Create expect/actual for camera/scanning

### Phase 3: Complete Screen Migration
1. Migrate DetailViewScreen to shared module
2. Migrate CreateTextScreen to shared module
3. Migrate CreateEmailScreen to shared module
4. Create platform-specific scanner implementations

### Phase 4: Testing
1. Test on Android
2. Set up iOS project
3. Test on iOS
4. Set up Desktop project
5. Test on Desktop

## 📝 Build Status

- ✅ **Shared module** builds successfully for all targets (Android, iOS, Desktop)
- ✅ **Android app** builds successfully with shared module
- ⏳ **iOS app** - Not yet set up
- ⏳ **Desktop app** - Not yet set up

## 🔧 Technical Details

### Dependencies
```kotlin
// commonMain
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
implementation(platform("io.insert-koin:koin-bom:4.0.1"))
implementation("io.insert-koin:koin-core")
implementation("io.insert-koin:koin-compose")
implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
implementation(compose.runtime)
implementation(compose.foundation)
implementation(compose.material3)
implementation(compose.ui)

// androidMain
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0") // Android-specific
```

**Note**: `lifecycle-runtime-compose` is Android-specific and must be in `androidMain`, not `commonMain`.

### Gradle Configuration
- **Kotlin**: 2.1.0
- **Compose Multiplatform**: 1.7.1
- **AGP**: 8.10.1
- **Targets**: androidTarget, iosX64, iosArm64, iosSimulatorArm64, jvm("desktop")

## 📚 References

- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Expect and Actual Declarations](https://kotlinlang.org/docs/multiplatform-connect-to-apis.html)
- [Koin for KMP](https://insert-koin.io/docs/reference/koin-mp/kmp)
