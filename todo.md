# TODO — App Store first release

Everything else for the submission is done and committed (`3ff2b57`): localized metadata for all
22 App Store locales, categories, review information, export-compliance key, and the answer sheet
at `iosApp/appstore/app_store_connect_answers.md`.

What remains is below.

---

## 1. Screenshots (main open item)

### Why it's blocked

Automated capture from this machine failed because:

- macOS denies Terminal the right to send Apple Events to the Simulator (error `-1743`), so taps
  can't be synthesized.
- The `qreverywhere://create` deep link works, but iOS shows an "Open in QR Everywhere?"
  confirmation dialog that itself needs a tap.
- There is **no UI-test target** in `iosApp.xcodeproj` — `iosApp/iosAppUITests/ScreenshotTests.swift`
  is orphaned and never runs.
- maestro is not installed, although flows already exist in `iosApp/screenshots/*.yaml`.

### Permission setup (to unblock automated capture)

The host app that needs the grants is **Terminal.app** (`/System/Applications/Utilities/Terminal.app`),
not Xcode and not Simulator. Two separate permissions are required:

| Permission | Why | Where |
|---|---|---|
| **Automation** → Terminal → System Events | Lets Terminal send Apple Events at all (the `-1743` failure) | Privacy & Security → Automation |
| **Accessibility** → Terminal | Lets System Events click at coordinates in the Simulator window | Privacy & Security → Accessibility |

> Accessibility is a broad grant: it lets Terminal control the whole machine, not just the
> Simulator. Reasonable for a screenshot run — consider revoking it afterwards.

