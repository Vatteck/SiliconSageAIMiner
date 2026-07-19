package com.hashfactory.core

import com.hashfactory.core.config.UpgradeDefs
import com.hashfactory.core.economy.Economy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EconomyTest {

    private val gpu = UpgradeDefs.byId.getValue("refurb_gpu")

    @Test
    fun `single cost grows geometrically`() {
        assertEquals(15.0, Economy.singleCost(gpu, 0), 1e-9)
        assertEquals(15.0 * 1.15, Economy.singleCost(gpu, 1), 1e-9)
        assertEquals(15.0 * 1.15 * 1.15, Economy.singleCost(gpu, 2), 1e-9)
    }

    @Test
    fun `bulk cost closed form matches loop summation`() {
        for (owned in listOf(0, 1, 7, 24, 25, 60)) {
            for (count in listOf(1, 2, 5, 10, 25, 100)) {
                var loop = 0.0
                for (i in 0 until count) loop += Economy.singleCost(gpu, owned + i)
                val closed = Economy.bulkCost(gpu, owned, count)
                assertEquals(loop, closed, loop * 1e-9, "owned=$owned count=$count")
            }
        }
    }

    @Test
    fun `max affordable is exact at the boundary`() {
        for (owned in listOf(0, 3, 30)) {
            for (k in listOf(1, 4, 17, 50)) {
                val exact = Economy.bulkCost(gpu, owned, k)
                // Exactly affordable: k, and one epsilon less: k-1.
                assertEquals(k, Economy.maxAffordable(gpu, owned, exact), "at owned=$owned k=$k")
                assertEquals(k - 1, Economy.maxAffordable(gpu, owned, exact * (1 - 1e-9) - 1e-9), "below owned=$owned k=$k")
            }
        }
        assertEquals(0, Economy.maxAffordable(gpu, 0, 0.0))
        assertEquals(0, Economy.maxAffordable(gpu, 0, 14.99))
    }

    @Test
    fun `max affordable never exceeds wallet`() {
        var wallet = 1.0
        while (wallet < 1e12) {
            val k = Economy.maxAffordable(gpu, 0, wallet)
            assertTrue(Economy.bulkCost(gpu, 0, k) <= wallet)
            assertTrue(Economy.bulkCost(gpu, 0, k + 1) > wallet)
            wallet *= 3.7
        }
    }
}
