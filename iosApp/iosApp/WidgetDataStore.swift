import Foundation
import WidgetKit

/// Shared data store for passing QR code data between the main app and widget extension.
/// Uses App Group UserDefaults for inter-process communication.
public class WidgetDataStore {
    public static let shared = WidgetDataStore()

    private let suiteName = "group.cut.the.crap.qreverywhere"

    private enum Keys {
        static let qrId = "qrId"
        static let qrText = "qrText"
        static let qrImage = "qrImage"
        static let qrType = "qrType"
        static let qrTimestamp = "qrTimestamp"
    }

    private var defaults: UserDefaults? {
        UserDefaults(suiteName: suiteName)
    }

    private init() {}

    // MARK: - Save Data

    /// Saves the latest QR code data for the widget to display.
    /// - Parameters:
    ///   - id: The QR code database ID
    ///   - text: The QR code content text
    ///   - imageData: PNG data of the generated QR code image
    ///   - type: The QR code type (URL, WiFi, vCard, etc.)
    public func saveLatestQrCode(id: Int, text: String, imageData: Data?, type: String? = nil) {
        guard let defaults = defaults else {
            print("WidgetDataStore: Failed to access App Group UserDefaults")
            return
        }

        defaults.set(id, forKey: Keys.qrId)
        defaults.set(text, forKey: Keys.qrText)
        defaults.set(imageData, forKey: Keys.qrImage)
        defaults.set(type, forKey: Keys.qrType)
        defaults.set(Date(), forKey: Keys.qrTimestamp)

        // Trigger widget refresh
        WidgetCenter.shared.reloadAllTimelines()
    }

    // MARK: - Read Data

    /// Retrieves the latest QR code data for widget display.
    /// - Returns: Tuple with QR id, text, optional image data, and optional type, or nil if no data exists
    public func getLatestQrCode() -> (id: Int, text: String, imageData: Data?, type: String?)? {
        guard let defaults = defaults,
              let text = defaults.string(forKey: Keys.qrText) else {
            return nil
        }

        let id = defaults.integer(forKey: Keys.qrId)
        let imageData = defaults.data(forKey: Keys.qrImage)
        let type = defaults.string(forKey: Keys.qrType)

        return (id, text, imageData, type)
    }

    /// Gets the timestamp of the last saved QR code.
    /// - Returns: The date when the QR code was saved, or nil if no data exists
    public func getLastUpdateTimestamp() -> Date? {
        return defaults?.object(forKey: Keys.qrTimestamp) as? Date
    }

    // MARK: - Clear Data

    /// Clears all widget data.
    public func clearData() {
        guard let defaults = defaults else { return }

        defaults.removeObject(forKey: Keys.qrId)
        defaults.removeObject(forKey: Keys.qrText)
        defaults.removeObject(forKey: Keys.qrImage)
        defaults.removeObject(forKey: Keys.qrType)
        defaults.removeObject(forKey: Keys.qrTimestamp)

        WidgetCenter.shared.reloadAllTimelines()
    }
}
