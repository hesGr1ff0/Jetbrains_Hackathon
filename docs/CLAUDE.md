# WasteTrack — Project Context & PRD

**Read this entire file before writing or changing any code.** This is the complete specification for WasteTrack, a Kotlin Multiplatform hackathon project. It covers the problem, the data, the architecture, every screen, and the exact logic. Build in the order given in §14.

---

## 1. Problem

Ghana's waste-collection system relies on trucks and tricycles ("aboboyaa") to move waste to authorized transfer stations. A July 2026 Presidential directive reopened six Zoomlion transfer stations specifically for tricycle drop-off, because government wants to formalize how independent aboboyaa operators, who currently work outside the formal system but still route through Zoomlion, participate in compliant disposal. An estimated 600–700 tons of waste a day is diverted away from authorized sites in the Accra area.

Government and operators (Zoomlion, Jospong) can mandate *where* waste should go. What they lack is per-vehicle evidence that it *actually went there*.

## 2. The insight

GPS proves presence, not disposal. A driver can defeat a GPS-only compliance system by physically driving through an authorized zone without unloading — a "drive-through" trick. Comparing cargo weight at zone exit against the vehicle's known empty (tare) weight turns mere presence into evidence of an actual drop-off. This is the single idea the entire project demonstrates, and it should be stated in the first minute of any pitch.

## 3. What WasteTrack is

A compliance detection layer, not a deployed system. It ingests a location signal and a weight signal per vehicle, checks each authorized-zone visit against tare weight, and produces a per-visit classification and an end-of-shift verdict.

- **Admin side (web):** used by a government or Zoomlion operations officer to monitor a fleet.
- **Driver side (Android):** used by the vehicle operator or field supervisor, showing their own live status and shift result.
- Both are the same Compose Multiplatform codebase, differing only in which layout renders at which screen width — not two separate apps.

## 4. What it is not

Not a deployed product. Not a claim that real sensors are wired up today. Not a driver-facing gig-economy platform ("Uber for refuse") — that invites scope and incentive questions outside today's build. Not a penalty/enforcement tool in its framing — the driver-facing language uses "reminder" and "nudge," not "violation" or a compliance score, to avoid an adversarial framing that a real deployment would need to handle carefully.

---

## 5. Hardware framing (for the pitch, not the build)

No single commercial device measures both location and weight. The pattern used today: a GPS tracker (transmitter) paired with an axle or suspension load sensor (Bluetooth or wired), calibrated per vehicle after install. This is realistic on Zoomlion trucks now. On aboboyaa tricycles, the blocker is calibration cost and the lack of a single fleet owner to mandate installs across many independent operators — not sensor technology itself. Say "a location signal and a weight signal," not a specific part number, when asked. The app's data-source layer is built so any device fitting that description plugs in without changing the engine (see §8, `ReadingSource`).

---

## 6. Hard constraints for this build

- **Build tool is Amper, not Gradle.** No `build.gradle.kts` exists. Configuration is in `project.yaml` and per-module `module.yaml` files. Never generate Gradle syntax or Gradle commands. If a dependency must be added, confirm the exact `module.yaml` syntax before editing — a malformed YAML file breaks every target's build.
- **Run commands**, from the project root:
  - `./kotlin run --module jvm-app` — desktop, fastest loop, use for all iteration
  - `./kotlin run --module android-app` — Android, needs a booted emulator or connected device
  - `./kotlin run --module web-app` (confirm exact module folder name — may be `wasm-app`) — web
- **Two platforms required for submission: Android and web.** Desktop is the development loop and fallback second platform if web cannot be finished.
- **All engine logic lives in `shared/src` (commonMain) with zero platform-specific imports.** If code in `shared/src` needs an Android-only or browser-only API, it belongs in `src@android` / `src@wasmJs` instead, or the design is wrong.
- **All data is simulated**, generated in-app from hardcoded Kotlin constants, not fetched from any network or read from a file. State this in the pitch before a judge asks.
- **No file I/O for data.** Zones and scenario routes are Kotlin source constants in `commonMain`, not JSON resources — this sidesteps a known resource-loading fragility on the web target.
- **No dependency injection framework, no networking library, no persistence.** A single `StateFlow`-backed view model is sufficient.

---

## 7. Module structure

Amper's folder names map to the standard KMP roles as follows:

