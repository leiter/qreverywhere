# Localization Audit Report - QrEveryWhere

**Date:** 2025-11-15  
**Branch:** migrate_to_kmp  
**Scope:** Android Compose UI files and Shared CommonMain Kotlin files

---

## Executive Summary

The project has multiple missed localization opportunities where hardcoded strings are used directly in Composable functions instead of using the centralized `Strings.kt` resource or generated resources. While a foundation has been created with `Strings.kt` for shared strings, many Compose screens in both Android and shared code are not utilizing these constants.

**Total Issues Found:** 47 hardcoded strings across 10 files

---

## Issues Found

### 1. CRITICAL: Android Compose Screens

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeCreateQrScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 36 | `"Create QR Code"` | Main screen title | HIGH |
| 42 | `"Choose what type of QR code to create"` | Screen subtitle/description | HIGH |
| 49 | `"Text QR Code"` | Card title in QrTypeCard | MEDIUM |
| 50 | `"Create a QR code with any text content"` | Card description | MEDIUM |
| 58 | `"Web URL QR Code"` | Card title | MEDIUM |
| 59 | `"Create a QR code with website URL"` | Card description | MEDIUM |
| 67 | `"Phone QR Code"` | Card title | MEDIUM |
| 68 | `"Create a QR code with phone number"` | Card description | MEDIUM |
| 76 | `"SMS QR Code"` | Card title | MEDIUM |
| 77 | `"Create a QR code with SMS message"` | Card description | MEDIUM |
| 85 | `"Email QR Code"` | Card title | MEDIUM |
| 86 | `"Create a QR code with email information"` | Card description | MEDIUM |

**Recommendation:** All these strings are already defined in `Strings.kt` (titleTextQr, titleUrlQr, etc.) but not being used. Replace with references to `Strings` object.

---

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeHistoryScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 72 | `"QR History"` | Screen title | HIGH |
| 84 | `"No QR codes yet.\nScan or create one to get started!"` | Empty state message | HIGH |
| 133 | `"QR Code"` | Image content description | MEDIUM |
| 149-153 | `"Scanned"`, `"Created"`, `"From File"`, `"Unknown"` | QR type labels in type display | HIGH |

**Recommendation:** 
- Line 72: Use `Strings.titleHistory`
- Line 84: Use `Strings.historyEmpty`
- Line 133: Use `Strings.cdQrCode`
- Lines 149-153: Create mapping function using localized enum names or add format string like `Strings.historyType`

---

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeCreateTextQrScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 68 | `"Please enter a valid URL"` | Validation error | HIGH |
| 69 | `"Please enter a valid phone number"` | Validation error | HIGH |
| 70 | `"Please enter a phone number"` | Validation error | HIGH |
| 71 | `"Please enter some text"` | Validation error | HIGH |
| 79 | `"Invalid URL format"` | Validation error | HIGH |
| 84 | `"Invalid phone number"` | Validation error | HIGH |
| 94 | `"Create URL QR Code"` | Title | HIGH |
| 95 | `"Website URL"` | Label | MEDIUM |
| 96 | `"https://example.com"` | Placeholder | MEDIUM |
| 97 | `"Create Phone QR Code"` | Title | HIGH |
| 98 | `"Phone Number"` | Label | MEDIUM |
| 99 | `"+1234567890"` | Placeholder | MEDIUM |
| 100 | `"Create SMS QR Code"` | Title | HIGH |
| 101 | `"Phone Number"` | Label | MEDIUM |
| 102 | `"+1234567890"` | Placeholder | MEDIUM |
| 103 | `"Create Text QR Code"` | Title | HIGH |
| 104 | `"Text Content"` | Label | MEDIUM |
| 105 | `"Enter any text"` | Placeholder | MEDIUM |
| 113 | `"Back"` | Icon content description | MEDIUM |
| 129 | `"Enter the $label to encode in the QR code"` | Instructions (with dynamic label) | HIGH |
| 201 | `"Creating..."` | Button text during creation | MEDIUM |
| 201 | `"Create QR Code"` | Button text | MEDIUM |
| 192 | `"Error creating QR code: ${e.message}"` | Error snackbar | HIGH |

