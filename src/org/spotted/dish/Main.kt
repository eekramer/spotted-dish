package org.spotted.dish

import org.itk.simple.*
import org.spotted.dish.simpleitk.extensions.notEqual
import org.spotted.dish.simpleitk.extensions.times


fun main(args: Array<String>) {

    val result = Segmenter().execute("resources/GT-raw-568.tif")
    val arcResult = Segmenter().execute("resources/GT-raw-633.tif", watershed = false)

    val binayReconstruction = BinaryReconstructionByDilationImageFilter()
    val convertedImage = CastImageFilter().apply {
        outputPixelType = arcResult.threshold.pixelID
    }.execute(result.labels!!.notEqual(0.0))
    val arcReconstruction =
            binayReconstruction.apply{fullyConnected = true}.execute(arcResult.threshold * convertedImage, convertedImage)
    SimpleITK.show(applyWatershedOverlay(result.original, ConnectedComponentImageFilter().execute(arcReconstruction)))

    val statsFilter = LabelShapeStatisticsImageFilter()
    statsFilter.execute(result.labels)
    val numOfNuclear = statsFilter.numberOfLabels
    val centroidNuclear = statsFilter.getCentroid(5)
    for (x in 0 until 3) {
        println(centroidNuclear[x])
    }

    val statsFilterArc = LabelShapeStatisticsImageFilter()
    statsFilterArc.execute(ConnectedComponentImageFilter().execute(arcReconstruction))
    val numOfArc = statsFilterArc.numberOfLabels

    println("Number of cells: $numOfNuclear")
    println("Arc tagged cells: $numOfArc")
}
private fun applyWatershedOverlay(original: Image, labeled: Image) : Image {
    return LabelMapOverlayImageFilter().apply {
        opacity = 0.5
    }.execute(LabelImageToLabelMapFilter().execute(labeled), original)
}
