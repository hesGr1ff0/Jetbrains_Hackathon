package wastetrack.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CLAUDE.md §14 step 14: "~15 or so strings is enough for the bonus" — this
 * covers the most visible labels (nav, verdict badges, key buttons,
 * settings), not every string in the app. Reasons/banners/chart copy stay
 * English-only; that's an intentional scope line, not an oversight.
 */
data class Strings(
    val appName: String,
    val tagline: String,
    val verdictCompliant: String,
    val verdictPartial: String,
    val verdictFlagged: String,
    val verdictInProgress: String,
    val navFleetOverview: String,
    val navLiveMap: String,
    val navVehicleDetail: String,
    val navSimulation: String,
    val navSettings: String,
    val navShift: String,
    val navActivity: String,
    val runShift: String,
    val pause: String,
    val reset: String,
    val language: String,
    val theme: String,
    val english: String,
    val twi: String,
    val light: String,
    val dark: String,
)

val EN_STRINGS = Strings(
    appName = "WasteTrack",
    tagline = "Disposal compliance",
    verdictCompliant = "Compliant",
    verdictPartial = "Partial",
    verdictFlagged = "Flagged",
    verdictInProgress = "In progress",
    navFleetOverview = "Fleet overview",
    navLiveMap = "Live map",
    navVehicleDetail = "Vehicle detail",
    navSimulation = "Simulation",
    navSettings = "Settings",
    navShift = "Shift",
    navActivity = "Activity",
    runShift = "Run shift",
    pause = "Pause",
    reset = "Reset",
    language = "Language",
    theme = "Theme",
    english = "English",
    twi = "Twi",
    light = "Light",
    dark = "Dark",
)

// ASSUMPTION / FLAG FOR REVIEW: these are a good-faith attempt, not verified
// by a native Twi speaker. Confirm with someone fluent before using live in
// front of Ghanaian judges — a wrong or awkward translation on screen would
// undercut the pitch rather than help it. See handing_over.md.
//
// Also deliberately ASCII-only: proper Twi orthography uses ɛ (open e) and
// ɔ (open o), but those rendered as tofu boxes (missing-glyph placeholders)
// in this environment's browser font — confirmed via real browser testing,
// not assumed. Since the actual presentation device's font support is
// unknown, these substitute the common e/o simplification long used in
// informal digital Twi (SMS, casual typing) where ɛ/ɔ aren't easily
// reachable on a keyboard, trading orthographic precision for guaranteed
// legibility. If the demo device is confirmed to render ɛ/ɔ correctly,
// restore them for accuracy.
val TWI_STRINGS = Strings(
    appName = "WasteTrack",
    tagline = "Nsee Hwe",
    verdictCompliant = "Eye",
    verdictPartial = "Efa",
    verdictFlagged = "Enye",
    verdictInProgress = "Ereko So",
    navFleetOverview = "Kar Nyinaa",
    navLiveMap = "Mapa",
    navVehicleDetail = "Kar No Ho Nsem",
    navSimulation = "Sohwe",
    navSettings = "Nhyehye",
    navShift = "Adwuma",
    navActivity = "Nnwuma",
    runShift = "Hye Ase",
    pause = "Gyae",
    reset = "Fi Ase",
    language = "Kasa",
    theme = "Ahosuo",
    english = "Borofo Kasa",
    twi = "Twi",
    light = "Hann",
    dark = "Sum",
)

val LocalStrings = staticCompositionLocalOf { EN_STRINGS }

fun stringsFor(language: Language): Strings = when (language) {
    Language.EN -> EN_STRINGS
    Language.TWI -> TWI_STRINGS
}
