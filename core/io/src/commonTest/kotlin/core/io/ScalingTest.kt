package core.io

import kotlin.test.Test
import kotlin.test.assertEquals

class ScalingTest {
    @Test
    fun noUpscaleWhenWithinBounds() {
        assertEquals(500 to 400, fitInto(500, 400, 1024))
    }

    @Test
    fun scalesLongSideDown() {
        val result = fitInto(4000, 2000, 1000)
        assertEquals(1000 to 500, result)
    }

    @Test
    fun handlesSquareImages() {
        val result = fitInto(3000, 3000, 1024)
        assertEquals(1024 to 1024, result)
    }

    @Test
    fun zeroMaxLongSideKeepsOriginal() {
        val result = fitInto(1200, 900, 0)
        assertEquals(1200 to 900, result)
    }
}
