# WasteTrack — Handing Over

Status as of this document: **Sections 1-9 of TODO.md are complete** (engine, data, tests, state management, adaptive shell, shared components, all 5 driver screens, all 4 admin screens). Sections 10-13 (Simulation panel, cross-platform verification, localization/polish, demo prep) are not started. Read `CLAUDE.md` first — it's the full spec — then `TODO.md` for the section-by-section checklist. This file is the "what actually happened and what to watch for" layer on top of both.

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

### Driver screens (`shared/src/ui/screens/driver/`) + `DriverRoot.kt` — Section 8
All 5: `LiveShiftScreen`, `DropOffReminderScreen`, `DropOffResultScreen`, `ShiftSummaryScreen`, `DriverSettingsScreen`. `DriverRoot` auto-starts the selected vehicle's default scenario as a live stream on entry (`LaunchedEffect(state.selectedVehicleId)`) and provides simple button-row navigation between the 5 (no nav library, as instructed). Driver copy says "empty weight," never "tare weight," per §13.

### Admin screens (`shared/src/ui/screens/admin/`) + `AdminRoot.kt` — Section 9
All 4: `FleetOverviewScreen` (stat cards + filterable table + zone source citations panel folded into `LiveMapScreen` instead, per spec — no separate zones page), `LiveMapScreen`, `VehicleDetailScreen`, `AdminSettingsScreen`. Clicking a vehicle row in Fleet Overview calls `viewModel.selectVehicle(id)` and jumps to Vehicle Detail.

**Not built yet:** the Simulation panel (admin screen #4 in CLAUDE.md §13, Section 10 in TODO.md) — the scenario chips / weight slider / noise toggle / Run-Pause-Reset controls. This is explicitly called out in CLAUDE.md §15 as *the* highest-value feature for the "Working Product" score — don't let it slip.

---

## Assumptions and invented details to confirm against the real spec/judges' expectations

None of these are blocking, but none are specified in CLAUDE.md either — flag if they need to change:

1. **Fleet Overview's initial 6-vehicle mix** (`ui/DefaultScenarios.kt`): the 4 tricycles get the 4 canonical scenarios 1:1 (Kwame→Compliant, Yaw→Partial, Abena→Drive-through, Kofi→No-zone); the 2 trucks get No-zone and Drive-through respectively, *not* Compliant/Partial — because those routes' `loadKg` values are tuned to the tricycle tare (250kg) and would silently misclassify against a truck's 2500kg tare. Current mix: 1 Compliant, 1 Partial, 4 Flagged.
2. **Colors**: exact hex values in `VerdictBadge.kt`/`WasteTrackTheme.kt` are placeholders — CLAUDE.md only names the words (green/amber/red/blue), not shades.
3. **"Collected vs. delivered" on the driver Shift Summary screen**: interpreted as collected = Σ(entryWeightKg − tareKg) across visits, delivered = Σ(entryWeightKg − exitWeightKg). Not defined in CLAUDE.md.
4. **`Verdict.UNKNOWN`/grey doesn't exist**: CLAUDE.md §13 names a grey "Unknown" badge state, but the `Verdict` enum (§8) only has 4 values. Nothing currently needs it — will matter if a screen ever needs to show "no data yet" for a vehicle.
5. **Reading interval / replay speed**: 1000ms between readings in the route data itself (just timestamps, affects `dwellMs`); `SimulatedSource` playback delay used across screens is 300-350ms/reading (within the spec's stated 300-400ms range).
6. **Zone box half-width**: 0.0009° (~100m), a round-number approximation, per CLAUDE.md's explicit permission to skip geodesic precision.

---

## Environment quirks (this sandbox specifically — may not apply elsewhere)

- **No attached display.** `screencapture` fails with "could not create image from display." Compose Desktop windows run and exit cleanly (no crash), but there's no way to visually confirm rendering or simulate clicks/drags. Every verification so far has been: (a) unit/integration tests hitting the real engine and view model directly, (b) temporary `println` traces inside `LaunchedEffect`s to prove live state changes, removed after confirming, (c) forcing explicit `WindowState` sizes to crash-check both layout branches on boot. **Only the two default screens per layout (Fleet Overview, Live Shift) have actually been launched and confirmed crash-free** — the other 7 screens are reachable only via button clicks that couldn't be synthesized here. They're compile-checked and their underlying data is test-verified, but nobody has watched them actually render. If you get a real display, launch `./kotlin run --module jvm-app` and click through all 9 screens before trusting them fully.
- **Disk was at 99% capacity (~291MB free)** earlier in this session; recovered to ~6.4GB free at some point without direct action (unclear why — possibly unrelated system cleanup). Worth checking `df -h /` before Section 11's Android/web builds, since those may need to download SDK/toolchain material.
- **`build/` (289MB+) was untracked** — added to `.gitignore`. If you see it reappear in `git status`, that's expected; don't commit it.

---

## Git workflow

Remote is `origin` → `https://github.com/hesGr1ff0/Jetbrains_Hackathon.git`, branch `main`. The user asked mid-session for commits+pushes to be part of the normal workflow going forward (not just on request) — see the memory file `feedback_git_commits_in_process.md` if continuing in a fresh session. Commits so far, each after a green build+test:
1. `aa4018f` — initial snapshot through Section 4 (this one predates this session's own `git commit` calls — likely an editor/IDE auto-commit, not something explicitly invoked)
2. `6aeba71` — Section 5 (FleetViewModel) + the ShiftTracker Option-B fix
3. `c4b7c1d` — Sections 6-7 (App shell, WeightGauge, theme)
4. *(pending as of this doc)* — Sections 8-9 (all 9 screens, extended FleetViewModel)

`ios-app`'s IDE-generated diffs (`project.pbxproj`, a `.xcscheme` file) have been deliberately left unstaged every time — that module is out of scope per CLAUDE.md and its churn isn't ours to manage.

---

## What's next (Sections 10-13, in order)

1. **Section 10 — Simulation panel.** The single highest-value remaining feature. Scenario chips swap the active route fed into `FleetViewModel.start()`; the weight slider binds to `setManualKg()` (starts at Auto/null — this is called out explicitly in CLAUDE.md §15 as a common mistake); noise toggle binds to `setNoiseEnabled()`; Run/Pause/Reset control the stream. The demo script in CLAUDE.md §15 is the acceptance test: drive-through vehicle's detail chart shows a flat line through the zone → run "Compliant" from the panel, verdict lands Compliant → drag the slider up mid-zone-visit, verdict flips to Flagged live → drag back down, Compliant again.
2. **Section 11 — Cross-platform verification.** Neither Android nor web/wasm has been run yet in this session — only jvm-app. `adb` wasn't available in this environment (checked once, early on); Android verification will need an emulator or device. `wasm-app` hasn't been attempted at all. Fall back to desktop as the second confirmed platform if web doesn't cooperate, per CLAUDE.md's own fallback rule.
3. **Section 12 — Localization/polish.** `Strings.kt` with EN/TWI, wired via `CompositionLocalProvider`; dark/light consistency pass across all 9 screens (several screens currently use a hardcoded light-only `Surface` color in `FleetOverviewScreen`'s stat cards — worth checking against `state.darkTheme` when this section is tackled).
4. **Section 13 — Demo prep.** Screen recording, pitch rehearsal, backup video.

Per TODO.md's own fallback rule: if time runs out, stop at the end of the last fully-checked section rather than half-building the next one. Sections 1-7 are non-negotiable; scope should flex after that if the clock forces a choice — but Section 10 (Simulation panel) is explicitly the one thing CLAUDE.md says not to skip if there's any time at all, since it's the entire "Working Product" pitch.
