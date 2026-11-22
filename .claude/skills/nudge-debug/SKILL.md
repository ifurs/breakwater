---
name: nudge-debug
description: Debug Nudge on a real phone — blocking not triggering, Reels/Shorts not detected, wrong re-blocks, service stopped, crashes. Covers logcat, checking the accessibility service is bound, dumping Instagram/YouTube view trees to find view IDs, and recording accessibility-event captures for replay tests. Use for any "it doesn't block", "it blocks the wrong thing", "Instagram/YouTube updated and it broke", or crash report.
---

# Debug Nudge on device

Set up the environment and `DEVICE` first (see `nudge-build`). All commands below use `adb -s "$DEVICE"`;
define `a() { adb -s "$DEVICE" "$@"; }` for brevity.

## 0. Rule: capture before theorising

Upstream's hardest-won lesson: every accessibility bug was code assuming an event stream the device does not
produce. Reproduce on the phone and record it **before** designing a fix. Read
`docs/architecture/accessibility-event-pipeline.md` and `docs/testing-strategy.md` (routing table) first.

## 1. Is the service even running?

```bash
a shell settings get secure enabled_accessibility_services   # must contain dev.ifurs.breakwater/...NudgeAccessibilityService
a shell dumpsys accessibility | grep -A3 'Bound services'     # must list Nudge
a shell dumpsys activity services dev.ifurs.breakwater | grep -E 'ServiceRecord|isForeground'
a shell dumpsys deviceidle whitelist | grep nudge             # battery optimisation exemption
```

Not bound after an install → re-grant (see `nudge-build` §4). Killed overnight → OEM battery killer;
see `docs/architecture/service-lifecycle-and-watchdog.md`.

## 2. Logs

Debug logs exist only in a debug build, or a release build with debug logging enabled
(Settings → tap the version number 7 times). Tags are string literals (`logger.tagged("Name")`; R8 is on in
release since 1.22.4, so tags are never inferred from class names) — grep the source for the tag you need.

```bash
a logcat -c
a logcat --pid="$(a shell pidof -s dev.ifurs.breakwater | tr -d '\r')" -v time
a logcat -v time | grep -E 'InAppDetector|NudgeAccessibilityService|BlockEngine|AndroidRuntime'
a logcat -b crash -d                                            # crashes
```

Useful decision lines: `instagram clips viewer detected`, `block package=… reason=…`.

## 3. Detection broke after an Instagram/YouTube update (most common)

Detection lives in `app/src/main/java/com/astraedus/nudge/service/InAppDetector.kt`: Instagram uses view IDs
(`clips_viewer_view_pager`, `clips_tab`, `feed_tab`…), YouTube and the fallback paths match on-screen
**text** like "Shorts"/"Reels". Check:

1. App versions: `a shell dumpsys package com.instagram.android | grep versionName` (same for
   `com.google.android.youtube`).
2. Phone language: `a shell getprop persist.sys.locale`. Text matching may fail on a non-English UI
   (e.g. Ukrainian) — test both if a text path is involved.
3. Dump the live view tree on the problem screen and look for the IDs/text the detector expects:
   ```bash
   a shell uiautomator dump /sdcard/ui.xml >/dev/null && a shell cat /sdcard/ui.xml > /tmp/claude-ui.xml
   grep -oE 'resource-id="[^"]+"' /tmp/claude-ui.xml | sort | uniq -c | sort -rn | head -40
   grep -oE '(text|content-desc)="[^"]*(Reels|Shorts)[^"]*"' /tmp/claude-ui.xml
   ```
   A bare `uiautomator dump` is safe. **Never** keep a UiAutomator/Maestro session open while testing
   blocking — Android suppresses every other accessibility service (Nudge) while it is connected.
4. Ask Ivan to take a screenshot if needed: `a exec-out screencap -p > /tmp/claude-screen.png`, then Read it.

## 4. Record a replayable capture (L2 test)

```bash
DEVICE="$DEVICE" scripts/a11y-capture.sh <descriptive-name> 30   # Ivan reproduces during the 30 s
```

Writes `app/src/test/resources/a11y-captures/<name>.jsonl`. Add the `# EXPECTED (test oracle):` header,
write a replay test that FAILS on it (see existing `yt-shorts-then-home-tab.jsonl` and its test), then fix.
Other probes in `tools/qa/` default to the upstream bench IP — always pass `DEVICE=`.

## 5. Foreground / overlay state

```bash
a shell dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity'
a shell dumpsys window | grep -E 'mCurrentFocus'
```

## Reporting

Tell Ivan what was observed (log lines, IDs found/missing, versions), the layer the regression test sits at
(L1–L6), and what was not verified on the device.
