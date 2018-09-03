package org.spotted.dish.simpleitk.segmenters

import org.itk.simple.GrayscaleErodeImageFilter
import org.itk.simple.SmoothingRecursiveGaussianImageFilter

class ArcSegmenter : Segmenter() {
    override fun execute(imageLocation: String): SegmenterResult {
        val originalImage = openImage(imageLocation)
        val erosionVector = vectorCube(2)

        val smoothedImage = SmoothingRecursiveGaussianImageFilter().execute(originalImage, 1.5, false)
        val erodeImage = GrayscaleErodeImageFilter().apply{kernelRadius=erosionVector}.execute(smoothedImage)

        val thresholdImage = applyThresholdFilter(erodeImage)
        return SegmenterResult(
                path = imageLocation,
                threshold = thresholdImage,
                original = originalImage
        )

    }

}
