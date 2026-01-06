# Compose Multiplatform - Full Screen UI Guide

## ✅ Yes, You Can Use the Whole Screen on iPhone!

Compose Multiplatform fully supports iOS and you can use Compose to handle the **entire screen** including:
- ✅ Top App Bar
- ✅ Bottom Navigation Bar
- ✅ Safe Areas (notch, home indicator)
- ✅ Status Bar
- ✅ Navigation between screens

This is one of the main advantages of Compose Multiplatform - **100% shared UI code** across iOS, Android, and Desktop.

## 📱 How iOS Safe Areas Work

The `Scaffold` component in Compose Multiplatform **automatically handles iOS safe areas**:

```kotlin
@Composable
fun App(viewModel: MainViewModel) {
    Scaffold(
        topBar = { TopAppBar(...) },
        bottomBar = { NavigationBar(...) }
    ) { innerPadding ->
        // Content automatically respects safe areas!
        AppNavHost(
            modifier = Modifier.padding(innerPadding) // ✨ This handles notch & home indicator
        )
    }
}
```

### What Gets Handled Automatically:
- **iPhone notch** (Dynamic Island on newer models)
- **Home indicator** at the bottom
- **Status bar** at the top
- **Rounded corners**
- **Landscape orientation** safe areas

## 🏗️ Architecture

### File Structure
```
shared/src/commonMain/kotlin/
└── presentation/
    ├── App.kt                    # Root composable with Scaffold
    ├── navigation/
    │   ├── Screen.kt            # Navigation destinations
    │   └── AppNavHost.kt        # Navigation host
    ├── screens/
    │   └── HistoryScreen.kt     # Example screen
    └── viewmodel/
        └── MainViewModel.kt      # Shared ViewModel
```

### Root Composable - `App.kt`

This is your entry point that will be called from iOS, Android, and Desktop:

```kotlin
@Composable
fun App(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("QR Everywhere") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                // Bottom navigation items
                NavigationBarItem(
                    selected = true,
                    onClick = { /* navigate */ },
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("History") }
                )
                // ... more items
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
```

## 🎨 Material 3 Components Work on iOS

All Material 3 components render natively using Skia on iOS:

### Top App Bar
```kotlin
TopAppBar(
    title = { Text("Screen Title") },
    navigationIcon = {
        IconButton(onClick = { /* back */ }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
        }
    },
    actions = {
        IconButton(onClick = { /* action */ }) {
            Icon(Icons.Default.MoreVert, "More")
        }
    }
)
```

### Bottom Navigation
```kotlin
NavigationBar {
    bottomNavItems.forEach { item ->
        NavigationBarItem(
            selected = currentRoute == item.route,
            onClick = { navController.navigate(item.route) },
            icon = { Icon(item.icon, item.label) },
            label = { Text(item.label) }
        )
    }
}
```

### Floating Action Button
```kotlin
ExtendedFloatingActionButton(
    onClick = { /* action */ },
    icon = { Icon(Icons.Default.Add, "Add") },
    text = { Text("Create QR") }
)
```

## 🧭 Navigation

Navigation Compose for Multiplatform works identically across platforms:

```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.History.route,
        modifier = modifier
    ) {
        composable(Screen.History.route) {
            HistoryScreen(viewModel)
        }

        composable(Screen.Detail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            DetailScreen(viewModel, itemId)
        }
    }
}
```

## 📐 Screen Layout Best Practices

### ✅ DO: Let Scaffold handle safe areas
```kotlin
@Composable
fun MyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Only content padding
    ) {
        // Content here
    }
}
```

### ❌ DON'T: Add extra padding for safe areas
```kotlin
@Composable
fun MyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 44.dp) // ❌ Don't do this!
    ) {
        // Scaffold already handles safe areas
    }
}
```

## 🍎 iOS-Specific Considerations

### Status Bar Style
The status bar style (light/dark) is controlled by your Material theme:

```kotlin
MaterialTheme(
    colorScheme = lightColorScheme() // Light status bar icons
    // OR
    colorScheme = darkColorScheme()  // Dark status bar icons
) {
    App(viewModel)
}
```