| Role | Folder | Contents |
|---|---|---|
| Shared logic + shared UI | `shared/src` | Engine, data constants, Composable screens, view model |
| Shared tests | `shared/test` | Scenario tests |
| Android entry point | `android-app/src` | `MainActivity.kt` only — a few lines |
| Web entry point | `web-app/src` (or `wasm-app/src`) | `main.kt` only — a few lines |
| Desktop entry point (dev loop / fallback) | `jvm-app/src` | `Main.kt` only — a few lines |
| Not used | `ios-app`, `src@ios`, `test@ios` | Ignore. Do not delete. |
| Platform-specific shared code (should stay empty) | `src@android`, `src@jvm`, `src@wasmJs` | Empty if the engine is truly pure Kotlin — a good pitch point |

Suggested files inside `shared/src`:

```
shared/src/
  engine/
    Models.kt          Coordinate, Reading, Zone, Vehicle, ZoneVisit, ShiftResult, Verdict
    PointInPolygon.kt
    VisitClassifier.kt
    ShiftEvaluator.kt
    ReadingSource.kt    interface + SimulatedSource implementation
  data/
    Zones.kt            5 real zone constants
    Scenarios.kt         4 scenario route constants
    Vehicles.kt          vehicle + driver name constants
  ui/
    theme/               colors, type
    FleetViewModel.kt
    screens/             one file per screen, listed in §11–12
    components/          VerdictBadge, WeightGauge, ZoneCanvas, etc.
shared/test/
  EngineTest.kt          one test per scenario
```

---

## 8. Data models

```kotlin
package wastetrack.engine

data class Coordinate(val lat: Double, val lng: Double)

data class Reading(
    val coordinate: Coordinate,
    val loadKg: Double,
    val timestampMs: Long
)

data class Zone(
    val id: String,
    val name: String,
    val boundary: List<Coordinate>, // polygon corners, closed
    val source: String
)

data class Vehicle(
    val id: String,        // e.g. Ghana plate "M-24-GT-1842"
    val driverName: String,
    val type: VehicleType,
    val tareKg: Double,     // empty weight
    val toleranceKg: Double = 3.0
)

enum class VehicleType { TRICYCLE, TRUCK }

data class ZoneVisit(
    val zone: Zone,
    val entryWeightKg: Double,
    val exitWeightKg: Double,
    val classification: VisitClassification,
    val dwellMs: Long
)

enum class VisitClassification { FULL, PARTIAL, NONE }

enum class Verdict { IN_PROGRESS, COMPLIANT, PARTIAL, FLAGGED }

data class ShiftResult(
    val vehicle: Vehicle,
    val visits: List<ZoneVisit>,
    val verdict: Verdict,
    val reason: String
)

interface ReadingSource {
    fun stream(): kotlinx.coroutines.flow.Flow<Reading>
}
```

**Coordinate order warning:** always use named arguments (`Coordinate(lat = ..., lng = ...)`). GeoJSON-style route data is `[lng, lat]`; human-written coordinates are usually spoken as `lat, lng`. Mixing the two produces a route that looks wrong with no compile error.

---

## 9. Classification rules — exact

Given a vehicle's `tareKg` and `toleranceKg` (default ±3kg), and a minimum-drop threshold of 20kg:

1. **No zone visited at all during the shift** → shift verdict **Flagged**, reason: "never reached an authorized zone."
2. **Visited a zone, but `entryWeightKg - exitWeightKg` < 20kg** → visit classification **NONE** (the drive-through trick: presence without unloading).
3. **`exitWeightKg` within `tareKg ± toleranceKg`** → visit classification **FULL**.
4. **Otherwise** (weight dropped meaningfully but exit weight still well above tare) → visit classification **PARTIAL**.

**Shift verdict**, derived from all visits in the shift:
- Any visit classified NONE, or zero visits → **Flagged**
- All visits FULL, and at least one visit occurred → **Compliant**
- Otherwise (mix including at least one PARTIAL, none NONE) → **Partial**

---

## 10. Zone data (real, researched, use as-is)

Source for all five: the 11 July 2026 Presidential directive reopening Zoomlion transfer stations for tricycle drop-off.

