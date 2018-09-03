package org.spotted.dish.simpleitk.segmenters

import mu.KotlinLogging
import org.itk.simple.*
import org.spotted.dish.LoggingClass
import org.spotted.dish.simpleitk.extensions.*

abstract class Segmenter
    : LoggingClass(KotlinLogging.logger {}) {

    abstract fun execute(imageLocation : String) : SegmenterResult

    protected fun applyThresholdFilter(image: Image) : Image {
        return log({
            YenThresholdImageFilter().apply {
                insideValue = 0
                outsideValue = 1
            }.execute(image)
        }, "Yen filter")
    }

    protected fun applyDistanceTransform(image: Image) : Image {
        return log({
            DanielssonDistanceMapImageFilter().apply {
                useImageSpacing = true
            }.execute(image)
        }, "Distance transform")
    }

    protected fun applyMaximaFilter(image: Image) : Image {
        return log({
            RegionalMaximaImageFilter().apply {
                foregroundValue = 1.0
                fullyConnected = true
            }.execute(image)
        }, "Maxima filter")
    }

    protected fun applyConnectedComponentFilter(image: Image) : Image {
        return log({
            ConnectedComponentImageFilter().apply {
                fullyConnected = true
            }.execute(image)
        }, "Connected component filter")
    }

    protected fun applyWatershed(image: Image, markers: Image) : Image {
        return log({
            MorphologicalWatershedFromMarkersImageFilter().apply {
                markWatershedLine = true
                fullyConnected = true
            }.execute(image, markers)
        }, "Watershed")
    }

    protected fun applyWatershedOverlay(original: Image, labeled: Image) : Image {
        return log({
            LabelMapOverlayImageFilter().apply {
                opacity = 0.50
            }.execute(LabelImageToLabelMapFilter().execute(labeled), original)
        }, "Watershed overlay")
    }

    protected fun openImage(path : String) : Image {
        return ImageFileReader().apply {
            fileName = path
        }.execute()
    }

    protected fun vectorCube(value : Long) : VectorUInt32 {
        val vector = VectorUInt32(3)
        vector[0] = value
        vector[1] = value
        vector[2] = value
        return vector
    }
}