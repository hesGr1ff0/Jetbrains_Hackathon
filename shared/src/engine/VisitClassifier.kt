package wastetrack.engine

const val MIN_DROP_KG = 20.0

/**
 * Classifies a single zone visit from its entry/exit weight.
 * Order matters: the drive-through check (no meaningful drop) takes
 * priority over the full/partial distinction, per the exact rules in
 * CLAUDE.md §9.
 */
fun classifyVisit(
    entryWeightKg: Double,
    exitWeightKg: Double,
    tareKg: Double,
    toleranceKg: Double
): VisitClassification {
    val drop = entryWeightKg - exitWeightKg
    return when {
        drop < MIN_DROP_KG -> VisitClassification.NONE
        exitWeightKg in (tareKg - toleranceKg)..(tareKg + toleranceKg) -> VisitClassification.FULL
        else -> VisitClassification.PARTIAL
    }
}