| # | Station | Lat | Lng |
|---|---|---|---|
| 1 | Achimota Transfer Station (ZoomPak) | 5.6226 | -0.2283 |
| 2 | Teshie Transfer Station | 5.5832 | -0.1046 |
| 3 | Kpone Transfer Station | 5.7031 | 0.0287 |
| 4 | Ashaiman Transfer Station | 5.6830 | -0.0488 |
| 5 | Pantang Transfer Station | 5.7087 | -0.1956 |

```kotlin
val ACCRA_ZONES = listOf(
    squareZone("z1", "Achimota Transfer Station (ZoomPak)", lat = 5.6226, lng = -0.2283),
    squareZone("z2", "Teshie Transfer Station",             lat = 5.5832, lng = -0.1046),
    squareZone("z3", "Kpone Transfer Station",              lat = 5.7031, lng = 0.0287),
    squareZone("z4", "Ashaiman Transfer Station",           lat = 5.6830, lng = -0.0488),
    squareZone("z5", "Pantang Transfer Station",            lat = 5.7087, lng = -0.1956),
)
```

`squareZone` generates a ~200m square centered on the point (approximate degree offsets are fine at this scale — do not spend time on precise geodesic math) and fills `source` with the directive citation from §10's header.

## 11. Vehicle data

| Vehicle ID | Driver | Type |
|---|---|---|
| M-24-GT-1842 | Kwame Mensah | Tricycle |
| M-23-GT-0977 | Yaw Boateng | Tricycle |
| M-24-GT-3310 | Abena Owusu | Tricycle |
| M-22-GT-7165 | Kofi Asare | Tricycle |
| GT-5109-23 | Samuel Tetteh | Truck |
| GT-2740-24 | Ibrahim Alhassan | Truck |

Use plausible tare weights (e.g. ~250kg for tricycles, ~2500kg for trucks) since exact figures were not sourced.

## 12. Scenario routes — four, all needed

Each is a `List<Reading>`, timestamped, roughly 25–40 points, replayable at ~300–400ms per point:

1. **Compliant:** route enters a zone, weight drops from a loaded value to within tare ± tolerance, exits. Verdict: Compliant.
2. **No zone:** route never enters any zone polygon. Weight stays loaded throughout. Verdict: Flagged.
3. **Drive-through:** route enters a zone, weight stays constant and loaded the entire time inside, exits. Verdict: Flagged. (This is the flagship scenario — the one that proves the insight.)
4. **Partial:** route enters a zone, weight drops but stops well short of tare (e.g. drops by half). Verdict: Partial.

Scenarios 3 and 4 can be generated as edits of scenario 1's `loadKg` values, holding the coordinates identical, since the underlying route is the same physical path — only the weight signal differs. This is worth pointing out in the pitch: same GPS trail, different verdict, purely from the weight signal.

---

## 13. Screens

### Admin (web, wide layout, ≥700dp)

1. **Fleet overview** (home). Stat cards: total vehicles, compliant, partial, flagged. A vehicle table: vehicle ID, driver, type, verdict badge, reason, current load. Filter chips: All / Flagged / Partial.
2. **Live map.** Zones shown as fixed-size markers (not to scale, city-wide view — the five real zones are 14–28km apart, so a to-scale 200m square would be invisible at that zoom). Selecting a zone or vehicle shows an inset zoomed view with the 200m square drawn to scale and the live route/trail on top. Zone list panel folded into this screen (no separate zones page), each with its source citation.
3. **Vehicle detail.** Header with vehicle ID, driver, verdict badge. Zone-visits table (zone, entry kg, exit kg, change kg, classification). A load-over-time chart with a dashed tare line and a shaded tolerance band.
4. **Simulation panel** (demo control, not a real feature — see §15). Scenario chips (Compliant / No zone / Drive-through / Partial), a weight slider (manual override), a sensor-noise toggle, Run/Reset/Pause.
5. **Settings.** Language (EN/Twi) and theme (dark/light) toggles.

### Driver (Android, narrow layout, <700dp)

1. **Live shift** (home). Verdict badge ("In progress" while active), current load vs. empty weight, in/out-of-zone status line, the same Canvas map (zoomed to the relevant zone) with route and moving dot.
2. **Drop-off reminder.** Nudge state: nearest zone name, distance, framed as a reminder, not a penalty.
3. **Drop-off result.** Shown after a zone visit: entry weight, exit weight, change, classification badge, reason.
4. **Shift summary.** End-of-shift verdict, collected vs. delivered totals, list of visits.
5. **Settings.** Language, theme, vehicle constants (tare weight), reminder toggle.

