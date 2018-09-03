package org.spotted.dish

import org.itk.simple.Image

class SegmenterResult1(val image : Image)

data class SegmenterResult(
        val original : Image,
        val threshold : Image,
        val labels : Image? = null,
        val peaks : Image? =  null,
        val distance : Image? = null)