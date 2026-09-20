# WasteTrack — Build Roadmap

Work through sections in order. Do not start a section until the previous one's checklist is fully checked off. Each section states its "done" condition — verify it before moving on. Refer to `CLAUDE.md` in the project root for full rules, data, and exact logic; this file is the execution checklist only.

---

## Section 0 — Toolchain Verification

- [x] Confirm project root contains `project.yaml`, not `build.gradle.kts` (Amper, not Gradle)
- [x] Run `./kotlin run --module jvm-app` — desktop template launches
- [x] Run `./kotlin run --module android-app` — emulator or device shows template (create/boot an emulator via Device Manager first if none connected)
- [x] Run `./kotlin run --module web-app` (or `wasm-app` — confirm exact folder name) — browser tab shows template
- [x] Confirm `shared/src` folder exists and locate its default `@Composable` screen file

**Done when:** all three run commands succeed on the unmodified template.

---

## Section 1 — Engine: Models

- [x] Create `shared/src/engine/Models.kt`
- [x] Define `Coordinate(lat, lng)`
- [x] Define `Reading(coordinate, loadKg, timestampMs)`
- [x] Define `Zone(id, name, boundary, source)`
- [x] Define `Vehicle(id, driverName, type, tareKg, toleranceKg)`
- [x] Define `VehicleType` enum (`TRICYCLE`, `TRUCK`)
- [x] Define `ZoneVisit(zone, entryWeightKg, exitWeightKg, classification, dwellMs)`
- [x] Define `VisitClassification` enum (`FULL`, `PARTIAL`, `NONE`)
- [x] Define `Verdict` enum (`IN_PROGRESS`, `COMPLIANT`, `PARTIAL`, `FLAGGED`)
- [x] Define `ShiftResult(vehicle, visits, verdict, reason)`
- [x] Define `ReadingSource` interface with `stream(): Flow<Reading>`
- [x] Verify: file compiles with no platform-specific imports

**Done when:** `./kotlin run --module jvm-app` still succeeds with this file added.

---

## Section 2 — Engine: Geometry and Classification Logic

- [x] Create `shared/src/engine/PointInPolygon.kt` — hand-written ray-casting, no library
- [x] Manually verify point-in-polygon against one known-inside and one known-outside point
- [x] Create `shared/src/engine/VisitClassifier.kt` implementing the exact rules:
  - [x] No zone visited in shift → Flagged, "never reached an authorized zone"
  - [x] `entryWeightKg - exitWeightKg` < 20kg inside a zone → `NONE`
  - [x] `exitWeightKg` within `tareKg ± toleranceKg` → `FULL`
  - [x] Otherwise → `PARTIAL`
- [x] Create `shared/src/engine/ShiftEvaluator.kt`:
  - [x] Any `NONE` visit, or zero visits → shift verdict `FLAGGED`
  - [x] All visits `FULL` → shift verdict `COMPLIANT`
  - [x] Otherwise → shift verdict `PARTIAL`
- [x] Create `shared/src/engine/SimulatedSource.kt`: `ReadingSource` implementation emitting a `List<Reading>` on a `delay()`-based `Flow`

**Done when:** engine package has zero Compose imports and zero platform-specific imports — deleting the UI folder entirely should still leave this package compiling.

---

## Section 3 — Engine: Tests (write before trusting any AI-generated engine code)

- [x] Create `shared/test/EngineTest.kt`
- [x] Test: compliant scenario route → verdict `COMPLIANT`
- [x] Test: no-zone scenario route → verdict `FLAGGED`
- [x] Test: drive-through scenario route → verdict `FLAGGED`, visit classification `NONE`
- [x] Test: partial scenario route → verdict `PARTIAL`
- [x] Run all four tests, confirm pass

**Done when:** all four tests pass. This is the checkpoint — do not proceed to data/UI work on top of an engine whose tests are failing or missing.

---

## Section 4 — Data: Zones, Vehicles, Scenarios

