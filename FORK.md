# Breakwater: Ivan's fork of Nudge

Breakwater (this repository, `ifurs/breakwater`) is a personal fork of [astraedus/nudge](https://github.com/astraedus/nudge),
released free, offline and under GPL-3.0. Upstream's `CLAUDE.md` stays the reference for architecture and
testing; this file says where the fork differs and how to work on it. **When the two disagree, this file
wins.**

## What the fork adds

- **Ukrainian translation** of the whole app, with typed UI text (`UiText`) so no English sentence is built
  in pure code. Rules, glossary and style: [`docs/i18n.md`](docs/i18n.md).
- **Easier navigation:** a bottom bar (Home, Apps, Stats, Settings) and a "Block short videos" card on Home
  with one switch each for Instagram Reels, YouTube Shorts, the TikTok feed and Instagram Explore. The
  per-app editor shows the Reels/Shorts section first.
- **Strict Mode hardening:** lowering the challenge difficulty, removing or weakening a Reels/Shorts block
  (Home switch or app editor Save) all need the unlock challenge; the Settings escape guard recognises
  Ukrainian system screens and survives non-breaking spaces and apostrophe variants.
- The fork's changes are listed under `[Unreleased]` in `CHANGELOG.md`.

## Rules (people and Claude)

1. **Offline forever.** Never add `android.permission.INTERNET` or any network, analytics, ads or crash SDK.
   The merged-permission gate (`verify<Variant>MergedPermissions`, `allowedMergedPermissions` in
   `app/build.gradle.kts`) runs on every `assemble*` — keep it passing and never loosen it.
2. **GPL-3.0.** Keep `LICENSE` and the upstream copyright; new code is GPL-3.0; credit upstream.
3. **Every user-visible string goes to resources in English and Ukrainian** (`values/strings_<area>.xml`
   + the same file in `values-uk/`). `TranslationParityTest` and lint `MissingTranslation` fail the build
   otherwise. No Russian, anywhere: not as a translation, not as keywords.
4. **Self-control gates are security.** Any new way to turn off, shorten or weaken a block must go through
   `StrictModeGate` while Strict Mode is on, decided by the pure policy in `domain/lock/`
   (`RuleWeakening`, `SettingsWeakening`, `FeatureOverrideWeakening`). Add an L1 test that fails without
   the gate.
5. **Upstream's release and process sections do not apply here:** "Releasing", "Google Play release",
   "SHIP AUTONOMOUSLY", the post-feature checklist's publish steps, and "Features go through Anti" (that is
   the upstream owner's approval flow; here Ivan decides). Never push to `origin` (upstream), never tag
   upstream, never run `scripts/publish-to-play.sh`, `gplay` or `gh release` against upstream.
6. **No bench Pixel 3.** `192.168.1.68:5555` is upstream's device. Use the phone from `adb devices` and pass
   `DEVICE=<serial>` to every script in `scripts/` and `tools/qa/`.
7. Claude commits only when Ivan asks, on a branch, and pushes only to `fork`.

## Setup (Ubuntu)

```bash
sudo apt install openjdk-21-jdk               # JDK 17 or 21; sources target Java 17
export ANDROID_HOME=$HOME/Android/Sdk
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-36" "platform-tools"
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

Gradle comes from `./gradlew` (8.13). Never upgrade to Gradle 9.

## Everyday commands

```bash
./gradlew :app:testDebugUnitTest      # L1–L4 JVM tests (~3,500, a few minutes); what CI runs
./gradlew :app:testDebugUnitTest --tests '*ShortVideo*'   # one area; keep the :app: prefix
./gradlew lintDebug                   # L5; the only check that catches API>26 calls (minSdk 26)
./gradlew assembleDebug               # app/build/outputs/apk/debug/app-debug.apk + permission gate
./gradlew :app:verifyPaparazziDebug   # screenshot tests (docs/screenshot-tests.md)
./gradlew :app:recordPaparazziDebug   # re-record after an intended UI change, then look at the PNGs
```

Screenshots of every gallery screen, in English and Ukrainian, light and dark, and at 360 dp, are in
`app/src/test/snapshots/images/`. Goldens recorded on JDK 21 can differ from CI's JDK 17 only in PNG
encoding; revert those.

Test layers L1–L6 and what each change needs: `docs/testing-strategy.md`.

## Phone

1. Developer options → USB debugging (or Wireless debugging → pair with code: `adb pair IP:PORT`, then
   `adb connect IP:PORT`).
2. `adb devices`, then `adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk`.
3. Turn on Breakwater's accessibility service. A sideloaded app may need App info → ⋮ → *Allow restricted
   settings* first. `install -r` drops the grant; re-enable it after each install.
4. Breakwater's `applicationId` is `dev.ifurs.breakwater`, so it no longer clashes with the Play/F-Droid
   Nudge (`dev.astraedus.nudge`) and can be installed next to it. Its data is separate: to move your rules,
   export them in Nudge's Settings and import them in Breakwater.

Debugging detection, logs and accessibility captures: `.claude/skills/nudge-debug/SKILL.md`.

## Branches and syncing with upstream

- Remotes: `origin` = upstream `astraedus/nudge` (read-only for us), `fork` = `ifurs/breakwater`.
- Work on `feature/<name>` or `fix/<name>` branches, merge into the fork's main line, push to `fork`.
- Pull upstream regularly, by merge (never rebase published branches):
  ```bash
  git fetch origin
  git merge origin/main
  ```
  Then: localize every new upstream string (rule 3), check upstream did not add a path around rule 4, run
  tests + lint + build, and keep our `[Unreleased]` CHANGELOG entries above upstream's released sections.
- A fix that upstream would want (e.g. a Strict Mode gate) can go upstream as a PR from a branch cut from
  `origin/main`: `gh pr create --repo astraedus/nudge --head ifurs:<branch>`.

## Releasing the fork (when Ivan decides)

1. Done: the fork is named Breakwater with `applicationId` `dev.ifurs.breakwater` in `app/build.gradle.kts`;
   `namespace` stays `com.astraedus.nudge`. `app_name` is identical in `values` and `values-uk` (see
   `docs/i18n.md`); scripts, skills and tests use the new ID (tests read it from `NudgeIdentity`).
2. Make our own release keystore; keep it and `keystore.properties` out of git. Losing it means users can
   never update.
3. `./gradlew assembleRelease`, then publish on GitHub Releases of `ifurs/breakwater` (Obtainium can track it)
   or submit to F-Droid as a separate app.

## Claude Code in this repo

- Skills (`.claude/skills/`): `nudge-build` (toolchain, build, install), `nudge-debug` (phone debugging,
  detection, captures), `nudge-develop` (feature workflow and the rules above).
- Agents (`.claude/agents/`): `nudge-ux-designer` (screens, copy, accessibility, Ukrainian) and
  `nudge-security-reviewer` (read-only MASVS/privacy review; run it before committing manifest,
  permission, dependency, export/import, accessibility-service or Strict/Nuke gate changes).
- Use Sonnet for heavy subagent work (builds, merges, reviews) to keep token use down.
- Machine-specific notes (paths on one laptop) go in `CLAUDE.local.md`, which is not committed.
