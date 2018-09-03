package org.spotted.dish

import org.spotted.dish.simpleitk.analysis.ArcAnalysis
import org.spotted.dish.simpleitk.segmenters.ArcSegmenter
import org.spotted.dish.simpleitk.segmenters.NuclearSegmenter


fun main(args: Array<String>) {
    val result = NuclearSegmenter().execute("resources/GT-raw-568.tif")
    val arcResult = ArcSegmenter().execute("resources/GT-raw-633.tif")

    val analysis = ArcAnalysis(result, arcResult)
    analysis.drawOverlay()
    analysis.dumpSummary("resources")
    analysis.dumpArcSummary("resources")
    analysis.dumpNuclearSummary("resources")


}