- [x] Create `shared/src/data/Zones.kt`
- [x] Add `squareZone()` helper generating a ~200m square around a lat/lng center
- [x] Add all 5 real zones (Achimota, Teshie, Kpone, Ashaiman, Pantang) with correct coordinates and the directive citation as `source`
- [x] Create `shared/src/data/Vehicles.kt`
- [x] Add all 6 vehicles (4 tricycles, 2 trucks) with plate IDs, driver names, plausible tare weights
- [x] Create `shared/src/data/Scenarios.kt`
- [x] Build **compliant** route: enters a zone, weight drops to within tare ± tolerance, exits (~25–40 points)
- [x] Build **no-zone** route: never enters any zone polygon, weight stays loaded
- [x] Build **drive-through** route: enters a zone, weight stays constant/loaded throughout, exits — reuse the compliant route's coordinates, only change `loadKg`
- [x] Build **partial** route: enters a zone, weight drops but stops well short of tare — reuse coordinates, edit `loadKg`
- [x] Re-run Section 3 tests against this real data, confirm all four still pass

**Done when:** all 4 scenario routes exist as Kotlin constants and Section 3's tests pass against them (not placeholder data).

---

## Section 5 — State Management

- [x] Create `shared/src/ui/FleetViewModel.kt`
- [x] Define `FleetState` data class (holds current readings, current vehicle, verdict, manualKg override, noise toggle state)
- [x] Implement single `MutableStateFlow<FleetState>` — no DI framework
- [x] Implement `start(scope)` collecting from `ReadingSource.stream()`, updating state per reading
- [x] Implement provisional-verdict computation: recompute verdict on every reading (see note below on the exact `shiftEnded` semantics)
- [x] Implement `manualKg` override: if non-null, replace incoming reading's `loadKg` before it reaches the classifier; `null` = Auto (follow route)
- [x] Implement noise toggle: ±0.5kg random jitter applied to readings when enabled

**Done when:** manually driving state changes (in a scratch test or temporary UI) shows the verdict updating live.

---

## Section 6 — Adaptive Layout Shell

- [x] Create/replace `shared/src/App.kt` as the single root `@Composable`
- [x] Use `BoxWithConstraints` to branch: width > 700dp → admin layout, else → driver layout
- [x] Construct `FleetViewModel` with `remember` inside `App()`
- [x] Verify on desktop: resizing the window switches between the two layouts live

**Done when:** the same `App()` composable, unmodified, is what both `MainActivity.kt` (Android) and `main.kt` (web) display.

---

## Section 7 — Shared UI Components

- [x] `shared/src/ui/components/VerdictBadge.kt` — color + text for each `Verdict` (never color alone: green=Compliant, amber=Partial, red=Flagged, blue=In progress, grey=Unknown)
- [x] `shared/src/ui/components/WeightGauge.kt` — current load vs. empty weight
- [x] `shared/src/ui/components/ZoneCanvas.kt` — draws a zone square, a route polyline, and a moving dot; project lat/lng to screen x/y with y-axis inverted (latitude increases upward, screen y increases downward)
- [x] `shared/src/ui/theme/` — color tokens, typography, dark/light variants

**Done when:** each component renders correctly in isolation on desktop (a scratch preview screen is fine for checking this).

---

## Section 8 — Driver Screens (Android, narrow layout)

- [x] `screens/driver/LiveShiftScreen.kt` — verdict badge, current load vs. empty weight, in/out-of-zone status, `ZoneCanvas` with route + dot
- [x] `screens/driver/DropOffReminderScreen.kt` — nudge state: nearest zone name, distance, framed as reminder not penalty
- [x] `screens/driver/DropOffResultScreen.kt` — entry weight, exit weight, change, classification badge, reason
- [x] `screens/driver/ShiftSummaryScreen.kt` — end-of-shift verdict, collected vs. delivered totals, visit list
- [x] `screens/driver/DriverSettingsScreen.kt` — language toggle, theme toggle, tare weight display, reminder toggle
- [x] Use "empty weight" in all driver-facing copy, never "tare weight"
- [x] Wire navigation between these 5 screens (simple state-based nav is enough — no navigation library needed)

**Done when:** all 5 driver screens are reachable and show live data from `FleetViewModel`, verified on desktop with the narrow-layout branch forced on.

---

## Section 9 — Admin Screens (Web, wide layout)