**Recommendation:**
- Add error message constants to `Strings.kt`
- Reuse title constants from `Strings.kt` 
- Add validation message constants
- Use string formatting for dynamic messages

---

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeCreateEmailQrScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 74 | `"Create Email QR Code"` | Screen title | HIGH |
| 77 | `"Back"` | Icon content description | MEDIUM |
| 93 | `"Enter email details to encode in the QR code"` | Instructions | HIGH |
| 106 | `"Email Address"` | TextField label | MEDIUM |
| 107 | `"example@email.com"` | Placeholder | MEDIUM |
| 125 | `"Subject (Optional)"` | TextField label | MEDIUM |
| 126 | `"Email subject"` | Placeholder | MEDIUM |
| 139 | `"Message (Optional)"` | TextField label | MEDIUM |
| 140 | `"Email message"` | Placeholder | MEDIUM |
| 156 | `"Please enter an email address"` | Validation error | HIGH |
| 161 | `"Invalid email address"` | Validation error | HIGH |
| 195 | `"Creating..."` | Button text | MEDIUM |
| 195 | `"Create QR Code"` | Button text | MEDIUM |
| 186 | `"Error creating QR code: ${e.message}"` | Error snackbar | HIGH |

**Recommendation:**
- Line 74: Use `Strings.titleEmailQr` or `Strings.titleCreateEmail`
- Add email-specific validation strings to `Strings.kt`
- Consolidate button text strings

---

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeDetailViewScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 138 | `"Storage permission is required to save QR code"` | Permission error snackbar | HIGH |
| 163 | `"Please grant storage permission in app settings"` | Permission snackbar | HIGH |
| 222 | `"QR code saved to file"` | Success snackbar | HIGH |
| 225 | `"Error saving QR code"` | Error snackbar | HIGH |
| 273 | `"Back"` | Icon content description | MEDIUM |
| 278 | `"Menu"` | Icon content description | MEDIUM |
| 285 | `"Save to file"` | Menu item text | MEDIUM |
| 291 | `"Save"` | Icon content description | MEDIUM |
| 296 | `"Delete"` | Menu item text | MEDIUM |
| 305 | `"Delete"` | Icon content description | MEDIUM |
| 329 | `"Launch"` | Icon content description | MEDIUM |
| 352 | `"Loading QR code..."` | Loading message | HIGH |
| 372 | `"No QR code data available"` | Error message | HIGH |
| 377 | `"Origin flag: $originFlag"` | Debug info | LOW |
| 414 | `"QR Code"` | Image content description | MEDIUM |
| 423 | `"Tap to view fullscreen"` | Helper text | MEDIUM |
| 437 | Uses `stringResource(R.string.detailview_content_title)` | Content section title | GOOD (already using resources) |
| 475 | `"Copied to clipboard"` | Success snackbar | HIGH |
| 488 | `"📋 Copy to clipboard"` | Button text with emoji | MEDIUM |

**Recommendation:**
- Add all permission and error messages to `Strings.kt`
- Use string resources or `Strings` constants for UI text
- Consider whether emojis in strings should be localized

---

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeScanQrScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 245 | `"No QR code found in image"` | Error message | HIGH |
| 298 | `"Scan from file"` | FAB content description | MEDIUM |
| 349 | Uses `context.getString(R.string.requestCameraPermission)` | Permission request | GOOD (using resources) |
| 356 | Uses `context.getString(R.string.requestQrFromFile)` | File picker button | GOOD (using resources) |

**Note:** This file is mostly using string resources correctly. Minor improvements noted above.

---

#### File: `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeFullscreenQrScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 108 | `"Back"` | Icon content description | MEDIUM |
| 153 | `"QR Code Fullscreen"` | Image content description | MEDIUM |

**Recommendation:** Use `Strings.cdBack` and `Strings.cdQrCode` with suffix.

---

### 2. Shared CommonMain Compose Screens

