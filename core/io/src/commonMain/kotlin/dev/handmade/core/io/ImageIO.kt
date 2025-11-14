package dev.handmade.core.io

import dev.handmade.core.domain.ImageRef
import dev.handmade.core.domain.SourceImage

// Expect/actual API for image operations. Android actual is provided; others will be added later.
expect class NativeImage

object ImageIO {
    // In S1 we expose simple helpers; implementations may throw NotImplementedError in actuals.
    expect fun load(path: String): NativeImage
    expect fun exifRotate(img: NativeImage): NativeImage
    expect fun toSRGB(img: NativeImage): NativeImage
    expect fun makePreview(img: NativeImage, maxSide: Int = 1024): NativeImage

    // Convert to SourceImage placeholder with synthetic refs (S1 skeleton)
    fun toSourceImage(img: NativeImage, preview: NativeImage): SourceImage {
        // In S1 we don't have real image store; use synthetic refs.
        return SourceImage(
            width = 0,
            height = 0,
            previewRef = ImageRef("preview://stub"),
            fullRef = ImageRef("full://stub"),
            exifOrientation = null
        )
    }
}
