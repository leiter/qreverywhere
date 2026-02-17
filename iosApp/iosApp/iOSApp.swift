import UIKit
import shared
// Firebase imports - uncomment after adding Firebase SDK via SPM
// import FirebaseCore
// import FirebaseCrashlytics

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Initialize Firebase before other SDKs
        // Uncomment after adding GoogleService-Info.plist and Firebase SDK:
        // FirebaseApp.configure()

        // Set up widget bridge for Kotlin/Native callbacks
        WidgetBridge.shared.setupWidgetCallback()

        window = UIWindow(frame: UIScreen.main.bounds)
        // Main_iosKt is the generated class name for Main.ios.kt in Kotlin/Native
        let mainViewController = Main_iosKt.MainViewController()
        window?.rootViewController = mainViewController
        window?.makeKeyAndVisible()
        return true
    }

    // MARK: - Deep Link Handling

    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return handleDeepLink(url)
    }

    private func handleDeepLink(_ url: URL) -> Bool {
        // URL format: qreverywhere://detail/{id} or qreverywhere://create
        guard url.scheme == "qreverywhere" else { return false }

        let host = url.host
        let pathComponents = url.pathComponents.filter { $0 != "/" }

        switch host {
        case "detail":
            if let idString = pathComponents.first, let id = Int(idString) {
                // Navigate to detail screen with QR id
                // Note: Navigation is handled by Compose, we'll need to communicate via shared module
                NotificationCenter.default.post(name: .openQrDetail, object: nil, userInfo: ["id": id])
            }
        case "create":
            // Navigate to create screen
            NotificationCenter.default.post(name: .openQrCreate, object: nil)
        default:
            return false
        }

        return true
    }
}

// MARK: - Notification Names for Deep Links

extension Notification.Name {
    static let openQrDetail = Notification.Name("openQrDetail")
    static let openQrCreate = Notification.Name("openQrCreate")
}
