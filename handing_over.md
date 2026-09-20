# WasteTrack — Handing Over

Status as of this document: **Sections 1-11 of TODO.md are complete** (engine through cross-platform verification), **plus a full visual redesign pass** matching a reference mockup the user supplied (`WasteTrack UI — admin and driver.pdf`, in `~/Downloads`, not committed to the repo), and a new manual Admin/Driver mode switch + profile chip that isn't part of CLAUDE.md's spec. Section 11 is genuinely web-verified with real screenshots (see below) — this is the first point in the project where the UI has actually been *seen* rendering, not just compiled. Sections 12-13 (localization/polish, demo prep) are not started. A `README.md` now exists at the repo root. Read `CLAUDE.md` first — it's the full spec — then `TODO.md` for the section-by-section checklist. This file is the "what actually happened and what to watch for" layer on top of both.

---

## How to run things

From the project root:
```
./kotlin build --module jvm-app --module shared --platform jvm   # compile check, fast
./kotlin test --include-module shared --platform jvm             # run all tests
./kotlin run --module jvm-app                                     # launch desktop (dev loop)
```

**Always scope with `--module` and `--platform jvm`.** An unscoped `./kotlin build` tries to compile every platform target declared in `shared/module.yaml` (`jvm, android, iosArm64, iosSimulatorArm64, wasmJs`), including iOS — which is explicitly out of scope per CLAUDE.md ("Not used... Ignore. Do not delete") and whose native (Konan/LLVM) toolchain has been failing to extract in this environment due to disk pressure (see Environment quirks below).

---

## What's built

### Engine (`shared/src/engine/`) — Sections 1-3
- `Models.kt`, `PointInPolygon.kt` (ray-casting, verified against known in/out points), `VisitClassifier.kt`, `ShiftEvaluator.kt` (`evaluateShift()` + the stateful `ShiftTracker` class), `SimulatedSource.kt`.
- Zero `androidx`/Compose imports anywhere in this package — verified by grep, and by the fact `shared/test` compiles and runs against it standalone.
- `shared/test/EngineTest.kt` — one test per scenario (compliant/no-zone/drive-through/partial), run against the real §10/§11/§12 data.

