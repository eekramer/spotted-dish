package org.spotted.dish

import mu.KotlinLogging
import org.itk.simple.*
import org.spotted.dish.simpleitk.extensions.*
import java.math.BigInteger

class Box() {
    var x : Int = 0
    var y : Int = 0
}

class Segmenter
    : LoggingClass(KotlinLogging.logger {}) {

    fun execute(imageLocation : String, watershed : Boolean = true) : SegmenterResult {
        val originalImage = ImageFileReader().apply {
            fileName = imageLocation
        }.execute()


        val erosionVector = VectorUInt32(3)
        erosionVector[0] = 2
        erosionVector[1] = 2
        erosionVector[2] = 2

        //val erodeImage = GrayscaleErodeImageFilter().apply{kernelRadius=erosionVector}.execute(originalImage)
        //val dilateImage = GrayscaleDilateImageFilter().apply{kernelRadius=erosionVector}.execute(originalImage)
        //val holyImage = GrayscaleFillholeImageFilter().apply { fullyConnected=true }.execute(originalImage)

        val smoothedImage = SmoothingRecursiveGaussianImageFilter().execute(originalImage, 1.5, false)

        val openImage = GrayscaleMorphologicalOpeningImageFilter().apply{kernelRadius=erosionVector}.execute(smoothedImage)
        val erodeImage = GrayscaleErodeImageFilter().apply{kernelRadius=erosionVector}.execute(arc)

        val thresholdImage = applyThresholdFilter(openImage)

        //val shapeDetect = ShapeDetectionLevelSetImageFilter().apply{curvatureScaling=1.0
        //    propagationScaling=1.0}.execute(thresholdImage, distanceTransform)

        val lap = LaplacianRecursiveGaussianImageFilter().apply {  }.execute(openImage)
        //SimpleITK.show(lap)


        if (watershed) {

            val invertedThresh = InvertIntensityImageFilter().apply{maximum=1.0}.execute(thresholdImage)
            var distanceTransform = applyDistanceTransform(invertedThresh)
            val distanceSmoothed = SmoothingRecursiveGaussianImageFilter().execute(distanceTransform, 2.0, true)
            distanceTransform = distanceSmoothed * distanceTransform.greaterThan(0.0)
            val maximaImage = applyMaximaFilter(distanceTransform)
            val connectedMarkers = applyConnectedComponentFilter(maximaImage)
            val watershedImage = applyWatershed(distanceTransform * -1.0, connectedMarkers)
            val labeledThreshold = watershedImage * (distanceTransform.greaterThan(0.0))
            //SimpleITK.show(labeledThreshold)



            //val connectedMarkersBig = applyConnectedComponentFilter(thresholdImage)
            //SimpleITK.show(connectedMarkersBig)
            val size = 1000.toBigInteger()
            val largeLab = RelabelComponentImageFilter().apply { minimumObjectSize=size }.execute(labeledThreshold)
            val invertLargeLab = InvertIntensityImageFilter().apply { maximum=1.0 }.execute(largeLab.greaterThan(0.0))
            val binSmolBab = labeledThreshold.greaterThan(0.0)
            val smolBab = binSmolBab * invertLargeLab
            //SimpleITK.show(SmolBab)

           // val statsFilter = LabelShapeStatisticsImageFilter()
            //statsFilter.execute(labeledThreshold)


            //val test = statsFilter.getPhysicalSize(6)
            //println(test)


            val lapLarge = largeLab * lap.lessThan(0.0)
            //SimpleITK.show(lapLarge)
            val thresholdImage2 = applyThresholdFilter(lapLarge)
            val invertedThresh2 = InvertIntensityImageFilter().apply{maximum=1.0}.execute(thresholdImage2)
            var distanceTransform2 = applyDistanceTransform(invertedThresh2)
            val distanceSmoothed2 = SmoothingRecursiveGaussianImageFilter().execute(distanceTransform2, 2.5, true)
            distanceTransform2 = distanceSmoothed2 * distanceTransform2.greaterThan(0.0)
            val maximaImage2 = applyMaximaFilter(distanceTransform2)
            val connectedMarkers2 = applyConnectedComponentFilter(maximaImage2)
            val watershedImage2 = applyWatershed(distanceTransform2 * -1.0, connectedMarkers2)
            val labeledThreshold2 = watershedImage2 * (largeLab.greaterThan(0.0))
                    // (distanceTransform2.greaterThan(0.0))

           //SimpleITK.show(labeledThreshold2)


            //val binSmall = labeledThreshold.greaterThan(0.0)
            val binLarge = labeledThreshold2.greaterThan(0.0)
            SimpleITK.show(binLarge)
            //SimpleITK.show(binSmall)


            val binCombo = binLarge + smolBab

            val connectedTotal = applyConnectedComponentFilter(binCombo)
            SimpleITK.show(applyWatershedOverlay(originalImage, connectedTotal))


            return SegmenterResult(threshold = thresholdImage,
                    distance = distanceTransform,
                    labels = connectedTotal,
                    peaks = maximaImage,
                    original = originalImage)
        }
        return SegmenterResult(
                threshold = thresholdImage,
                original = originalImage
        )

    }
    private fun applyThresholdFilter(image: Image) : Image {
        return log({
            YenThresholdImageFilter().apply {
                insideValue = 0
                outsideValue = 1
            }.execute(image)
        }, "Yen filter")
    }

    private fun applyDistanceTransform(image: Image) : Image {
        return log({
            DanielssonDistanceMapImageFilter().apply {
                useImageSpacing = true
            }.execute(image)
        }, "Distance transform")
    }

    private fun applyMaximaFilter(image: Image) : Image {
        return log({
            RegionalMaximaImageFilter().apply {
                foregroundValue = 1.0
                fullyConnected = true
            }.execute(image)
        }, "Maxima filter")
    }

    private fun applyConnectedComponentFilter(image: Image) : Image {
        return log({
            ConnectedComponentImageFilter().apply {
                fullyConnected = true
            }.execute(image)
        }, "Connected component filter")
    }

    private fun applyWatershed(image: Image, markers: Image) : Image {
        return log({
            MorphologicalWatershedFromMarkersImageFilter().apply {
                markWatershedLine = true
                fullyConnected = true
            }.execute(image, markers)
        }, "Watershed")
    }

    private fun applyWatershedOverlay(original: Image, labeled: Image) : Image {
        return log({
            LabelMapOverlayImageFilter().apply {
                opacity = 0.50
            }.execute(LabelImageToLabelMapFilter().execute(labeled), original)
        }, "Watershed overlay")
    }
}