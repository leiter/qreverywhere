#!/usr/bin/env bash
#
# Captures App Store screenshots for every storefront locale, unattended.
#
# The app is launched once per screen with `-screenshotMode YES`, which seeds
# demo content and jumps straight to the requested screen. Nothing is tapped,
# so this needs no UI-test target, no Accessibility grant and no Apple Events
# permission — the three things that blocked earlier attempts.
#
# Usage:
#   iosApp/scripts/capture_screenshots.sh                 # all locales
#   iosApp/scripts/capture_screenshots.sh en-US de-DE     # just these
#   SKIP_BUILD=1 iosApp/scripts/capture_screenshots.sh    # reuse last build
#
set -euo pipefail

BUNDLE_ID="cut.the.crap.qreverywhere"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IOS_DIR="$REPO_ROOT/iosApp"
OUT_ROOT="$IOS_DIR/fastlane/screenshots"
DERIVED="${DERIVED_DATA:-$IOS_DIR/build/screenshots-dd}"
APP_PATH="$DERIVED/Build/Products/Debug-iphonesimulator/QR Everywhere.app"

# Seconds to wait after launch before grabbing the frame. Covers app start,
# the seeding coroutine and the navigation animation.
SETTLE="${SETTLE:-6}"

# Storefront locale -> value for -AppleLanguages.
#
# Right-to-left storefronts (ar-SA, he) are deliberately absent: the app does
# not mirror its layout yet, so a capture would ship a left-to-right UI to RTL
# users. See todo.md before adding them back.
LOCALES=(
  "en-US:en"      "de-DE:de"      "es-ES:es"      "fr-FR:fr"
  "it:it"         "nl-NL:nl"      "pl:pl"         "pt-BR:pt"
  "ru:ru"         "tr:tr"         "uk:uk"         "id:id"
  "ms:ms"         "vi:vi"         "th:th"         "hi:hi"
  "ja:ja"         "ko:ko"         "zh-Hans:zh-Hans" "zh-Hant:zh-Hant"
)

# Device slot -> simulator name. Both slots are required for a universal app.
DEVICES=( "6.9:iPhone 17 Pro Max" "13:iPad Pro 13-inch (M5)" )

# Screens to capture, in listing order.
SCREENS=( "01_scan:scan" "02_create:create" "03_wifi:wifi" "04_history:history" "05_detail:detail" )

log() { printf '\033[1;32m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m!!!\033[0m %s\n' "$*" >&2; }

# Restrict to locales named on the command line, if any.
if [[ $# -gt 0 ]]; then
  filtered=()
  for want in "$@"; do
    for entry in "${LOCALES[@]}"; do
      [[ "${entry%%:*}" == "$want" ]] && filtered+=("$entry")
    done
  done
  [[ ${#filtered[@]} -eq 0 ]] && { warn "no known locale among: $*"; exit 1; }
  LOCALES=("${filtered[@]}")
fi

boot_sim() {
  local name="$1" udid
  udid=$(xcrun simctl list devices available \
    | grep -F "$name (" | head -1 | sed -E 's/.*\(([0-9A-F-]{36})\).*/\1/')
  [[ -z "$udid" ]] && { warn "simulator not found: $name"; return 1; }
  xcrun simctl boot "$udid" >/dev/null 2>&1 || true
  xcrun simctl bootstatus "$udid" -b >/dev/null 2>&1 || true
  # Marketing-standard status bar: 9:41, full signal, charged.
  xcrun simctl status_bar "$udid" override \
    --time "9:41" --batteryState charged --batteryLevel 100 \
    --cellularBars 4 --wifiBars 3 >/dev/null 2>&1 || true
  echo "$udid"
}

build_app() {
  local device="$1"
  log "Building for $device"
  xcodebuild -project "$IOS_DIR/iosApp.xcodeproj" -scheme iosApp \
    -configuration Debug -sdk iphonesimulator \
    -destination "platform=iOS Simulator,name=$device" \
    -derivedDataPath "$DERIVED" build >/dev/null
}

capture() {
  local udid="$1" locale_dir="$2" lang="$3" prefix="$4" screen="$5" name="$6"
  xcrun simctl terminate "$udid" "$BUNDLE_ID" >/dev/null 2>&1 || true
  xcrun simctl launch "$udid" "$BUNDLE_ID" \
    -AppleLanguages "($lang)" -AppleLocale "$lang" \
    -screenshotMode YES -screenshotScreen "$screen" >/dev/null
  sleep "$SETTLE"
  mkdir -p "$OUT_ROOT/$locale_dir"
  xcrun simctl io "$udid" screenshot "$OUT_ROOT/$locale_dir/${prefix}_${name}.png" >/dev/null 2>&1
}

total=$(( ${#LOCALES[@]} * ${#DEVICES[@]} * ${#SCREENS[@]} ))
log "Capturing $total screenshots (${#LOCALES[@]} locales x ${#DEVICES[@]} devices x ${#SCREENS[@]} screens)"
count=0

for device_entry in "${DEVICES[@]}"; do
  slot="${device_entry%%:*}"
  device="${device_entry#*:}"

  [[ -n "${SKIP_BUILD:-}" ]] || build_app "$device"
  [[ -d "$APP_PATH" ]] || { warn "no app at $APP_PATH"; exit 1; }

  udid=$(boot_sim "$device") || continue
  xcrun simctl install "$udid" "$APP_PATH"

  for locale_entry in "${LOCALES[@]}"; do
    locale_dir="${locale_entry%%:*}"
    lang="${locale_entry#*:}"

    for screen_entry in "${SCREENS[@]}"; do
      name="${screen_entry%%:*}"
      screen="${screen_entry#*:}"
      count=$((count + 1))
      printf '  [%3d/%3d] %-8s %-8s %s\n' "$count" "$total" "$locale_dir" "$slot" "$name"
      capture "$udid" "$locale_dir" "$lang" "$slot" "$screen" "$name"
    done
  done

  xcrun simctl terminate "$udid" "$BUNDLE_ID" >/dev/null 2>&1 || true
  xcrun simctl status_bar "$udid" clear >/dev/null 2>&1 || true
  xcrun simctl shutdown "$udid" >/dev/null 2>&1 || true
done

log "Done. Screenshots in $OUT_ROOT/<locale>/"
log "Upload with: cd iosApp && fastlane deliver_metadata"