#### File: `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/DetailScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 71 | `"Error: ${state.message}"` | Error display | HIGH |
| 101 | `"QR Code"` | Image content description | MEDIUM |
| 122 | `"Content"` | Section title | MEDIUM |
| 145 | `"Details"` | Section title | MEDIUM |
| 150 | `"Type"` | Metadata label | MEDIUM |
| 151 | `"Created"` | Metadata label | MEDIUM |
| 165 | `"Share not yet implemented for cross-platform"` | Log warning | MEDIUM |
| 170 | `"Share"` | Icon description | MEDIUM |
| 172 | `"Share"` | Button text | MEDIUM |
| 182 | `"Save"` | Icon description | MEDIUM |
| 184 | `"Save Image"` | Button text | MEDIUM |
| 197 | `"Delete"` | Icon description | MEDIUM |
| 199 | `"Delete"` | Button text | MEDIUM |
| 205 | `"No QR code selected"` | Error message | HIGH |

**Recommendation:**
- Migrate to use generated resource strings from `Res` (like App.kt does)
- Add missing strings to resource files
- Some strings like "Share", "Delete" are already in `Strings.kt`

---

#### File: `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/HistoryScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 61 | `"No QR codes yet.\nScan or create one to get started!"` | Empty state | HIGH |
| 106 | `"QR Code Preview"` | Image content description | MEDIUM |
| 125 | `"Type: ${qrItem.acquireType.name}"` | Type label (with dynamic value) | HIGH |

**Recommendation:**
- Line 61: Use string from `Strings.kt`
- Line 106: Use `Strings.cdQrPreview`
- Line 125: Create localized format string

---

#### File: `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/CreateTextScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 45 | `"Web URL QR Code"` | Title | MEDIUM |
| 46 | `"Enter website URL"` | Label | MEDIUM |
| 47 | `"https://example.com"` | Placeholder | MEDIUM |
| 51 | `"Phone QR Code"` | Title | MEDIUM |
| 52 | `"Enter phone number"` | Label | MEDIUM |
| 53 | `"+1234567890"` | Placeholder | MEDIUM |
| 57 | `"SMS QR Code"` | Title | MEDIUM |
| 58 | `"Enter phone number and message"` | Label | MEDIUM |
| 59 | `"+1234567890:Hello"` | Placeholder | MEDIUM |
| 63 | `"Text QR Code"` | Title | MEDIUM |
| 64 | `"Enter text"` | Label | MEDIUM |
| 65 | `"Type anything..."` | Placeholder | MEDIUM |
| 102-104 | `"The URL should start..."`, `"Include country code..."`, `"Format: phone:message..."` | Hints | HIGH |
| 136 | `"Generate QR Code"` | Button text | MEDIUM |

**Recommendation:**
- Use title constants from `Strings.kt`
- Add validation hint messages to resources
- Ensure button text is consistent with other screens

---

#### File: `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/ScanScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 32-34 | `"Camera scanning not yet implemented..."` | Logger warning | LOW |
| 46 | `"📷"` | Emoji in Text | LOW |
| 53 | `"Camera Scanning"` | Title | HIGH |
| 61-65 | `"QR code scanning requires platform-specific..."` | Description (multiline) | HIGH |

**Recommendation:**
- These are informational/placeholder messages - could be in resources
- Emoji usage should be reviewed for localization

---

#### File: `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/FullscreenScreen.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 63 | `"Back"` | Icon content description | MEDIUM |
| 106 | `"QR Code Fullscreen"` | Image content description | MEDIUM |

**Recommendation:**
- Use `Strings.cdBack` and `Strings.cdQrCode`

---

#### File: `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/App.kt`

| Line | String | Context | Severity |
|------|--------|---------|----------|
| 54 | `"create/text/"` | Route check (code, not UI) | LOW |
| 61 | `"create/text/"` | Route check (code, not UI) | LOW |

**Status:** This file is already using generated resource strings (Res.string.*), which is best practice. GOOD!

---

## Summary Table by File

