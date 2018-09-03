package org.spotted.dish.simpleitk.segmenters

import org.itk.simple.*
import org.spotted.dish.simpleitk.extensions.greaterThan
import org.spotted.dish.simpleitk.extensions.lessThan
import org.spotted.dish.simpleitk.extensions.plus
import org.spotted.dish.simpleitk.extensions.times


class NuclearSegmenter(private val largeCellSize : Int = 1000) : Segmenter() {

    override fun execute(imageLocation: String): SegmenterResult {
        val originalImage = openImage(imageLocation)
        val erosionVector = vectorCube(2)

        // Do initial processing
        val openImage = applySmoothingAndOpening(originalImage, erosionVector)
        val thresholdImage = applyThresholdFilter(openImage)
        val labeledThreshold = executeInternal(thresholdImage)
        val binaryFirstWatershed = labeledThreshold.greaterThan(0.0)

        // Reprocess the larger cells
        val size = largeCellSize.toBigInteger()
        val largeLabels = RelabelComponentImageFilter().apply { minimumObjectSize=size }.execute(labeledThreshold)
        val cutLargeLabels = applyRemoveNegativeCurvature(openImage, largeLabels)
        val thresholdLargeLabels = applyThresholdFilter(cutLargeLabels)
        val watershedLargeLabels = executeInternal(thresholdLargeLabels)
        val binaryLargeLabels = watershedLargeLabels.greaterThan(0.0)

        // Get the small labels and combine with the large labels
        val binarySmallLabels = binaryFirstWatershed * InvertIntensityImageFilter().apply { maximum=1.0 }.execute(largeLabels.greaterThan(0.0))
        val combinedBinaryLabels = binaryLargeLabels + binarySmallLabels

        val connectedTotal = applyConnectedComponentFilter(combinedBinaryLabels)
        return SegmenterResult(
                path = imageLocation,
                threshold = thresholdImage,
                labels = connectedTotal,
                original = originalImage)
    }

    private fun executeInternal(thresholdImage : Image) : Image {
        val invertedThresh = InvertIntensityImageFilter().apply{maximum=1.0}.execute(thresholdImage)
        var distanceTransform = applyDistanceTransform(invertedThresh)
        val distanceSmoothed = SmoothingRecursiveGaussianImageFilter().execute(distanceTransform, 2.0, true)
        distanceTransform = distanceSmoothed * distanceTransform.greaterThan(0.0)
        val maximaImage = applyMaximaFilter(distanceTransform)
        val connectedMarkers = applyConnectedComponentFilter(maximaImage)
        val watershedImage = applyWatershed(distanceTransform * -1.0, connectedMarkers)
        return watershedImage * (distanceTransform.greaterThan(0.0))
    }

    private fun applySmoothingAndOpening(image : Image, erosionVector : VectorUInt32, sigma : Double = 1.5) : Image {
        return log({
            val smoothedImage = SmoothingRecursiveGaussianImageFilter().execute(image, sigma, false)
            GrayscaleMorphologicalOpeningImageFilter().apply{
            kernelRadius=erosionVector
        }.execute(smoothedImage)}, "Smoothing and opening")
    }


    private fun applyRemoveNegativeCurvature(baseImage : Image, labels : Image) : Image{
        return log({
            labels * LaplacianRecursiveGaussianImageFilter().execute(baseImage).lessThan(0.0)
        }, "Removing negative curvature")
    }
}