- [x] `screens/admin/FleetOverviewScreen.kt` — stat cards (total/compliant/partial/flagged), vehicle table (ID, driver, type, verdict badge, reason, current load), filter chips (All/Flagged/Partial)
- [x] `screens/admin/LiveMapScreen.kt` — zones as fixed-size markers (not to scale) on a city-wide layout; selecting a zone/vehicle opens a to-scale inset with the 200m square and live trail; zone source citations shown in a side panel (no separate zones page)
- [x] `screens/admin/VehicleDetailScreen.kt` — header (vehicle ID, driver, verdict badge), zone-visits table, load-over-time chart with dashed tare line and shaded tolerance band
- [x] `screens/admin/AdminSettingsScreen.kt` — language toggle, theme toggle
- [x] Wire navigation between these 4 screens

**Done when:** all 4 admin screens are reachable and show live/real data (not hardcoded table rows), verified on desktop with the wide-layout branch.

---

## Section 10 — Simulation Panel (highest-value feature for Working Product score)

- [ ] `screens/admin/SimulationPanelScreen.kt`
- [ ] Scenario chips: Compliant / No zone / Drive-through / Partial — selecting one swaps the active `SimulatedSource` route and restarts
- [ ] Weight slider (250–400kg range) bound to `manualKg` in `FleetViewModel`; starts in Auto (`null`) state
- [ ] Sensor-noise toggle bound to the ±0.5kg jitter in `FleetViewModel`
- [ ] Run / Pause / Reset controls
- [ ] Label the panel "Simulation" visibly in the UI
- [ ] Verify: dragging the slider while the vehicle is inside a zone changes the verdict live, in front of you, not just at shift end

**Done when:** you can run the full demo script from the PRD (§15) end to end: open fleet overview → drive-through vehicle's detail shows a flat weight line through the zone → open Simulation, run "Compliant," verdict lands Compliant → drag slider up mid-zone-visit, verdict flips to Flagged → drag back down, returns to Compliant.

---

## Section 11 — Cross-Platform Verification

- [ ] Run `./kotlin run --module android-app` — confirm both driver and admin layouts render correctly (test admin by forcing a wide window on a tablet emulator, or trust the desktop verification if time is short)
- [ ] Run `./kotlin run --module web-app` — confirm both layouts render; resize the browser window to confirm the adaptive breakpoint works live
- [ ] If web is not cooperating within its allotted time, fall back to desktop as the confirmed second platform and note this in the pitch
- [ ] Fix only genuine breakages found here — do not add features during this section

**Done when:** the app is confirmed working, without added scope, on Android and (web or desktop).

---

## Section 12 — Localization and Polish

- [ ] Create `shared/src/ui/Strings.kt` — a `Strings` data class with ~15 key labels (verdict names, screen titles, button labels)
- [ ] Provide `EN` and `TWI` instances
- [ ] Wrap `App()` in a `CompositionLocalProvider` supplying the active `Strings`
- [ ] Wire the language toggle in both settings screens to switch it
- [ ] Confirm dark/light theme toggle works and looks consistent across all 9 screens

**Done when:** switching language updates visible text; switching theme updates all screens without a broken/unstyled element.

---

## Section 13 — Demo Preparation

- [ ] Screen-record a full run-through of the demo script (§15 in the PRD) on the most reliable platform
- [ ] Save the recording somewhere accessible for the pitch, independent of a live internet/build dependency
- [ ] Rehearse the three-sentence framing: problem → insight → what was built today with AI assistance
- [ ] Confirm the backup video plays on the actual presentation machine before the pitch slot
- [ ] Prepare answers for the edge-case questions in PRD §16 (illegal dump then empty visit; sensor drift; multiple short visits)

**Done when:** the backup video exists, plays, and the pitch has been said aloud at least once, start to finish.

---

## Fallback rule

If time runs out mid-section, stop at the end of the last fully checked-off section rather than leaving a later one half-built. A complete Section 9 with Section 10 untouched outscores a half-built Section 10 with broken navigation. Sections 1–7 (engine, data, tests, layout shell, components) are non-negotiable — everything after Section 7 is where scope should flex if the clock forces a choice.
