fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## iOS

### ios test

```sh
[bundle exec] fastlane ios test
```

Run tests

### ios build_simulator

```sh
[bundle exec] fastlane ios build_simulator
```

Build iOS app for simulator

### ios build

```sh
[bundle exec] fastlane ios build
```

Build iOS app for device (requires signing)

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Submit a new Beta Build to TestFlight

### ios deploy

```sh
[bundle exec] fastlane ios deploy
```

Deploy a new version to the App Store

### ios deliver_metadata

```sh
[bundle exec] fastlane ios deliver_metadata
```

Upload metadata and screenshots only (no binary)

Pass skip_screenshots:true to upload text metadata without touching screenshots

### ios bump_build

```sh
[bundle exec] fastlane ios bump_build
```

Increment build number

### ios bump_version

```sh
[bundle exec] fastlane ios bump_version
```

Increment the marketing version (major|minor|patch, default patch)

### ios submit_review

```sh
[bundle exec] fastlane ios submit_review
```

Submit the current App Store version for review (uploads nothing)

Declarations mirror iosApp/appstore/app_store_connect_answers.md

### ios latest_beta

```sh
[bundle exec] fastlane ios latest_beta
```

Print the latest build number available on TestFlight

### ios sync_certificates

```sh
[bundle exec] fastlane ios sync_certificates
```

Sync certificates and provisioning profiles

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
