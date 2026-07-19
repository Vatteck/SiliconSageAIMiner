package com.hashfactory.core.datamining

import kotlin.random.Random

/**
 * Procedural 12×12 pixel-art face generation. Produces grids of booleans:
 * true = filled pixel (rendered as green on terminal), false = dark.
 *
 * Corruption comes in three varieties so the player can't just learn
 * one "not-a-face" pattern:
 *   STATIC   — sparse random noise
 *   SCRAMBLE — a real face with 30–50 % of pixels flipped
 *   EMPTY    — nearly blank (1–4 scattered pixels)
 *
 * Zero external dependencies — pure Kotlin math, testable in :core.
 */
object FaceGenerator {

    const val SIZE = 12

    enum class CorruptionType { STATIC, SCRAMBLE, EMPTY }

    // ── public API ──────────────────────────────────────────────────────

    /** Deterministic face at 12×12. */
    fun generateFace(seed: Long = Random.nextLong()): PixelGrid {
        val rng = Random(seed)
        val grid = PixelGrid(SIZE)

        // Face background fill: rows 3–9, cols 2–10
        for (y in 3..9) {
            for (x in 2..10) {
                grid[x, y] = true
            }
        }

        // Round corners: top-left, top-right, bottom-left, bottom-right
        grid[2, 3] = false; grid[10, 3] = false
        grid[2, 4] = false; grid[10, 4] = false
        grid[2, 8] = false; grid[10, 8] = false
        grid[2, 9] = false; grid[10, 9] = false

        // Hair line — optional top row above the face
        val hairY = 2 + rng.nextInt(1) // row 2 sometimes
        if (rng.nextDouble() < 0.5) {
            for (x in 3..9) grid[x, hairY] = true
        }

        // Eyes — two punch-out regions
        val eyeY = 4 + rng.nextInt(1) // row 4 or 5
        val eyeSize = 1 + rng.nextInt(1) // 1 or 2 wide
        // Left eye
        for (dx in 0 until eyeSize) {
            grid[3 + dx, eyeY] = false
            if (eyeSize > 1) grid[3 + dx, eyeY + 1] = false
        }
        // Right eye
        for (dx in 0 until eyeSize) {
            grid[8 - dx, eyeY] = false
            if (eyeSize > 1) grid[8 - dx, eyeY + 1] = false
        }

        // Nose — small vertical line or dot in center
        val noseY = 5 + rng.nextInt(2) // row 5 or 6
        grid[6, noseY] = true

        // Mouth — horizontal strip
        val mouthY = 7 + rng.nextInt(1) // row 7 or 8
        val mouthW = 2 + rng.nextInt(3) // 2–4 wide
        val mouthX = 6 - mouthW / 2 + (rng.nextInt(3) - 1)
        for (x in mouthX until (mouthX + mouthW).coerceAtMost(10)) {
            grid[x.coerceIn(2, 10), mouthY] = rng.nextDouble() < 0.7
        }

        return grid
    }

    /** Generate a corruption tile of the specified type. */
    fun generateCorruption(
        type: CorruptionType = CorruptionType.STATIC,
        seed: Long = Random.nextLong(),
    ): PixelGrid = when (type) {
        CorruptionType.STATIC   -> generateStatic(seed)
        CorruptionType.SCRAMBLE -> generateScramble(seed)
        CorruptionType.EMPTY    -> generateEmpty(seed)
    }

    // ── corruption generators ───────────────────────────────────────────

    private fun generateStatic(seed: Long): PixelGrid {
        val rng = Random(seed)
        val grid = PixelGrid(SIZE)
        // Biased low density — 15-25 % fill
        val density = 0.15 + rng.nextDouble() * 0.10
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            grid[x, y] = rng.nextDouble() < density
        }
        return grid
    }

    private fun generateScramble(seed: Long): PixelGrid {
        val rng = Random(seed)
        // Generate a real face, then corrupt it
        val face = generateFace(seed)
        val grid = PixelGrid(SIZE)
        val flipRatio = 0.30 + rng.nextDouble() * 0.20 // 30–50 % flipped
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            grid[x, y] = if (rng.nextDouble() < flipRatio) !face[x, y] else face[x, y]
        }
        return grid
    }

    private fun generateEmpty(seed: Long): PixelGrid {
        val rng = Random(seed)
        val grid = PixelGrid(SIZE)
        // 1–4 scattered pixels total
        val count = 1 + rng.nextInt(4)
        repeat(count) {
            grid[rng.nextInt(SIZE), rng.nextInt(SIZE)] = true
        }
        return grid
    }
}

/** Mutable 2D boolean grid with string dumps for debugging/tests. */
class PixelGrid(val size: Int) {
    private val data = BooleanArray(size * size)

    operator fun get(x: Int, y: Int): Boolean = data[y * size + x]
    operator fun set(x: Int, y: Int, v: Boolean) { data[y * size + x] = v }

    fun pixelCount(): Int = data.count { it }

    fun toAscii(bright: Char = '#', dark: Char = '.'): String = buildString {
        for (y in 0 until size) {
            for (x in 0 until size) append(if (this@PixelGrid[x, y]) bright else dark)
            append('\n')
        }
    }
}