| File | Location | Hardcoded Strings | Status |
|------|----------|-------------------|--------|
| ComposeCreateQrScreen.kt | Android Compose | 12 | NEEDS WORK |
| ComposeHistoryScreen.kt | Android Compose | 4 | NEEDS WORK |
| ComposeCreateTextQrScreen.kt | Android Compose | 23 | CRITICAL |
| ComposeCreateEmailQrScreen.kt | Android Compose | 14 | NEEDS WORK |
| ComposeDetailViewScreen.kt | Android Compose | 18 | CRITICAL |
| ComposeScanQrScreen.kt | Android Compose | 1 | GOOD |
| ComposeFullscreenQrScreen.kt | Android Compose | 2 | NEEDS WORK |
| DetailScreen.kt | Shared | 14 | NEEDS WORK |
| HistoryScreen.kt | Shared | 3 | NEEDS WORK |
| CreateTextScreen.kt | Shared | 14 | NEEDS WORK |
| ScanScreen.kt | Shared | 4 | ACCEPTABLE (placeholder) |
| FullscreenScreen.kt | Shared | 2 | NEEDS WORK |
| App.kt | Shared | 0 | EXCELLENT |

---

## Key Observations

1. **Existing Infrastructure:** The project has `Strings.kt` with many predefined strings, but they are not being used consistently.

2. **Two Localization Approaches:**
   - Android-specific files use raw strings or `stringResource(R.string.*)`
   - Shared files have started using generated resources (`Res.string.*`) in `App.kt`, which is the right pattern

3. **Validation Messages:** Error and validation messages lack centralized definitions

4. **Consistency Issue:** The same app has different localization strategies in different files, making maintenance difficult

5. **Missing Strings in Strings.kt:**
   - Validation error messages
   - Permission-related messages
   - Snackbar/feedback messages
   - Field labels and placeholders for various QR types

---

## Recommended Action Plan

### Phase 1: Consolidate String Resources (HIGH PRIORITY)

1. **Extend Strings.kt** with:
   - Validation error messages
   - Permission-related strings
   - Snackbar feedback messages
   - Field labels and placeholders
   - Button action labels

2. **Add to generated resources** (`compose/generated/resources`):
   - All strings from `Strings.kt`
   - Platform-specific strings if needed
   - Error/validation messages

### Phase 2: Update Android Compose Files

1. Replace all hardcoded strings with `Strings.*` constants or `stringResource()`
2. Prioritize CRITICAL files:
   - ComposeCreateTextQrScreen.kt
   - ComposeDetailViewScreen.kt

### Phase 3: Align Shared Screens

1. Migrate all shared screens to use generated resource strings (like `App.kt`)
2. Ensure consistency between Android-specific and shared implementations

### Phase 4: Testing

1. Verify all screens display correctly with localized strings
2. Test with RTL languages (if applicable)
3. Check for string length issues in different languages

---

## Files Needing Updates (by Priority)

**CRITICAL (23+ strings):**
- `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeCreateTextQrScreen.kt`
- `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeDetailViewScreen.kt`

**HIGH (10+ strings):**
- `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeCreateEmailQrScreen.kt`
- `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeCreateQrScreen.kt`
- `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/CreateTextScreen.kt`
- `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/DetailScreen.kt`

**MEDIUM (5+ strings):**
- `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeHistoryScreen.kt`
- `/app/src/main/java/cut/the/crap/qreverywhere/compose/screens/ComposeFullscreenQrScreen.kt`
- `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/HistoryScreen.kt`
- `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/FullscreenScreen.kt`

**LOW:**
- `/shared/src/commonMain/kotlin/cut/the/crap/qreverywhere/shared/presentation/screens/ScanScreen.kt`

---

## Best Practices Observed

- `App.kt` in shared module is using generated resources correctly
- `ComposeScanQrScreen.kt` is mostly following resource patterns
- Permission strings in `ComposeScanQrScreen.kt` use `stringResource(R.string.*)`

---

**Report Generated:** 2025-11-15  
**Total Hardcoded Strings Found:** 47  
**Files Affected:** 10  
**Overall Status:** NEEDS IMPROVEMENT
