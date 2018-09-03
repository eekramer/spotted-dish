package org.spotted.dish

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl
import org.itk.simple.*

fun main(args: Array<String>) {
    val registeredImage = openFile("resources/la-binary.tif")

    val connectedMask = ConnectedComponentImageFilter().execute(registeredImage, true)

    val connectedStats = LabelShapeStatisticsImageFilter().apply{
        execute(connectedMask)
    }
    val boundingBox = connectedStats.getBoundingBox(connectedStats.labels.get(0))
    println("${boundingBox[0]} ${boundingBox[1]} ${boundingBox[2]} ${boundingBox[3]} ${boundingBox[4]} ${boundingBox[5]}")
    val indexVector = VectorInt32(3)
    for (x in 0 until 3) {
        indexVector[x] = (boundingBox[x].toInt())
    }
    val sizeVector = VectorUInt32(3)
    for (x in 0 until 3) {
        sizeVector[x] = (boundingBox[x+3])
    }
    print("${sizeVector[0]} ${sizeVector[1]} ${sizeVector[2]}")
    println("${indexVector[0]} ${indexVector[1]} ${indexVector[2]}")
    println("Cropping arc")
    // WARNING!!! We delete the registered image to save ram - don't access after this point!
    registeredImage.delete()



    val arcImage = openFile("resources/633-CLAEH5.tif")
    val croppedArch = cropImage(indexVector, sizeVector, arcImage)
    arcImage.delete()
    println("Cropping nuclear")
    val nuclearImage = openFile("resources/568-CLAHC30.tif")
    val croppedNuclear = cropImage(indexVector, sizeVector, nuclearImage)
    nuclearImage.delete()
    SimpleITK.show(croppedArch)
    SimpleITK.show(croppedNuclear)

}

private fun openFile(imageLocation : String) : Image {
    return ImageFileReader().apply {
        fileName = imageLocation
    }.execute()
}

private fun cropImage(indexVector : VectorInt32, sizeVector : VectorUInt32, image: Image) :Image {
    return RegionOfInterestImageFilter().apply {
        index = indexVector
        size = sizeVector
    }.execute(image)

}