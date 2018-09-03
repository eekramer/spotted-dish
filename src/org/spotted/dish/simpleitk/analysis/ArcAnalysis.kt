package org.spotted.dish.simpleitk.analysis

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl
import mu.KotlinLogging
import org.itk.simple.*
import org.spotted.dish.LoggingClass
import org.spotted.dish.simpleitk.extensions.cast
import org.spotted.dish.simpleitk.extensions.crop
import org.spotted.dish.simpleitk.extensions.notEqual
import org.spotted.dish.simpleitk.extensions.times
import org.spotted.dish.simpleitk.segmenters.SegmenterResult
import java.io.File
import java.io.FileWriter

class ArcAnalysis(private val nuclearResult : SegmenterResult, private val arcResult : SegmenterResult)
    : LoggingClass(KotlinLogging.logger {})  {
    private val nuclearStatsFilter = LabelShapeStatisticsImageFilter()
    private val arcStatsFilter = LabelShapeStatisticsImageFilter()
    private val arcReconstruction : Image = applyBinaryReconstruction(nuclearResult.labels!!, arcResult.threshold)

    init {
        nuclearStatsFilter.execute(nuclearResult.labels)
        arcStatsFilter.execute(arcReconstruction)
    }

    fun drawOverlay() {
        SimpleITK.show(applyWatershedOverlay(nuclearResult.original, arcReconstruction))
    }

    fun dumpSummary(folderPath : String) {
        val file = File(folderPath).resolve("summary.csv")
        val writer = FileWriter(file)
        writer.appendln("nuclear_path, ${nuclearResult.path}")
        writer.appendln("arc_path, ${arcResult.path}")
        writer.appendln("nuclear_count, ${nuclearStatsFilter.numberOfLabels}")
        writer.appendln("arc_count, ${arcStatsFilter.numberOfLabels}")
        writer.close()
        val summary = File(folderPath).resolve("overlay.tif")
        SimpleITK.writeImage(applyWatershedOverlay(nuclearResult.original, arcReconstruction), summary.toString())
    }

    fun dumpArcSummary(folderPath: String) {
        val file = File(folderPath).resolve("arc_summary.csv")
        val writer = FileWriter(file)
        val folder = File(folderPath).resolve("arc")
        folder.mkdir()
        dumpLabels(writer, arcStatsFilter, folder, arcReconstruction)
        writer.close()
    }

    fun dumpNuclearSummary(folderPath: String) {
        val file = File(folderPath).resolve("nuclear_summary.csv")
        val writer = FileWriter(file)
        val folder = File(folderPath).resolve("nuclear")
        folder.mkdir()
        dumpLabels(writer, nuclearStatsFilter, folder, nuclearResult.original)
        writer.close()
    }

    private fun dumpLabels(writer : FileWriter, statsFilter : LabelShapeStatisticsImageFilter, folder : File, image : Image) {

        writer.appendln("label_number, size, centroid, bounding_box")
        val labels = statsFilter.labels
        for (i in 0 until statsFilter.numberOfLabels.toInt()) {
            val label = labels[i]
            val centroid = statsFilter.getCentroid(label)
            val box = statsFilter.getBoundingBox(label)
            val path = folder.resolve("${label}_nuclear.tif")
            writer.appendln("$label, ${statsFilter.getPhysicalSize(label)}, ${writeVector(centroid)}, ${writeVector(box)}" )
            val croppedImage = image.crop(box).cast(PixelIDValueEnum.sitkUInt8)
            SimpleITK.writeImage(croppedImage, path.toString())
        }
    }

    private fun writeVector(vector : VectorDouble) : String {
        val string = StringBuilder("(${vector[0]}")
        for (i in 1 until vector.size())
            string.append(",${vector[i.toInt()]}")
        string.append(")")
        return string.toString()
    }
    private fun writeVector(vector : VectorUInt32) : String {
        val string = StringBuilder("(${vector[0]}")
        for (i in 1 until vector.size())
            string.append(",${vector[i.toInt()]}")
        string.append(")")
        return string.toString()
    }
}
private fun applyWatershedOverlay(original: Image, labeled: Image) : Image {
    return LabelMapOverlayImageFilter().apply {
        opacity = 0.5
    }.execute(LabelImageToLabelMapFilter().execute(labeled), original)
}

private fun applyBinaryReconstruction(nuclearLabels : Image, arcThreshold : Image) : Image {
    val binayReconstruction = BinaryReconstructionByDilationImageFilter()
    val convertedImage = (nuclearLabels!!.notEqual(0.0)).cast(arcThreshold.pixelID)
    val reconstruction = binayReconstruction.apply{fullyConnected = true}.execute(arcThreshold * convertedImage, convertedImage)
    return ConnectedComponentImageFilter().execute(reconstruction)
}