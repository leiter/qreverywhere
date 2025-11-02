# Drawable/Image Resources: Android vs iOS

## ❌ No, Drawable References Are NOT Int on iOS

### Key Difference

| Platform | Resource Type | Example |
|----------|--------------|---------|
| **Android** | `Int` (compile-time constant) | `R.drawable.icon` → `2130837504` |
| **iOS** | `String` (asset name) | `UIImage(named: "icon")` |
| **Desktop** | `String` (resource path) | `/drawable/icon.png` |

## 📱 How Each Platform Works

### Android
```kotlin
// Drawable resource IDs are Int values generated at compile time
val iconId: Int = R.drawable.ic_launcher  // e.g., 2130903040
val icon2Id: Int = R.drawable.qr_code     // e.g., 2130903041

// Resources are compiled into R.java / R.kt
imageView.setImageResource(iconId)
```

**Why Int?**
- Android uses AAPT (Android Asset Packaging Tool) to generate unique Int IDs
- These IDs are stored in the `R` class at compile time
- Very efficient lookup in the resources table

### iOS
```swift
// Images are loaded by STRING name from asset catalogs
let icon = UIImage(named: "ic_launcher")  // String parameter, returns UIImage?
let qrIcon = UIImage(systemName: "qrcode") // SF Symbols

// NO Int IDs - always string-based lookups
imageView.image = icon
```

**Why String?**
- iOS uses Asset Catalogs (`.xcassets`)
- Images are organized by name, not numeric IDs
- Runtime lookup by string name
- Supports multiple resolutions (@1x, @2x, @3x) automatically

## ✅ Cross-Platform Solution in Your Project

I've created **platform-specific implementations** using `expect/actual`:

### Common Code (shared/commonMain)
```kotlin
// Define platform-agnostic image resources
data class ImageResource(val name: String)

object AppIcons {
    val QrCode = ImageResource("qr_code")
    val Scanner = ImageResource("ic_scanner")
}

// Platform will implement this
@Composable
expect fun ImageResource.toPainter(): Painter
```

### Android Implementation (shared/androidMain)
```kotlin
@Composable
actual fun ImageResource.toPainter(): Painter {
    val context = LocalContext.current

    // Convert string name to Int resource ID using reflection
    val resourceId: Int = context.resources.getIdentifier(
        name,           // "qr_code"
        "drawable",
        context.packageName
    )

    // Use the Int ID with Android's painterResource
    return painterResource(id = resourceId)
}
```

**How it works:**
1. Takes the string name `"qr_code"`
2. Uses reflection to find `R.drawable.qr_code` → gets the Int value
3. Passes the Int to Android's `painterResource()`

### iOS Implementation (shared/iosMain)
```kotlin
@Composable
actual fun ImageResource.toPainter(): Painter {
    return remember(name) {
        // Directly load by string name - NO Int conversion!
        val uiImage = UIImage.imageNamed(name)  // String parameter

        // Convert UIImage to Compose ImageBitmap
        val imageBitmap = uiImage.toComposeImageBitmap()
        BitmapPainter(imageBitmap)
    }
}
```

**How it works:**
1. Takes the string name `"qr_code"`
2. Calls `UIImage.imageNamed()` directly with the string
3. Converts `UIImage` to Compose's `ImageBitmap`

### Desktop Implementation (shared/desktopMain)
```kotlin
@Composable
actual fun ImageResource.toPainter(): Painter {
    return remember(name) {
        // Load from resources directory by path
        val resourcePath = "/drawable/$name.png"  // String path
        val imageBytes = javaClass.getResourceAsStream(resourcePath)?.readBytes()

        // Decode using Skia
        val skiaImage = SkiaImage.makeFromEncoded(imageBytes)
        BitmapPainter(skiaImage.toComposeImageBitmap())
    }
}
```

## 🎯 Usage in Your Code

### Shared Compose UI
```kotlin
@Composable
fun HistoryItem(qrItem: QrItem) {
    Row {
        // Works on Android, iOS, and Desktop!
        Image(
            painter = AppIcons.QrCode.toPainter(),
            contentDescription = "QR Code"
        )

        // Display generated QR code from ByteArray
        qrItem.imageData?.let { imageBytes ->
            Image(
                painter = imageBytes.toImagePainter() ?: return@let,
                contentDescription = "Generated QR"
            )
        }
    }
}
```

### What Happens on Each Platform

**Android:**
```
AppIcons.QrCode.toPainter()
→ ImageResource("qr_code").toPainter()
→ getIdentifier("qr_code", "drawable", ...) → Int: 2130903040
→ painterResource(id = 2130903040)
→ Painter
```

**iOS:**
```
AppIcons.QrCode.toPainter()
→ ImageResource("qr_code").toPainter()
→ UIImage.imageNamed("qr_code")  // No Int involved!
→ Convert UIImage to ImageBitmap
→ BitmapPainter(imageBitmap)
```

**Desktop:**
```
AppIcons.QrCode.toPainter()
→ ImageResource("qr_code").toPainter()
→ Load "/drawable/qr_code.png" from resources
→ Decode with Skia
→ BitmapPainter(imageBitmap)
```

## 📂 File Organization

### Android
```
app/src/main/res/
├── drawable/
│   ├── qr_code.xml         (vector drawable)
│   └── ic_scanner.png
├── drawable-mdpi/
├── drawable-hdpi/
└── drawable-xhdpi/
```

### iOS
```
iosApp/Assets.xcassets/
├── qr_code.imageset/
│   ├── qr_code@1x.png
│   ├── qr_code@2x.png
│   ├── qr_code@3x.png
│   └── Contents.json
└── ic_scanner.imageset/
```

### Desktop
```
shared/src/desktopMain/resources/
└── drawable/
    ├── qr_code.png
    └── ic_scanner.png
```

## 🔑 Key Takeaways

1. **Android uses Int IDs** - Generated at compile time by AAPT/R8
2. **iOS uses String names** - Looked up at runtime from asset catalogs
3. **Desktop uses String paths** - Loaded from resources directory
4. **Compose Multiplatform** - Abstracts this away with `expect/actual`
5. **Your code** - Write once, works everywhere using `ImageResource`

## 🚀 Implementation Files

I've created the following files in your project:

- ✅ `shared/src/commonMain/.../ImageResources.kt` - Common interface
- ✅ `shared/src/androidMain/.../ImageResources.android.kt` - Android (Int-based)
- ✅ `shared/src/iosMain/.../ImageResources.ios.kt` - iOS (String-based)
- ✅ `shared/src/desktopMain/.../ImageResources.desktop.kt` - Desktop (String-based)

## 📖 Example: Displaying a QR Code

```kotlin
@Composable
fun QrCodeDisplay(qrItem: QrItem) {
    Card {
        Column {
            // Static icon (from resources)
            Icon(
                painter = AppIcons.QrCode.toPainter(),
                contentDescription = "QR Icon"
            )

            // Dynamic QR code (generated at runtime)
            qrItem.imageData?.let { bytes ->
                Image(
                    painter = bytes.toImagePainter() ?: return@let,
                    contentDescription = "Generated QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}
```

This works identically on Android (Int), iOS (String), and Desktop (String) because the `expect/actual` pattern handles the platform differences!

## 🎨 Alternative: Compose Resources (Recommended)

Compose Multiplatform also provides a built-in resource system:

```kotlin
// Place images in: shared/src/commonMain/composeResources/drawable/
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyIcon() {
    Image(
        painter = painterResource(Res.drawable.qr_code),
        contentDescription = "QR Code"
    )
}
```

This generates type-safe resource references for all platforms automatically!
