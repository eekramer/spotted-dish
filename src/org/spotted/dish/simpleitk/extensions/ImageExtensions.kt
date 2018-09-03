package org.spotted.dish.simpleitk.extensions

import org.itk.simple.*

operator fun Image.times(constant : Double) : Image {
    return MultiplyImageFilter().execute(this, constant)
}

operator fun Image.times(image : Image) : Image {
    val desiredPixelId = this.pixelID
    val convertedImage = CastImageFilter().apply {
        outputPixelType = desiredPixelId
    }.execute(image)
    return MultiplyImageFilter().execute(this, convertedImage)
}

operator fun Image.minus(image : Image) : Image {
    val desiredPixelId = this.pixelID
    val convertedImage = CastImageFilter().apply {
        outputPixelType = desiredPixelId
    }.execute(image)
    return SubtractImageFilter().execute(this, convertedImage)
}

operator fun Image.plus(image : Image) : Image {
    val desiredPixelId = this.pixelID
    val convertedImage = CastImageFilter().apply {
        outputPixelType = desiredPixelId
    }.execute(image)
    return AddImageFilter().execute(this, convertedImage)
}

fun Image.greaterThan(constant : Double) : Image {
    return GreaterImageFilter().execute(this, constant)
}

fun Image.notEqual(constant : Double) : Image {
    return NotEqualImageFilter().execute(this, constant)
}

fun Image.lessThan(constant : Double) : Image {
    return LessImageFilter().execute(this, constant)
}

fun Image.cast(pixelType : PixelIDValueEnum) : Image {
    return CastImageFilter().apply {
        outputPixelType = pixelType
    }.execute(this)
}

fun Image.crop(boundingBox : VectorUInt32) : Image {
    assert(boundingBox.size().toInt() == 6)
    val indexVector = VectorInt32(3)
    for (x in 0 until 3) {
        indexVector[x] = (boundingBox[x].toInt())
    }
    val sizeVector = VectorUInt32(3)
    for (x in 0 until 3) {
        sizeVector[x] = (boundingBox[x+3])
    }
    return RegionOfInterestImageFilter().apply {
        index = indexVector
        size = sizeVector
    }.execute(this)
}