Use "empty weight," not "tare weight," in driver-facing copy — a driver should not need the technical term. Color always carries a verdict word alongside it, never color alone (Compliant = green, Partial = amber, Flagged = red, In progress = blue, Unknown = grey).

---

## 14. Build order

1. Models (§8)
2. Point-in-polygon (hand-rollable ray-casting, ~20 lines, no library — verify manually against one known-inside point before trusting it)
3. Classifier + shift evaluator (§9)
4. Zone data (§10), vehicle data (§11)
5. `shared` compiles cleanly via `./kotlin run --module jvm-app`, no UI yet
6. `EngineTest.kt`: one test per scenario in §12 — write these before or alongside the route data, they are the guardrail for any AI-generated engine code
7. All four scenario routes (§12)
8. `ReadingSource` + `SimulatedSource` (delay-based Flow replay, per §8)
9. `FleetViewModel`: single `StateFlow<FleetState>`, no DI framework
10. One adaptive `App()` composable using `BoxWithConstraints` to pick admin vs. driver layout by width
11. Driver screens 1–3 (§13) — build these before admin, they're simpler
12. Admin screens 1–3 (§13)
13. Simulation panel (§13 admin #4, §15) — the interactive weight slider, this is the single highest-value feature for the "Working Product" judging category
14. Settings screens, dark mode, localization strings (15 or so strings is enough for the bonus)
15. Confirm on Android (`./kotlin run --module android-app`)
16. Confirm on web (`./kotlin run --module web-app`), fall back to desktop as the second platform if web is not cooperating
17. Backup screen recording of the working app
18. Pitch rehearsal

If time runs out at any point, stop at the end of the last fully completed numbered step above rather than leaving a later step half-built — a smaller complete set of screens outscores a larger broken one.

---

## 15. The simulation panel — how it works and why

This exists only for the demo; a real deployment would receive readings from actual hardware, not a control panel. It is what proves the engine is live rather than a pre-recorded animation, which is the biggest risk to the "Working Product" score.

- Scenario chips swap which `List<Reading>` feeds the `SimulatedSource`.
- The weight slider sets a `manualKg: Double?` value in the view model. Before each reading reaches the classifier, if `manualKg` is non-null, the reading's `loadKg` is overridden with it: `reading.copy(loadKg = manualKg)`. `null` means "Auto" (follow the route's own values) — this must be the slider's starting state, or every visit will read as a zero-change "no drop-off."
- Compute a **provisional verdict** on every incoming reading (`tracker.result(shiftEnded = true)`), not only at the route's natural end, or the slider will appear to do nothing while a judge is dragging it mid-route.
- Noise toggle adds ±0.5kg jitter to demonstrate the tolerance band absorbing sensor imprecision without producing false partials.
- Label this panel "Simulation" visibly in the UI — this makes the honesty about simulated data visible rather than something that must be explained verbally.

**Demo script:** open on Fleet overview → click into the drive-through vehicle, show the flat weight line through the zone on its detail chart → open Simulation, run "Drops off fully," verdict lands Compliant → hand the slider to a judge, they drag it up while the vehicle is inside the zone, verdict flips to Flagged live, then back down → Compliant again. Same GPS trail, different weight, different verdict — that sequence is the entire pitch.

---

## 16. Known edge cases and gaming vectors (for Q&A, not necessarily built)

- **Illegal dump then empty visit:** a driver dumps illegally elsewhere, then visits a zone already empty — reads as Compliant. Out of scope today; the honest answer if asked is that this requires per-cycle verification (e.g. photo or a second weigh-in), not solvable by weight-vs-tare alone.
- **Sensor drift/miscalibration:** handled by the tolerance band, not a fix — say this plainly if asked, since tolerance is a mitigation, not a guarantee.
- **Multiple short visits to avoid a single full-drop threshold:** not handled today; flag as a stretch consideration only if asked.

---

## 17. Pitch framing

Problem is real and government-acknowledged. Insight is GPS-plus-weight, not GPS alone. Live demo is the slider flipping the verdict in front of the judge. Technical point: one shared engine (`shared/src`, zero platform imports) running on two platforms, unit-tested, with the sensor swappable behind `ReadingSource`. State plainly that data is simulated and that the web prototype/research predates today, while the KMP engine and app were built today, with AI-assisted coding used throughout — say this rather than let it be discovered.