### Home Indicator
The home indicator automatically gets the right color based on your `NavigationBar` background. No manual configuration needed!

### Dynamic Island
On iPhone 14 Pro and newer, the Dynamic Island is automatically avoided by `Scaffold`'s top padding.

### Landscape Orientation
Safe areas work in both portrait and landscape:

```kotlin
Scaffold(
    topBar = { TopAppBar(...) },
    bottomBar = { NavigationBar(...) }
) { innerPadding ->
    // innerPadding automatically adjusts for:
    // - Portrait: notch + home indicator
    // - Landscape: side safe areas + rounded corners
}
```

## 🎯 How to Integrate in iOS Project

### 1. Create Kotlin Framework (already configured)
Your `shared/build.gradle.kts` already exports the framework:

```kotlin
iosTarget.binaries.framework {
    baseName = "shared"
    isStatic = true
}
```

### 2. Call from iOS (SwiftUI)

```swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea() // Let Compose handle safe areas
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Initialize Koin DI
        KoinKt.doInitKoin()

        // Get ViewModel from Koin
        let viewModel = KoinKt.getMainViewModel()

        // Create Compose UIViewController
        return MainViewControllerKt.MainViewController(
            viewModel: viewModel
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### 3. Create iOS Entry Point in Shared Module

Add this to `shared/src/iosMain/kotlin/`:

```kotlin
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(viewModel: MainViewModel): UIViewController {
    return ComposeUIViewController {
        App(viewModel = viewModel)
    }
}
```

## 🎨 Theming

Your Material theme works across all platforms:

```kotlin
@Composable
fun App(viewModel: MainViewModel) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            primaryContainer = Color(0xFF3700B3),
            // ... other colors
        )
    ) {
        Scaffold { ... }
    }
}
```

### Platform-Specific Themes (Optional)

If you want different themes per platform:

```kotlin
// shared/src/commonMain/kotlin/
expect fun getColorScheme(): ColorScheme

// shared/src/iosMain/kotlin/
actual fun getColorScheme() = lightColorScheme(
    // iOS-specific colors
)

// shared/src/androidMain/kotlin/
actual fun getColorScheme() = lightColorScheme(
    // Android-specific colors (can use Material You)
)
```

## 🚀 Performance on iOS

Compose Multiplatform on iOS uses **Skia** for rendering:
- ✅ 60 FPS smooth animations
- ✅ Native-like performance
- ✅ Lazy lists (LazyColumn) with recycling
- ✅ Hardware acceleration

## 📊 Comparison

| Feature | Native iOS | Compose Multiplatform |
|---------|-----------|---------------------|
| Safe Area Handling | Manual EdgeInsets | Automatic with Scaffold |
| Navigation | UINavigationController | NavHost (same as Android) |
| Bottom Bar | UITabBarController | NavigationBar |
| Code Sharing | 0% | 100% |
| Performance | Native | Native-like (Skia) |
| Development Speed | Slower (2 codebases) | Faster (1 codebase) |

## ✅ Current Implementation

Your project now has:
- ✅ **App.kt** - Full-screen Compose UI with Scaffold
- ✅ **Top App Bar** - Material 3 TopAppBar
- ✅ **Bottom Navigation** - 3 tabs (History, Scan, Create)
- ✅ **Navigation** - NavHost with screen routes
- ✅ **HistoryScreen** - Example screen implementation
- ✅ **Safe Area Handling** - Automatic via Scaffold padding

## 📝 Next Steps

1. **iOS Project Setup**: Create iOS app project in Xcode
2. **Link Framework**: Add the shared framework to iOS project
3. **Call from Swift**: Use `ComposeUIViewController` as shown above
4. **Test on Device**: Run on iPhone to see safe areas in action
5. **Add More Screens**: Implement Detail, Scan, Create screens
6. **Platform-Specific Features**: Use expect/actual for camera, etc.

## 🔗 Resources

- [Compose Multiplatform iOS](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Compose for iOS Tutorial](https://github.com/JetBrains/compose-multiplatform-ios-android-template)
- [Material 3 Components](https://m3.material.io/components)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
