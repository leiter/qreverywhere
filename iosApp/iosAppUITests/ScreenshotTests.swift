import XCTest

final class ScreenshotTests: XCTestCase {

    let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = true
        app.launch()
    }

    func testCaptureScreenshots() throws {
        // Wait for app to load
        sleep(2)

        // Screenshot 1: Scan screen (home)
        let scanScreenshot = XCUIScreen.main.screenshot()
        let scanAttachment = XCTAttachment(screenshot: scanScreenshot)
        scanAttachment.name = "01_scan_screen"
        scanAttachment.lifetime = .keepAlways
        add(scanAttachment)

        // Navigate to Create tab
        let createTab = app.buttons["Create"]
        if createTab.waitForExistence(timeout: 5) {
            createTab.tap()
            sleep(1)

            // Screenshot 2: Create screen
            let createScreenshot = XCUIScreen.main.screenshot()
            let createAttachment = XCTAttachment(screenshot: createScreenshot)
            createAttachment.name = "02_create_screen"
            createAttachment.lifetime = .keepAlways
            add(createAttachment)

            // Try to tap on Text/URL option
            let textOption = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'Text'")).firstMatch
            if textOption.waitForExistence(timeout: 3) {
                textOption.tap()
                sleep(1)

                // Screenshot 3: Text creation screen
                let textScreenshot = XCUIScreen.main.screenshot()
                let textAttachment = XCTAttachment(screenshot: textScreenshot)
                textAttachment.name = "03_create_text_screen"
                textAttachment.lifetime = .keepAlways
                add(textAttachment)

                // Go back
                let backButton = app.navigationBars.buttons.firstMatch
                if backButton.exists {
                    backButton.tap()
                    sleep(1)
                }
            }

            // Try to tap on WiFi option
            let wifiOption = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'WiFi'")).firstMatch
            if wifiOption.waitForExistence(timeout: 3) {
                wifiOption.tap()
                sleep(1)

                // Screenshot 4: WiFi creation screen
                let wifiScreenshot = XCUIScreen.main.screenshot()
                let wifiAttachment = XCTAttachment(screenshot: wifiScreenshot)
                wifiAttachment.name = "04_create_wifi_screen"
                wifiAttachment.lifetime = .keepAlways
                add(wifiAttachment)

                // Go back
                let backButton = app.navigationBars.buttons.firstMatch
                if backButton.exists {
                    backButton.tap()
                    sleep(1)
                }
            }
        }

        // Navigate to History tab
        let historyTab = app.buttons["History"]
        if historyTab.waitForExistence(timeout: 5) {
            historyTab.tap()
            sleep(1)

            // Screenshot 5: History screen
            let historyScreenshot = XCUIScreen.main.screenshot()
            let historyAttachment = XCTAttachment(screenshot: historyScreenshot)
            historyAttachment.name = "05_history_screen"
            historyAttachment.lifetime = .keepAlways
            add(historyAttachment)
        }
    }
}
