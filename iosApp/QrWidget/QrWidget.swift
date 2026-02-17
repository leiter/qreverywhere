import WidgetKit
import SwiftUI
import UIKit

// MARK: - Widget Entry

struct QrWidgetEntry: TimelineEntry {
    let date: Date
    let qrId: Int?
    let qrText: String?
    let qrImage: UIImage?
    let qrType: String?

    static var placeholder: QrWidgetEntry {
        QrWidgetEntry(
            date: Date(),
            qrId: nil,
            qrText: "QR Everywhere",
            qrImage: nil,
            qrType: nil
        )
    }
}

// MARK: - Timeline Provider

struct QrWidgetProvider: TimelineProvider {
    private let dataStore = WidgetDataStore.shared

    func placeholder(in context: Context) -> QrWidgetEntry {
        .placeholder
    }

    func getSnapshot(in context: Context, completion: @escaping (QrWidgetEntry) -> Void) {
        let entry = createEntry()
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<QrWidgetEntry>) -> Void) {
        let entry = createEntry()

        // Refresh every 24 hours
        let nextUpdate = Calendar.current.date(byAdding: .hour, value: 24, to: Date()) ?? Date()
        let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))

        completion(timeline)
    }

    private func createEntry() -> QrWidgetEntry {
        guard let qrData = dataStore.getLatestQrCode() else {
            return .placeholder
        }

        var image: UIImage? = nil
        if let imageData = qrData.imageData {
            image = UIImage(data: imageData)
        }

        return QrWidgetEntry(
            date: Date(),
            qrId: qrData.id,
            qrText: qrData.text,
            qrImage: image,
            qrType: qrData.type
        )
    }
}

// MARK: - Widget View

struct QrWidgetEntryView: View {
    var entry: QrWidgetProvider.Entry
    @Environment(\.widgetFamily) var family

    var body: some View {
        if let image = entry.qrImage {
            // Show QR code
            qrCodeView(image: image)
        } else {
            // Empty state
            emptyStateView
        }
    }

    private func qrCodeView(image: UIImage) -> some View {
        GeometryReader { geometry in
            VStack(spacing: 4) {
                // QR code image
                Image(uiImage: image)
                    .resizable()
                    .interpolation(.none)
                    .scaledToFit()
                    .frame(maxWidth: imageSize(for: geometry.size), maxHeight: imageSize(for: geometry.size))
                    .background(Color.white)
                    .cornerRadius(8)

                // Label (truncated)
                if let text = entry.qrText {
                    Text(truncatedText(text))
                        .font(.caption2)
                        .lineLimit(1)
                        .foregroundColor(.primary)
                }

                // Type badge (only for medium widget)
                if family == .systemMedium, let type = entry.qrType {
                    Text(type)
                        .font(.caption2)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.blue.opacity(0.2))
                        .foregroundColor(.blue)
                        .cornerRadius(4)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(8)
        }
        .widgetURL(deepLinkURL)
    }

    private var emptyStateView: some View {
        VStack(spacing: 8) {
            Image(systemName: "qrcode.viewfinder")
                .font(.system(size: 40))
                .foregroundColor(.secondary)

            Text("No QR code saved")
                .font(.caption)
                .foregroundColor(.secondary)

            Text("Tap to create")
                .font(.caption2)
                .foregroundColor(.blue)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .widgetURL(URL(string: "qreverywhere://create"))
    }

    private func imageSize(for containerSize: CGSize) -> CGFloat {
        switch family {
        case .systemSmall:
            return min(containerSize.width, containerSize.height) - 40
        case .systemMedium:
            return min(containerSize.width / 2, containerSize.height) - 40
        default:
            return 120
        }
    }

    private func truncatedText(_ text: String) -> String {
        let maxLength = family == .systemSmall ? 20 : 30
        if text.count > maxLength {
            return String(text.prefix(maxLength - 3)) + "..."
        }
        return text
    }

    private var deepLinkURL: URL? {
        if let id = entry.qrId {
            return URL(string: "qreverywhere://detail/\(id)")
        }
        return URL(string: "qreverywhere://create")
    }
}

// MARK: - Widget Configuration

struct QrWidget: Widget {
    let kind: String = "QrWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: QrWidgetProvider()) { entry in
            if #available(iOS 17.0, *) {
                QrWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                QrWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName("Recent QR Code")
        .description("Shows your most recent QR code for quick access.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

// MARK: - Preview

#if DEBUG
struct QrWidget_Previews: PreviewProvider {
    static var previews: some View {
        QrWidgetEntryView(entry: .placeholder)
            .previewContext(WidgetPreviewContext(family: .systemSmall))

        QrWidgetEntryView(entry: QrWidgetEntry(
            date: Date(),
            qrId: 1,
            qrText: "https://example.com/very-long-url-that-should-be-truncated",
            qrImage: nil,
            qrType: "URL"
        ))
        .previewContext(WidgetPreviewContext(family: .systemMedium))
    }
}
#endif
