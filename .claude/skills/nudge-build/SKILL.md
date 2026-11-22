---
name: nudge-build
description: Build, test, lint and install Nudge on Ivan's own Android phone from this Linux laptop. Use when asked to build the app, run tests, run lint, install/reinstall on the phone, connect the phone over ADB (USB or wireless), or when a Gradle/JDK/SDK error appears.
---

# Build and install Nudge (Ivan's setup)

Fork rules and the human-readable version of this skill: `FORK.md`. Upstream `CLAUDE.md` assumes the
author's Pixel 3 at `192.168.1.68:5555`. That device does not exist here.
Always pass Ivan's phone serial explicitly (`DEVICE=…` / `adb -s …`).

## 1. Environment (every shell — Bash state does not persist)

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}
export PATH=$ANDROID_HOME/platform-tools:$PATH
```

Requirements (check with `java -version`, `ls $ANDROID_HOME/platforms`):
- JDK 17 or 21 (Ubuntu: `openjdk-21-jdk` or `openjdk-17-jdk`). Source/target are Java 17.
- SDK platform `android-36` (compileSdk 36) + build-tools. If missing:
  `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-36"`
- Gradle comes from `./gradlew` (8.13). **Never upgrade to Gradle 9.**

## 2. Gates (run before calling anything done)

```bash
./gradlew :app:testDebugUnitTest   # L1–L4, all JVM, no device (~3,500 tests); what CI runs
./gradlew lintDebug                # L5 — the ONLY check that catches API>26 calls (minSdk 26)
./gradlew assembleDebug            # → app/build/outputs/apk/debug/app-debug.apk + merged-permission gate
./gradlew :app:verifyPaparazziDebug   # screenshot gallery (en + uk); record with recordPaparazziDebug
```

Run long builds in the background (first build downloads dependencies, several minutes).
`./gradlew test` runs the same JVM tests twice (debug + release); prefer `:app:testDebugUnitTest`.
For a single test class: `./gradlew :app:testDebugUnitTest --tests '*InAppDetector*'` (keep `:app:` — without
it the `--tests` filter also hits the `:qa-driver` module and fails with "No tests found").
After re-recording Paparazzi goldens, Read the changed PNGs; revert goldens that differ only in PNG encoding
(this laptop's JDK 21 vs CI's JDK 17).

## 3. Connect the phone

- USB: enable Developer options → USB debugging, plug in, accept the RSA prompt, `adb devices`.
- Wireless (Android 11+): Developer options → Wireless debugging → *Pair device with pairing code*, then
  `adb pair <ip>:<pair-port>` (Ivan types the code himself) and `adb connect <ip>:<port>`.
- Set `DEVICE=<serial from adb devices>` and use `adb -s "$DEVICE"` everywhere.

## 4. Install

```bash
adb -s "$DEVICE" install -r app/build/outputs/apk/debug/app-debug.apk
```

- **Side by side with Nudge:** the fork is `dev.ifurs.breakwater`, so it installs next to the Play/F-Droid
  Nudge (`dev.astraedus.nudge`) without a signature conflict. Their data is separate.
- **`install -r` drops the accessibility grant.** Re-enable:
  ```bash
  adb -s "$DEVICE" shell settings put secure enabled_accessibility_services \
    dev.ifurs.breakwater/com.astraedus.nudge.service.NudgeAccessibilityService
  adb -s "$DEVICE" shell settings put secure accessibility_enabled 1
  ```
  Note this overwrites the list; if Ivan uses other accessibility services, read the current value first and
  append with `:`.
- Android 13+ sideloads: if the accessibility toggle is greyed out, App info → ⋮ → *Allow restricted settings*.

## 5. Release-signed build (only when Ivan wants to distribute)

`assembleRelease` needs `keystore.properties` + a keystore (both gitignored). Ivan creates his own key;
never use or request upstream's. Losing the key means users cannot update.

## Do not

- Run `scripts/publish-to-play.sh`, `gplay`, `gh release create` against upstream, or push to `origin` (that
  is upstream `astraedus/nudge`). Push only to `fork` (`ifurs/nudge`). Upstream's "ship autonomously" flow
  does not apply in this fork.
- Run `scripts/device-qa.sh` without `DEVICE=…`; it also downloads upstream APKs by default (`APK=main`) —
  use `APK=installed` or `APK=debug ALLOW_CERT_SWITCH=1`.
