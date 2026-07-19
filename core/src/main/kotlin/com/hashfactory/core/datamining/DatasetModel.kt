package com.hashfactory.core.datamining

import com.hashfactory.core.datamining.FaceGenerator.CorruptionType

/**
 * A purchased dataset — a grid of tiles the player must classify as face or corruption.
 * Pure data; no state mutation here.
 */
data class Dataset(
    val id: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val tiles: List<Tile>,
    val cost: Double,
    val rewardPerFace: Double,
    val penaltyPerMistake: Double,
)

data class Tile(
    val pixels: PixelGrid,
    val isFace: Boolean,
    /** Only meaningful for corruption tiles — which variety is this? */
    val corruptionType: CorruptionType? = null,
)

// ── factory ─────────────────────────────────────────────────────────────

object DatasetFactory {

    fun generate(
        id: String,
        width: Int = 4,
        height: Int = 4,
        faceRatio: Double = 0.5,
        cost: Double = 10.0,
        rewardPerFace: Double = 50.0,
        penaltyPerMistake: Double = 25.0,
        baseSeed: Long = kotlin.random.Random.nextLong(),
    ): Dataset {
        val totalTiles = width * height
        val faceCount = (totalTiles * faceRatio).toInt().coerceIn(1, totalTiles - 1)
        val rng = kotlin.random.Random(baseSeed)

        // Pick which indices are faces
        val faceIndices = (0 until totalTiles).shuffled(rng).take(faceCount).toSet()

        // Distribute corruption types evenly across non-face tiles
        val corruptTypes = CorruptionType.entries
        var corruptNext = 0

        val tiles = (0 until totalTiles).map { i ->
            val pixelSeed = baseSeed + i * 100 + 1
            if (i in faceIndices) {
                Tile(
                    pixels = FaceGenerator.generateFace(pixelSeed),
                    isFace = true,
                )
            } else {
                val cType = corruptTypes[corruptNext % corruptTypes.size]
                corruptNext++
                Tile(
                    pixels = FaceGenerator.generateCorruption(cType, pixelSeed),
                    isFace = false,
                    corruptionType = cType,
                )
            }
        }

        return Dataset(
            id = id,
            gridWidth = width,
            gridHeight = height,
            tiles = tiles,
            cost = cost,
            rewardPerFace = rewardPerFace,
            penaltyPerMistake = penaltyPerMistake,
        )
    }
}

// ── scoring ─────────────────────────────────────────────────────────────

data class DatasetResult(
    val facesFound: Int,
    val mistakes: Int,
    val totalFaces: Int,
    val flopsEarned: Double,
    val isComplete: Boolean,
)

/** Calculate payout when the player finishes (or abandons) a dataset. */
fun scoreDataset(
    dataset: Dataset,
    clicks: Map<Int, Boolean>, // tileIndex -> player clicked it?
): DatasetResult {
    var facesFound = 0
    var mistakes = 0
    var totalFaces = 0

    for ((i, tile) in dataset.tiles.withIndex()) {
        if (!tile.isFace) {
            if (clicks[i] == true) mistakes++
        } else {
            totalFaces++
            if (clicks[i] == true) facesFound++
        }
    }

    val earned = (facesFound * dataset.rewardPerFace) - (mistakes * dataset.penaltyPerMistake)
    val complete = facesFound == totalFaces && mistakes == 0

    return DatasetResult(
        facesFound = facesFound,
        mistakes = mistakes,
        totalFaces = totalFaces,
        flopsEarned = earned.coerceAtLeast(0.0),
        isComplete = complete,
    )
}
