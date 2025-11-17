package dev.handmade.core.io

/**
 * Временные простые доменные заглушки, чтобы модуль компилировался в S1.
 * Позже их можно перенести в :core:domain и повесить зависимость.
 */
data class ImageRef(val uri: String)

data class SourceImage(
    val width: Int,
    val height: Int,
    val previewRef: ImageRef,
    val fullRef: ImageRef,
    val exifOrientation: Int?
)

/** Платформенный handle на нативное изображение. */
expect class NativeImage

// ----- ВАЖНО: expect на ТOП-УРОВНЕ (соответствует actual в Android) -----

/** Декод файла в нативное изображение. */
expect fun load(path: String): NativeImage

/** Применить EXIF-ориентацию, вернуть новый нативный образ (или тот же, если не требуется). */
expect fun exifRotate(img: NativeImage): NativeImage

/** Привести пиксели к sRGB (если уже sRGB — вернуть как есть). */
expect fun toSRGB(img: NativeImage): NativeImage

/** Сделать превью, ограничив длинную сторону maxSide (без апскейла). */
expect fun makePreview(img: NativeImage, maxSide: Int = 1024): NativeImage

/**
 * Оболочка для сборки SourceImage из нативных картинок.
 * Пока это S1-скелет: размеры и EXIF не считаем, ссылки — синтетические.
 */
object ImageIO {
    fun toSourceImage(img: NativeImage, preview: NativeImage): SourceImage {
        return SourceImage(
            width = 0,
            height = 0,
            previewRef = ImageRef("preview://stub"),
            fullRef = ImageRef("full://stub"),
            exifOrientation = null
        )
    }
}
