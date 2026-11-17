---
name: nudge-develop
description: Workflow for changing Nudge in Ivan's fork — adding a feature, supporting a new app or surface (Facebook Reels, TikTok, Snapchat Spotlight…), changing rules/UI, renaming the fork, or porting an idea to another platform. Covers which docs to read, architecture boundaries, tests per layer, the license/offline constraints, and what "done" means here. Use before writing any code in this repo.
---

# Develop in Ivan's Nudge fork

This is a **personal fork** (`ifurs/nudge`) of `astraedus/nudge` (GPL-3.0), released free and offline.
`FORK.md` is the authority for the fork (rules, branches, upstream sync, release); upstream `CLAUDE.md` is
the source for architecture and testing, but its release/Play/"ship autonomously" and "Features go through
Anti" sections are the upstream owner's process and do **not** apply: Ivan decides features; never push to
`origin`, never publish, never tag without Ivan asking.

## Before coding

1. Read `FORK.md`, `tasks/lessons.md` and the matching row of the **Subsystem deep-dives** table in `CLAUDE.md`
   (e.g. `docs/architecture/rules-and-features.md` for in-app detection).
2. Check `docs/BACKLOG.md` — the feature may be planned or known-broken.
3. If behaviour on the phone is involved, capture first (`nudge-debug` §4).

## Architecture rules (from upstream, enforced by tests)

- `domain/` is pure Kotlin: no `android.*` imports. Put decisions there so they are JVM-testable.
- `ui -> domain <- data`, `service -> domain`. Hilt for DI, Room for data, Compose for UI.
- Room schema change → bump DB version + add a migration in `DatabaseModule`; `NudgeDatabaseMigrationTest`
  checks every gap.
- Any `android.*` API above 26 → `Build.VERSION.SDK_INT` guard (lint enforces).
- `NudgeAccessibilityService.onAccessibilityEvent` is a hot path: no blocking I/O, no new early returns
  without reading `foreground-detection.md`.

## Text and language (fork rule)

- No user-visible English literal in Kotlin. Every string goes to `res/values/strings_<area>.xml` with the
  same key in `res/values-uk/strings_<area>.xml`; plurals need uk `one/few/many/other`; positional args.
- Pure code (`domain/`, ViewModels, formatters) returns `UiText`; the UI resolves it (`asString()` /
  `resolve(context)`). Durations/hours/dates via `ui/text/DurationText`, `HourText`, `UiText.Date`.
- Assert wording in JVM tests through `XmlTextResolver` (`.en()`, `.uk()`); `TranslationParityTest` gates
  keys, placeholders and plural forms. Glossary and Ukrainian style: `docs/i18n.md`.
- English and Ukrainian only — no Russian strings or keywords anywhere.
- New screens: add en + uk Paparazzi gallery variants (incl. 360 dp) and look at them.

## Self-control gates (fork rule)

Anything that turns off, shortens or weakens a block (a switch, a Save, an import, a new setting) must, with
Strict Mode on, go through `StrictModeGate.run`, decided by pure policy in `domain/lock/` (`RuleWeakening`,
`SettingsWeakening`, `FeatureOverrideWeakening`), with the decision taken from the same snapshot the write
uses (no check-then-act race). Strengthening stays instant. Ship an L1 test that fails without the gate and
run `nudge-security-reviewer` before committing.

## Syncing with upstream

`git fetch origin && git merge origin/main` (merge, never rebase published branches). Then localize every new
upstream string, check new upstream paths against the gate rule above, run tests + lint + build, keep our
`[Unreleased]` CHANGELOG entries on top. Details: `FORK.md`.

## Adding a new app/surface to block (typical task)

1. Dump the target screen's view tree on the phone (`nudge-debug` §3) and record a capture (§4).
2. Prefer stable **view IDs** over text — text breaks with the phone's language. If text is unavoidable,
   cover English and Ukrainian (and normalise like `StrictModeEscapeGuard.normalize`).
3. Extend `InAppDetector` (`service/`) and the `Feature` enum; wire the rule option in the rule editor.
4. Tests: L1 unit for the decision, L2 replay of the capture (must fail before the fix).
5. Check `docs/architecture/qr.md`'s merged-manifest permission allowlist if any dependency is added.

## Hard constraints for this fork

- **Stay offline:** never add `android.permission.INTERNET` or any network/analytics/crash SDK. The merged
  manifest is checked by the permission allowlist gate — keep it passing, do not loosen it.
- **GPL-3.0:** keep `LICENSE` and copyright notices; new code is GPL-3.0 too. Credit upstream in README/About.
- **Branding:** if Ivan wants to distribute, rename the app (string `app_name`, icon) and change
  `applicationId` in `app/build.gradle.kts` (e.g. `dev.ivan.<name>`); leave `namespace`
  (`com.astraedus.nudge`) alone unless doing a full package move. Update every hard-coded component name
  (`dev.astraedus.nudge/...` in scripts and skills) — rule (b) "fixture honesty": derive, never retype.
- `README.md` is protected by a repo hook; edit it only when Ivan explicitly asks.

## Definition of done

1. `./gradlew :app:testDebugUnitTest` and `./gradlew lintDebug` pass (see `nudge-build`); every new string
   exists in English and Ukrainian.
2. `./gradlew assembleDebug` builds; if a phone is connected, install and check the behaviour on it
   (say plainly if not checked on device).
3. `CHANGELOG.md` entry for user-visible changes.
4. Commit on a branch only when Ivan asks; push only to `fork`, never to `origin`.

## Other platforms

- **iOS:** this approach is impossible (no cross-app screen reading); only Screen Time whole-app limits.
- **Desktop browsers:** `extension/` is upstream's browser extension (WXT + TypeScript, own `CLAUDE.md`,
  `npm test`, Playwright e2e). New web-side features go there.
