#!/bin/bash

BUNDLETOOL=/home/mandroid/Videos/AA_FILES/bundletool.jar
if [ ! -f "$BUNDLETOOL" ]; then
  echo "Downloading bundletool..."
  curl -L -o "$BUNDLETOOL" https://github.com/google/bundletool/releases/download/1.18.1/bundletool-all-1.18.1.jar
fi

set -e

echo "Building device-specific APKs..."
java -jar "$BUNDLETOOL" build-apks \
  --bundle="$AAB" \
  --output="$APKS" \
  --ks="$KEYSTORE" \
  --ks-pass=pass:"$KS_PASS" \
  --ks-key-alias="$KEY_ALIAS" \
  --key-pass=pass:"$KEY_PASS" \
  --connected-device \
  --overwrite

echo "Installing on connected device..."
java -jar "$BUNDLETOOL" install-apks --apks="$APKS"

echo "Done."