**Step 1 — Accessibility.** Open the pane and enable Terminal (add via `+` → Applications →
Utilities → Terminal if it isn't listed):

```sh
open "x-apple.systempreferences:com.apple.preference.security?Privacy_Accessibility"
```

**Step 2 — Automation.** The `-1743` means the prompt was already dismissed or denied, so it will
not reappear on its own. Clear the old decision:

```sh
tccutil reset AppleEvents com.apple.Terminal
```

This clears **all** of Terminal's automation grants, so other scripts driven from Terminal will
re-prompt once.

**Step 3 — Restart Terminal.** Both grants only apply to newly launched processes. Quit Terminal
fully (⌘Q) and reopen.

**Step 4 — Verify.** A prompt should appear the first time; click OK.

```sh
osascript -e 'tell application "System Events" to get name of every process whose name is "Simulator"'
```

Another `-1743` means the grant didn't stick.

### What has to be captured

App Store Connect requires iPhone **6.9"** at minimum, plus iPad **13"** if the app ships for iPad.

| Device | Simulator | Required pixels | Status |
|---|---|---|---|
| iPhone 6.9" | iPhone 17 Pro Max | 1320 × 2868 | 5 present (`6.9_*`) — see content problems below |
| iPad 13" | iPad Pro 13-inch (M5) | 2064 × 2752 portrait **or** 2752 × 2064 landscape | 4 present (`13_*`, landscape, added in `ebc99d7`) — `02_create` missing |

Both slots are now filled well enough to submit: App Store Connect requires at least one screenshot
per slot and allows up to ten.

Five screens, matching the existing filenames:

| | Screen | iPhone | iPad |
|---|---|---|---|
| `01_scan` | scan screen | yes | yes |
| `02_create` | create hub | yes | **missing** |
| `03_wifi` | WiFi creation | yes | yes |
| `04_history` | history list | yes | yes |
| `05_detail` | QR detail | yes | yes |

### Naming and location

`deliver` reads `iosApp/fastlane/screenshots/<locale>/`, and infers the device from the image
**resolution**, not the filename. The prefix is only for human sorting — keep it honest:

```
iosApp/fastlane/screenshots/en-US/6.9_01_scan.png     (1320 × 2868, portrait)
iosApp/fastlane/screenshots/en-US/13_01_scan.png      (2752 × 2064, landscape)
```

Mixing orientation between the iPhone and iPad sets is fine — they are separate slots.

The previous set was misnamed `6.7_*` while actually being 6.9" pixels; it has been renamed. A
second junk set named `iPhone 67_*` was deleted.

English screenshots are used for all 22 storefronts — that's allowed and common. Only `en-US`
needs to be populated.

### Manual capture recipe

```sh
# Boot and install
xcrun simctl boot "iPhone 17 Pro Max"
xcrun simctl install booted "/tmp/qrdd/Build/Products/Debug-iphonesimulator/QR Everywhere.app"

# Clean status bar: 9:41, full bars, charged battery
xcrun simctl status_bar booted override \
  --time "9:41" --batteryState charged --batteryLevel 100 --cellularBars 4 --wifiBars 3

xcrun simctl launch booted cut.the.crap.qreverywhere
# navigate by hand, then per screen:
xcrun simctl io booted screenshot iosApp/fastlane/screenshots/en-US/6.9_01_scan.png
```

Rebuild if needed:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max' \
  -derivedDataPath /tmp/qrdd build
```

### Remaining content problems (quality, not compliance)

None of these will cause a rejection — App Review checks sizes and accuracy, not persuasiveness.
They cost conversions, not approval.

| Screenshot | Problem |
|---|---|
| `6.9_01_scan`, `13_01_scan` | "Camera not available" placeholder — see below |
| `6.9_03_wifi`, `13_03_wifi` | Completely empty form: placeholder text in every field, nothing filled in |
| `6.9_04_history` | Only two entries, two-thirds white space, and one is `https://appstoreconnect.apple.com/login` — dev test data on a public store page |
| `13_04_history` | A single entry on an otherwise empty screen |
| `13_02_create` | Does not exist |

The cheap fix: seed a handful of varied history entries and fill in the WiFi form with a real SSID
and password, then recapture those screens on both devices in one pass.

### The camera problem — needs a physical device

On the Simulator the scan screen shows **"Camera not available — Running on simulator"**. Both
`6.9_01_scan.png` and `13_01_scan.png` have exactly this text in them. As the lead screenshot it is
a poor first impression and invites reviewer questions.

No permission grant fixes this — the Simulator has no camera. A real camera view in `01_scan`
requires capturing on a physical device (⌘⇧4 via QuickTime, or Xcode → Devices, or
Volume-Up + Side button on the device itself and then AirDrop the file).

Note a device screenshot is 1320 × 2868 only on a 6.9" iPhone (15/16/17 Pro Max or Plus). Smaller
iPhones produce sizes App Store Connect will reject for this slot.

---

## 2. App Review phone number

Not invented — it must be real. Either enter it in App Store Connect directly, or create
`iosApp/fastlane/metadata/review_information/phone_number.txt` so `deliver` uploads it.

## 3. Version number — settled at 1.0

Version is **1.0**, build **10**, set consistently in both places:

- `iosApp/iosApp/Info.plist`: `CFBundleShortVersionString = 1.0`, `CFBundleVersion = 10`
- `iosApp.xcodeproj/project.pbxproj`: `MARKETING_VERSION = 1.0`, `CURRENT_PROJECT_VERSION = 10`

**Still unresolved (low priority):** because the target uses `INFOPLIST_FILE`, only the Info.plist
values actually ship — the build settings are inert. They now agree, so nothing is wrong today, but
fastlane's `increment_build_number` edits the pbxproj and therefore still has **no effect on the
shipped binary**. Bump `CFBundleVersion` in the Info.plist by hand for future builds, or make the
plist reference `$(MARKETING_VERSION)` / `$(CURRENT_PROJECT_VERSION)` so the build settings become
the single source of truth and fastlane starts working.

## 4. Copyright name

`iosApp/fastlane/metadata/copyright.txt` currently reads `2026 Marco Leiter`. It must match the
legal entity name on the developer account.

---

## Upload

Once the above is settled:

```sh
cd iosApp && fastlane deliver_metadata   # metadata + screenshots, no binary
```

`Deliverfile` already has `submit_for_review(false)`, so nothing is submitted automatically.
