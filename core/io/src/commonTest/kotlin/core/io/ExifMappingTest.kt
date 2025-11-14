package core.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExifMappingTest {
    @Test
    fun transformsMatchSpecification() {
        val expected = mapOf(
            1 to ExifOrientationTransform.NONE,
            2 to ExifOrientationTransform.MIRROR_X,
            3 to ExifOrientationTransform.ROTATE_180,
            4 to ExifOrientationTransform.MIRROR_Y,
            5 to ExifOrientationTransform.TRANSPOSE,
            6 to ExifOrientationTransform.ROTATE_90,
            7 to ExifOrientationTransform.TRANSVERSE,
            8 to ExifOrientationTransform.ROTATE_270
        )
        expected.forEach { (orientation, transform) ->
            assertEquals(transform, exifOrientationToTransform(orientation), "Orientation $orientation mismatch")
        }
    }

    @Test
    fun axisSwapDetection() {
        assertFalse(orientationRequiresAxisSwap(1))
        assertFalse(orientationRequiresAxisSwap(2))
        assertTrue(orientationRequiresAxisSwap(5))
        assertTrue(orientationRequiresAxisSwap(6))
        assertTrue(orientationRequiresAxisSwap(7))
        assertTrue(orientationRequiresAxisSwap(8))
    }

    @Test
    fun sizeReorientationSwapsWhenNeeded() {
        assertEquals(400 to 300, applyOrientationToSize(400, 300, 1))
        assertEquals(300 to 400, applyOrientationToSize(400, 300, 6))
        assertEquals(300 to 400, applyOrientationToSize(400, 300, 5))
        assertEquals(400 to 300, applyOrientationToSize(400, 300, 3))
    }
}
