package com.hashfactory.core.datamining

import com.hashfactory.core.datamining.FaceGenerator.CorruptionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FaceGeneratorTest {

    @Test
    fun `face pixels cluster in center, static noise scatters`() {
        val face = FaceGenerator.generateFace(42)
        val noise = FaceGenerator.generateCorruption(CorruptionType.STATIC, 42)
        // Faces fill center rows (3-9), static noise is spread everywhere.
        var faceCenter = 0
        var noiseCenter = 0
        for (y in 3..8) for (x in 3..9) {
            if (face[x, y]) faceCenter++
            if (noise[x, y]) noiseCenter++
        }
        assertTrue(faceCenter > noiseCenter,
            "face center=$faceCenter should exceed noise center=$noiseCenter")
    }

    @Test
    fun `scrambled face differs from original face`() {
        val face = FaceGenerator.generateFace(42)
        val scrambled = FaceGenerator.generateCorruption(CorruptionType.SCRAMBLE, 42)
        // Scrambled should not be identical to face
        assertNotEquals(face.toAscii(), scrambled.toAscii(),
            "scrambled face should differ from original")
        // But should have similar pixel count (30-50% flipped, not a total rewrite)
        val diff = (0 until FaceGenerator.SIZE).sumOf { y ->
            (0 until FaceGenerator.SIZE).count { x -> face[x, y] != scrambled[x, y] }
        }
        assertTrue(diff in 1 until FaceGenerator.SIZE * FaceGenerator.SIZE,
            "scramble should change some but not all pixels: $diff changed")
    }

    @Test
    fun `empty grid has very few pixels`() {
        val empty = FaceGenerator.generateCorruption(CorruptionType.EMPTY, 42)
        assertTrue(empty.pixelCount() in 1..4,
            "empty grid should have 1-4 pixels, got ${empty.pixelCount()}")
    }

    @Test
    fun `faces are deterministic per seed`() {
        val a = FaceGenerator.generateFace(123)
        val b = FaceGenerator.generateFace(123)
        assertEquals(a.toAscii(), b.toAscii(), "same seed should produce identical face")
    }

    @Test
    fun `different seeds produce different faces`() {
        val a = FaceGenerator.generateFace(100)
        val b = FaceGenerator.generateFace(999)
        assertNotEquals(a.toAscii(), b.toAscii(), "different seeds should differ")
    }

    @Test
    fun `grid size is 12`() {
        assertEquals(12, FaceGenerator.SIZE)
        val grid = FaceGenerator.generateFace()
        assertEquals(12, grid.size)
    }
}

class DatasetModelTest {

    @Test
    fun `dataset has correct tile count and face ratio`() {
        val ds = DatasetFactory.generate("test", width = 4, height = 3, faceRatio = 0.5, baseSeed = 1)
        assertEquals(12, ds.tiles.size)
        val faces = ds.tiles.count { it.isFace }
        val corruptions = ds.tiles.count { !it.isFace }
        assertTrue(faces in 4..8, "expected ~6 faces, got $faces")
        assertTrue(corruptions in 4..8, "expected ~6 corruptions, got $corruptions")
    }

    @Test
    fun `corruption tiles have all three types spread across dataset`() {
        val ds = DatasetFactory.generate("test", width = 4, height = 4, faceRatio = 0.25, baseSeed = 99)
        val types = ds.tiles.filter { !it.isFace }.map { it.corruptionType }.toSet()
        assertTrue(types.size >= 2,
            "should have at least 2 different corruption types, got ${types.size}: $types")
    }

    @Test
    fun `perfect solve earns max reward`() {
        val ds = DatasetFactory.generate("test", width = 2, height = 2, faceRatio = 0.5, baseSeed = 7)
        val clicks = mutableMapOf<Int, Boolean>()
        ds.tiles.forEachIndexed { i, tile ->
            if (tile.isFace) clicks[i] = true
        }
        val result = scoreDataset(ds, clicks)
        assertEquals(0, result.mistakes)
        assertTrue(result.flopsEarned > 0.0, "perfect solve should earn flops")
        assertTrue(result.isComplete, "all faces found with no mistakes should be complete")
    }

    @Test
    fun `clicking corruption penalizes`() {
        val ds = DatasetFactory.generate("test", width = 2, height = 2, faceRatio = 0.25, baseSeed = 9)
        val clicks = ds.tiles.indices.associateWith { true }
        val result = scoreDataset(ds, clicks)
        assertTrue(result.mistakes > 0, "clicking everything should produce mistakes")
        val maxPossible = result.facesFound * ds.rewardPerFace
        assertTrue(result.flopsEarned < maxPossible,
            "penalties should reduce payout: earned=${result.flopsEarned} < max=$maxPossible")
    }

    @Test
    fun `flops earned never goes negative`() {
        val ds = DatasetFactory.generate("test", width = 2, height = 2, faceRatio = 0.25, baseSeed = 9)
        val clicks = ds.tiles.indices.associateWith { true }
        val result = scoreDataset(ds, clicks)
        assertTrue(result.flopsEarned >= 0.0, "earned should not go negative: ${result.flopsEarned}")
    }

    @Test
    fun `dataset is deterministic per seed`() {
        val a = DatasetFactory.generate("a", baseSeed = 42)
        val b = DatasetFactory.generate("b", baseSeed = 42)
        assertEquals(a.tiles.size, b.tiles.size)
        a.tiles.zip(b.tiles).forEachIndexed { i, (ta, tb) ->
            assertEquals(ta.isFace, tb.isFace, "tile $i face flag mismatch")
            assertEquals(ta.pixels.toAscii(), tb.pixels.toAscii(), "tile $i pixel mismatch")
            assertEquals(ta.corruptionType, tb.corruptionType, "tile $i corruption type mismatch")
        }
    }
}
