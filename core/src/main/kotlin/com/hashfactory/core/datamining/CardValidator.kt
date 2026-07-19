package com.hashfactory.core.datamining

import kotlin.random.Random

/**
 * A data profile card — a personnel/access record presented for validation.
 * Some profiles are clean (isValid=true), others have injectable red flags.
 */
data class Profile(
    val id: String,
    val fields: List<ProfileField>,
    val isValid: Boolean,
    /** Human-readable reason this profile is flagged, null if valid. */
    val flagReason: String? = null,
)

data class ProfileField(
    val label: String,
    val value: String,
    val isFlagged: Boolean = false, // rendered in red
)

data class CardBatch(
    val profiles: List<Profile>,
    val cost: Double,
    val rewardPerCorrect: Double,
    val penaltyPerWrong: Double,
)

// ── generator ───────────────────────────────────────────────────────────

object CardValidator {

    enum class FlagType(val description: String) {
        EXPIRED("Clearance expired"),
        RESTRICTED("Sector restricted"),
        ANOMALY("Temporal anomaly detected"),
        SUSPENDED("Operator suspended"),
        MISMATCH("ID/Name mismatch"),
        CORRUPT("Data corruption detected"),
    }

    private val firstNames = listOf("K.", "M.", "A.", "J.", "R.", "S.", "T.", "V.", "D.", "L.")
    private val lastNames = listOf(
        "Rasimov", "Chen", "Okonkwo", "Volkov", "Hatfield",
        "Nkosi", "Delacroix", "Park", "Santoro", "Ibrahim",
    )
    private val roles = listOf(
        "Network Engineer", "Data Analyst", "SysAdmin",
        "Security Auditor", "Field Technician", "Operator",
        "Archivist", "Logistics Coordinator",
    )
    private val sectors = (1..24).map { "$it" } + listOf("7-G", "7-H", "14-B", "14-C", "14-G", "21-A")
    private val clearanceLevels = listOf("Level 1", "Level 2", "Level 3", "Level 4")

    fun generateBatch(
        size: Int = 12,
        flagRatio: Double = 0.5,
        cost: Double = 15.0,
        rewardPerCorrect: Double = 30.0,
        penaltyPerWrong: Double = 15.0,
        seed: Long = Random.nextLong(),
    ): CardBatch {
        val rng = Random(seed)
        val flagCount = (size * flagRatio).toInt().coerceIn(1, size - 1)
        val flagIndices = (0 until size).shuffled(rng).take(flagCount).toSet()

        val profiles = (0 until size).map { i ->
            if (i in flagIndices) generateFlagged(rng, i) else generateClean(rng, i)
        }

        return CardBatch(
            profiles = profiles,
            cost = cost,
            rewardPerCorrect = rewardPerCorrect,
            penaltyPerWrong = penaltyPerWrong,
        )
    }

    // ── profile factories ────────────────────────────────────────────

    private fun generateClean(rng: Random, index: Int): Profile {
        val fname = firstNames[rng.nextInt(firstNames.size)]
        val lname = lastNames[rng.nextInt(lastNames.size)]
        val id = "%04X-%04X".format(rng.nextInt(0xFFFF), rng.nextInt(0xFFFF))
        return Profile(
            id = id,
            fields = listOf(
                ProfileField("ID", id),
                ProfileField("NAME", "$fname $lname"),
                ProfileField("ROLE", roles[rng.nextInt(roles.size)]),
                ProfileField("CLEARANCE", clearanceLevels[rng.nextInt(clearanceLevels.size)]),
                ProfileField("SECTOR", sectors[rng.nextInt(sectors.size)]),
                ProfileField("LOGIN", timestamp(rng)),
                ProfileField("STATUS", "ACTIVE"),
            ),
            isValid = true,
        )
    }

    private fun generateFlagged(rng: Random, index: Int): Profile {
        // Start with a clean profile, then inject 1-2 flags
        val clean = generateClean(rng, index)
        val flagType = FlagType.entries[rng.nextInt(FlagType.entries.size)]
        val fields = clean.fields.toMutableList()

        when (flagType) {
            FlagType.EXPIRED -> {
                fields[3] = ProfileField("CLEARANCE", "${fields[3].value} [EXPIRED]", isFlagged = true)
            }
            FlagType.RESTRICTED -> {
                fields[4] = ProfileField("SECTOR", "${fields[4].value} [RESTRICTED]", isFlagged = true)
            }
            FlagType.ANOMALY -> {
                val h = 25 + rng.nextInt(10)  // impossible hour
                fields[5] = ProfileField("LOGIN", "20${52 + rng.nextInt(20)}-${(13 + rng.nextInt(5))}-${(32 + rng.nextInt(2))} ${"%02d".format(h)}:00", isFlagged = true)
            }
            FlagType.SUSPENDED -> {
                fields[6] = ProfileField("STATUS", "SUSPENDED", isFlagged = true)
            }
            FlagType.MISMATCH -> {
                fields[1] = ProfileField("NAME", "NULL_REF", isFlagged = true)
            }
            FlagType.CORRUPT -> {
                val corruptIdx = rng.nextInt(fields.size)
                fields[corruptIdx] = ProfileField(
                    fields[corruptIdx].label,
                    "0x${"%08X".format(rng.nextInt())}",
                    isFlagged = true,
                )
            }
        }

        return Profile(
            id = clean.id,
            fields = fields,
            isValid = false,
            flagReason = flagType.description,
        )
    }

    private fun timestamp(rng: Random): String {
        val month = "%02d".format(1 + rng.nextInt(12))
        val day = "%02d".format(1 + rng.nextInt(28))
        val hour = "%02d".format(rng.nextInt(24))
        val min = "%02d".format(rng.nextInt(60))
        return "2026-$month-$day $hour:$min"
    }
}
