import Foundation
import shared

/// Bridge class that connects Kotlin/Native widget callbacks to Swift WidgetDataStore.
/// This class sets up the callback so that QR code updates from the shared module
/// are automatically synced to the widget.
class WidgetBridge {
    static let shared = WidgetBridge()

    private init() {}

    /// Sets up the widget update callback from Kotlin/Native.
    /// Call this during app initialization.
    func setupWidgetCallback() {
        Main_iosKt.onWidgetUpdateCallback = { [weak self] qrData in
            self?.handleWidgetUpdate(qrData: qrData)
        }

        Main_iosKt.onDeepLinkCallback = { action, id in
            Self.handleDeepLink(action: action, id: id?.intValue)
        }
    }

    private func handleWidgetUpdate(qrData: WidgetQrData?) {
        guard let qrData = qrData else {
            WidgetDataStore.shared.clearData()
            return
        }

        // Convert Kotlin ByteArray to Swift Data
        var imageData: Data? = nil
        if let kotlinByteArray = qrData.imageData {
            imageData = kotlinByteArray.toData()
        }

        WidgetDataStore.shared.saveLatestQrCode(
            id: Int(qrData.id),
            text: qrData.text,
            imageData: imageData,
            type: qrData.type
        )
    }

    private static func handleDeepLink(action: String?, id: Int?) {
        switch action {
        case "detail":
            if let id = id {
                NotificationCenter.default.post(
                    name: .openQrDetail,
                    object: nil,
                    userInfo: ["id": id]
                )
            }
        case "create":
            NotificationCenter.default.post(name: .openQrCreate, object: nil)
        default:
            break
        }
    }
}

// MARK: - Kotlin ByteArray to Swift Data Extension

extension KotlinByteArray {
    /// Converts a Kotlin ByteArray to Swift Data.
    func toData() -> Data {
        let size = self.size
        var bytes = [UInt8](repeating: 0, count: Int(size))
        for i in 0..<size {
            bytes[Int(i)] = UInt8(bitPattern: self.get(index: i))
        }
        return Data(bytes)
    }
}