### Data (`shared/src/data/`) — Section 4
- `Zones.kt` — `squareZone()` helper (±0.0009° half-width, ~200m square at Accra's latitude — an approximation, per CLAUDE.md's own instruction not to bother with geodesic precision) + the 5 real zones from the July 2026 directive.
- `Vehicles.kt` — all 6 vehicles, tricycle tare 250kg / truck tare 2500kg (spec's own suggested magnitudes).
- `Scenarios.kt` — all 4 routes. Compliant/drive-through/partial share one 30-point coordinate list through the Achimota zone (verified against the real zone box: indices 11-19 are inside, everything else outside — see the Python check in conversation history if you need to re-derive it); only `loadKg` differs between them, per the PRD's explicit note. No-zone is a separate 28-point path confirmed outside all 5 zone boxes.

### State management (`shared/src/ui/FleetViewModel.kt`) — Section 5, extended in 8/9
This grew beyond Section 5's original single-vehicle scope once Sections 8-9 needed it. Current shape:
- `FleetState` holds **all 6 vehicles** as `Map<String, VehicleSnapshot>` (each with `currentReading`, `loadHistory`, `visits`, `verdict`, `reason`), plus `selectedVehicleId`, `manualKg`, `noiseEnabled`, `isRunning`, `language`, `darkTheme`, `reminderEnabled`.
- On construction, **every vehicle's snapshot is computed for real** by running its assigned default scenario fully through a fresh `ShiftTracker` — see `DefaultScenarios.kt` for the assignment (flagged as an assumption below). This is what makes the Fleet Overview table non-hardcoded.
- `start(scope, source)` streams a `ReadingSource` into whichever vehicle is currently selected, live, updating that one vehicle's snapshot per reading (including `loadHistory`, used by the Vehicle Detail chart).
- `selectVehicle(id)` cancels any live stream and resets `manualKg`/`noiseEnabled` — switching vehicles doesn't carry over a slider position from a different vehicle.

**Important design decision — confirmed with the user, don't relitigate without reason:** `ShiftTracker.result(shiftEnded: Boolean)` in `ShiftEvaluator.kt` only fires the §9 "never reached a zone → Flagged" rule once `shiftEnded` is genuinely true. `FleetViewModel.start()` passes `shiftEnded = false` on every in-flight reading and `true` only once the stream completes. Get this wrong and every scenario shows red "Flagged" from frame one, before the vehicle has gone anywhere — see the earlier back-and-forth in conversation history if this needs re-deriving. A currently-active zone visit is *always* hypothetically closed and evaluated regardless of the flag — that's what lets a live weight override flip the verdict mid-visit.

### Adaptive shell + shared components — Sections 6-7
- `App.kt` — the single root composable. `BoxWithConstraints` branches on `isAdminLayout(maxWidth)` (`LayoutBreakpoint.kt`, unit-tested — pure `width >= 700.dp` check, pulled out specifically so it doesn't need a real display to verify). Wraps everything in `WasteTrackTheme(darkTheme = state.darkTheme)`.
- `ui/components/`: `VerdictBadge`, `WeightGauge`, `ZoneCanvas` (zone square + route polyline + moving dot, y-axis inverted for lat), `LoadOverTimeChart` (dashed tare line + shaded tolerance band).
- `ui/theme/WasteTrackTheme.kt` — light/dark `ColorScheme`.

### Driver screens (`shared/src/ui/screens/driver/`) + `DriverRoot.kt` — Section 8, restyled after a reference mockup (see below)
`LiveShiftScreen`, `DropOffResultScreen`, `ShiftSummaryScreen`, `DriverSettingsScreen` — 4 files now, not 5. `DropOffReminderScreen` was removed: its content is folded into `LiveShiftScreen` as a conditional `StatusBanner` (green "Inside zone" when applicable, amber "haven't dropped off yet" when carrying a load with no visits and reminders are on, neutral otherwise) — see the visual redesign section below for why. `DriverRoot` auto-starts the selected vehicle's default scenario as a live stream on entry (`LaunchedEffect(state.selectedVehicleId)`) and now uses a 3-tab bottom nav (Shift / Activity / Settings) instead of 5 buttons — `Activity` is `ShiftSummaryScreen`, tapping a zone visit row drills into `DropOffResultScreen` (local `viewingResult` boolean in `DriverRoot`, not a 4th tab). Driver copy says "empty weight," never "tare weight," per §13.

### Admin screens (`shared/src/ui/screens/admin/`) + `AdminRoot.kt` — Section 9, restyled after a reference mockup
All 4: `FleetOverviewScreen` (stat cards + filterable table + zone source citations panel folded into `LiveMapScreen` instead, per spec — no separate zones page), `LiveMapScreen`, `VehicleDetailScreen`, `AdminSettingsScreen`. Clicking a vehicle row in Fleet Overview calls `viewModel.selectVehicle(id)` and jumps to Vehicle Detail. `AdminRoot` now uses a dark left sidebar nav (fixed 220dp width) instead of a top button row, matching the mockup.

### Simulation panel (`screens/admin/SimulationPanelScreen.kt`) — Section 10
Scenario chips (Compliant/No zone/Drive-through/Partial) each call a local `runScenario()` that resets `manualKg`→null and `noiseEnabled`→false, then calls `viewModel.start()` with that scenario's route — a clean run every time a chip is clicked. Weight slider is `250f..400f`, starts at a visual default of 320f but does **not** call `setManualKg` until the user actually drags it (so `manualKg` genuinely starts `null`/Auto, per the explicit warning in CLAUDE.md §15 that getting this wrong makes every visit read as a zero-change "no drop-off"). "Run" restarts whichever scenario is currently selected (same clean-reset path as a chip click); "Pause" just calls `viewModel.stop()` without touching state; "Reset" stops and clears the override/noise/slider back to defaults without restarting.

**The core demo-script mechanic is proven by a dedicated test**, not just eyeballed: `SimulationDemoScriptTest.sliderFlipsVerdictLiveWithinTheSameZoneVisit` drives the drive-through route reading-by-reading (via a deterministic `SteppableSource` — see `shared/test/TestSupport.kt`), and asserts the verdict is `COMPLIANT` after dragging to empty weight mid-visit, flips to `FLAGGED` after dragging back up (**still inside the same zone visit, before the vehicle exits**), then back to `COMPLIANT` after dragging down again and letting the route finish. That sequence — flip, flip back, all pre-exit — is the exact thing CLAUDE.md §15's demo script describes.

### Visual redesign + mode switch / profile chip (post-Section-10, before Sections 11-12)
The user supplied a 9-page PDF mockup (`WasteTrack UI — admin and driver.pdf`) as the target look and asked for two additions not in CLAUDE.md: a manual Admin/Driver mode switch, and a profile chip beside it. This was a full visual pass across every screen, not just new features:

- **`ui/theme/DesignTokens.kt`** — a `Palette` data class (light + dark instances) with verdict-specific fg/bg color pairs (`compliantFg`/`compliantBg`, etc.), sidebar colors, surface/background tokens. Every screen now takes `state.darkTheme` and computes `paletteFor(state.darkTheme)` locally rather than reading raw hardcoded hex — this is the thing to extend if judges want different colors before the pitch.
- **`ui/TopControlBar.kt`** — new, not in CLAUDE.md. A persistent 48dp strip above both layouts (added in `App.kt`, so it's visible regardless of admin/driver) containing a 3-way `Auto | Admin | Driver` segmented switch (`FleetState.layoutOverride: LayoutMode?`, `null` = follow the width breakpoint) and a profile chip (colored initials circle + driver name + vehicle id, sourced from `state.selectedVehicle`). `App.kt`'s layout decision is now `state.layoutOverride?.let { it == LayoutMode.ADMIN } ?: isAdminLayout(maxWidth)`.
- **`AdminRoot.kt`** — rebuilt with a fixed-width (220dp) dark sidebar nav (WasteTrack wordmark, 5 nav items with a highlighted-pill active state, EN/TWI + Dark mode toggles pinned to the bottom), matching the mockup's admin pages.
- **`DriverRoot.kt`** — rebuilt with a 3-tab bottom nav (Shift/Activity/Settings) instead of 5 buttons, per the mockup's IA (see above).
- **`VehicleDetailScreen.kt`** — added a genuinely-computed "How this was decided" reasoning list (`reasoningFor()`), not static copy: it reads the actual last visit's drop amount and tolerance check and produces the matching sentence. Uses the *real* `MIN_DROP_KG` constant (20kg) from the engine — the mockup's own placeholder copy says "5 kg minimum," which is just mockup flavor text, not a spec change; don't let that 5kg number leak into the real implementation.
- **`SimulationPanelScreen.kt`** — rebuilt as a two-column layout (driver-behaviour scenario buttons + slider + noise + run/pause/reset on the left; live verdict card + a static "same route, different weight" explainer table + zone canvas on the right), matching the mockup.

**Two deliberate deviations from the mockup, both because CLAUDE.md's constraints override visual fidelity:**
1. **No real basemap.** The mockup's Live Map shows a real Leaflet/OpenStreetMap map with roads and coastline. CLAUDE.md explicitly rules out network calls ("all data is simulated... not fetched from any network"). `LiveMapScreen` draws a stylized flat Canvas map instead (zone dots + vehicle dots on a normalized lat/lng projection) — same information, no map tiles.
2. **No fabricated shift history.** The mockup's Shift Summary shows specific wall-clock times ("06:10 to 14:24") and a "Recent shifts" list of other days. We only track the current simulated shift; inventing a plausible-looking history would be fabricated data, which contradicts CLAUDE.md's insistence that simulated data stay honestly labeled. `ShiftSummaryScreen` omits both.

**New assumption**: the mockup's driver screens default to dark theme while admin defaults to light — but `darkTheme` is one shared boolean across the whole app (both layouts have their own toggle in Settings, matching the mockup's own per-screen dark-mode controls). Rather than hardcoding "driver always starts dark," this was treated as the same user preference toggle in both places, defaulting to light for both. If judges specifically want driver-dark by default, that's a one-line change (default `darkTheme` in `FleetState`, or split into two independent flags).

**Verification note**: same limitation as before (no display) *for the redesign pass itself*. That changed in Section 11 — see below, this got real browser verification and the "nobody has seen it render" caveat no longer applies to the wasm target.

### Cross-platform verification — Section 11
- **Web (`wasm-app`) — genuinely verified, not just compiled.** Chrome browser automation controls a real browser (not this sandbox's own display), so this is actual visual confirmation: navigated to the running `wasm-app` dev server and clicked through all 9 screens, toggled dark mode, and used the new Admin/Driver mode switch — all render correctly and match the intended design. Also drove the live Simulation mechanic in the real browser: selected "Drives through," watched the verdict flip to `Flagged` live the instant the vehicle entered the zone, and confirmed the weight-override slider applies live to the streamed load value (the exact full round-trip back to `Compliant` wasn't caught in a single screenshot, since manual browser-automation timing couldn't reliably land inside the ~3-second zone-dwell window of a 350ms/reading replay — but the mechanic is already proven deterministically by `SimulationDemoScriptTest`, and this live session confirmed it's wired correctly end to end).
- **Android (`android-app`) — compiles and packages, could not confirm running.** `./kotlin build --module android-app --module shared --platform android` succeeds and produces a real debug APK (`build/tasks/_android-app_buildAndroidDebug/.../gradle-project-debug.apk`). This environment has a full Android SDK at `~/Library/Android/sdk` with an existing AVD (`Medium_Phone`, android-37 arm64) already running (started independently, not by this session) — but it is **unresponsive**: `adb devices` reports it as `device`, yet every single shell command (`echo hello`, `getprop`, `logcat -d`, even `adb install`) times out, and the qemu process has used ~0.8% CPU over 2+ hours, meaning it's hung, not slow-booting. This looks like a virtualization limitation of this sandboxed environment, not something fixable by retrying or waiting longer. **Did not kill the pre-existing emulator process** — it wasn't started by this session and its purpose to the user is unknown. If you have a working emulator or physical device, `adb install -r <path above>` then `adb shell am start -n com.example.app/hello.world.MainActivity` should launch it directly (see `android-app/module.yaml` for the actual applicationId if `com.example.app` doesn't work — check first).

**Two real, genuine bugs found and fixed during this live testing** (not from static review — only surfaced by actually clicking through a rendered app):
1. `ZoneCanvas` computed its zoom bounding box from `zone.boundary + route`, so a 30-point route spanning kilometers made the 200m zone render as a barely-visible speck in a corner instead of the prominent "to-scale" inset both CLAUDE.md §13 and the mockup call for. Fixed: the projection now zooms to the zone itself plus a fixed margin (`ZONE_VIEW_MARGIN_FACTOR = 0.8`, so the zone fills roughly the middle third of the canvas); route/position points outside that view are simply clipped by the Canvas, which is the correct behavior.
2. `manualKg`/`noiseEnabled` set from the admin Simulation panel were leaking into the driver's Live Shift screen when switching via the new mode-switch toggle, since both screens share one `FleetViewModel` and `DriverRoot` never reset them. Fixed: `DriverRoot`'s `LaunchedEffect` now clears both before starting its own stream, so the driver's shift always starts clean regardless of what a judge left set in the admin panel.

Also found and fixed **`MainActivity.kt` and `wasm-app/src/main.kt` were still calling the old template `Screen()`** instead of `App()` — only `jvm-app` had actually been wired to the real app. This was a genuine gap from Section 6 (whose "done when" clause explicitly requires all three entry points to display the same `App()`) that went unnoticed until this section's cross-platform check. Worth remembering: a "done" checkbox on a section doesn't mean every one of its stated acceptance criteria was independently re-verified — cross-checking earlier claims against the actual code is exactly what this section was for.

---

## Assumptions and invented details to confirm against the real spec/judges' expectations

None of these are blocking, but none are specified in CLAUDE.md either — flag if they need to change:

1. **Fleet Overview's initial 6-vehicle mix** (`ui/DefaultScenarios.kt`): the 4 tricycles get the 4 canonical scenarios 1:1 (Kwame→Compliant, Yaw→Partial, Abena→Drive-through, Kofi→No-zone); the 2 trucks get No-zone and Drive-through respectively, *not* Compliant/Partial — because those routes' `loadKg` values are tuned to the tricycle tare (250kg) and would silently misclassify against a truck's 2500kg tare. Current mix: 1 Compliant, 1 Partial, 4 Flagged.
2. **Colors**: exact hex values in `VerdictBadge.kt`/`WasteTrackTheme.kt` are placeholders — CLAUDE.md only names the words (green/amber/red/blue), not shades.
3. **"Collected vs. delivered" on the driver Shift Summary screen**: interpreted as collected = Σ(entryWeightKg − tareKg) across visits, delivered = Σ(entryWeightKg − exitWeightKg). Not defined in CLAUDE.md.
4. **`Verdict.UNKNOWN`/grey doesn't exist**: CLAUDE.md §13 names a grey "Unknown" badge state, but the `Verdict` enum (§8) only has 4 values. Nothing currently needs it — will matter if a screen ever needs to show "no data yet" for a vehicle.
5. **Reading interval / replay speed**: 1000ms between readings in the route data itself (just timestamps, affects `dwellMs`); `SimulatedSource` playback delay used across screens is 300-350ms/reading (within the spec's stated 300-400ms range).
6. **Zone box half-width**: 0.0009° (~100m), a round-number approximation, per CLAUDE.md's explicit permission to skip geodesic precision.
7. **Vehicle "capacity"** (`LiveShiftScreen`'s load bar): `tareKg * 1.6`, a display-only estimate — `Vehicle` (§8) has no capacity field, so this isn't used by the classifier, only the progress bar visual.
8. **Shared `darkTheme` flag** applies to both layouts identically (see the visual-redesign note above) rather than defaulting driver to dark and admin to light as the mockup's screenshots happen to show.

---

## Environment quirks (this sandbox specifically — may not apply elsewhere)

- **No attached display for the desktop (jvm-app) window specifically.** `screencapture` fails with "could not create image from display." Compose Desktop windows run and exit cleanly (no crash), but there's no way to visually confirm *jvm-app's own window* or simulate clicks/drags on it. This is why Section 11 leaned on `wasm-app` + Chrome browser automation instead — that's a real browser, not this sandbox's display, and gave genuine screenshots. If jvm-app-specific rendering ever needs checking, either forward the display or trust that it renders identically to the now-verified web build (same `App()` composable, same engine).
- **Android emulator is present but hung** — see Section 11 above for the full diagnostic. Don't re-attempt booting a fresh one without checking `ps aux | grep qemu` first; a `Medium_Phone` AVD instance may already be running (started outside this session) and launching a second one fails immediately ("Running multiple emulators with the same AVD is an experimental feature").
- **Disk**: was at 99% capacity (~291MB free) earlier in this session; recovered to ~6GB+ free at some point without direct action (unclear why — possibly unrelated system cleanup). Stayed healthy through the Android/web builds in Section 11.
- **`build/` (289MB+) was untracked** — added to `.gitignore`. If you see it reappear in `git status`, that's expected; don't commit it.

---

## Git workflow

Remote is `origin` → `https://github.com/hesGr1ff0/Jetbrains_Hackathon.git`, branch `main`. The user asked mid-session for commits+pushes to be part of the normal workflow going forward (not just on request) — see the memory file `feedback_git_commits_in_process.md` if continuing in a fresh session. Commits so far, each after a green build+test:
1. `aa4018f` — initial snapshot through Section 4 (this one predates this session's own `git commit` calls — likely an editor/IDE auto-commit, not something explicitly invoked)
2. `6aeba71` — Section 5 (FleetViewModel) + the ShiftTracker Option-B fix
3. `c4b7c1d` — Sections 6-7 (App shell, WeightGauge, theme)
4. `1df7dca` — Sections 8-9 (all 9 screens, extended FleetViewModel, this doc's first version)
5. `8e644e5` — Section 10 (Simulation panel + demo-script test)
6. `4223a4e` — visual redesign to match the reference mockup + mode switch/profile chip + README.md
7. `76592cc` — Section 11 (cross-platform verification): wired `MainActivity.kt`/`wasm-app` to the real `App()`, moved JVM-only tests to `shared/test@jvm/` (fixes wasmJs test compilation), fixed the `ZoneCanvas` zoom bug and the Simulation-panel-to-driver state leak, both found via real browser testing
8. *(pending as of this doc)* — added a "How to demo this" section to README.md: run web/desktop from your own terminal rather than a sandboxed assistant process (which is why the Android emulator hung — no display/GPU access in that process), Android via a physical device as the more reliable fallback, and record a backup video ahead of time

`ios-app`'s IDE-generated diffs (`project.pbxproj`, a `.xcscheme` file) have been deliberately left unstaged every time — that module is out of scope per CLAUDE.md and its churn isn't ours to manage.

---

## What's next (Sections 12-13, in order)

1. **Section 12 — Localization/polish.** `Strings.kt` with EN/TWI, wired via `CompositionLocalProvider`. Dark/light consistency: all screens already read from `paletteFor(state.darkTheme)` (not hardcoded colors) as of the redesign pass, so this should mostly be a matter of spot-checking rather than rework — `ZoneCanvas` and `LoadOverTimeChart` are the two components that still use fixed hex colors rather than the palette (zone green / route blue / dot red), worth a look for dark-mode contrast.
2. **Section 13 — Demo prep.** Screen recording — web (`wasm-app` + a real browser) is the strongest option now that it's genuinely verified; consider it over jvm-app given the desktop-window display limitation above. Pitch rehearsal, backup video.
3. **If there's any time left over**: the Android emulator situation is worth one more look with a real display attached (not this sandboxed session) — the SDK, AVD, and a working APK are all already in place; only the emulator's responsiveness is in question.

Per TODO.md's own fallback rule: if time runs out, stop at the end of the last fully-checked section rather than half-building the next one. Sections 1-10 are done, including the highest-value Simulation panel — everything remaining (11-13) is verification/polish/prep, so scope should flex there first if the clock forces a choice.
