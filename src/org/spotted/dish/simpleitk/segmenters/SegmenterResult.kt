package org.spotted.dish.simpleitk.segmenters

import org.itk.simple.Image


data class SegmenterResult(
        val path : String,
        val original : Image,
        val threshold : Image,
        val labels : Image? = null)