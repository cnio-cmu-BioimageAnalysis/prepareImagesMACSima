/*
* Confocal Microscopy Unit - CNIO : BioImage Analysis
*
* Developer : Ana Cayuela & María Calvo de Mora
*
* Last Update Janury 2025
*
*/

import ij.IJ
import ij.ImagePlus
import ij.measure.ResultsTable
import ij.plugin.RGBStackMerge



// INPUT UI
//
#@File(label = "Input File Directory", style = "directory") inputDir
#@File(label = "Output File Directory", style = "directory") outputDir
//def inputDir = new File("\\\\imgserver.cnio.es\\IMAGES\\CONFOCAL\\MACSima\\MG\\MG_140125_ABpanel_Run1_250121_231231\\MG_140125_ABpanel_Run1_2025-01-14_12-08-11\\PreprocessedData")
//def outputDir = new File("\\\\imgserver.cnio.es\\IMAGES\\CONFOCAL\\IA\\OUTPUT_MACSima\\MG_140125_ABpanel_Run1_2025-01-14_12-08-11")

IJ.log("-Parameters selected: ")
IJ.log("    -input Directory: " + inputDir)
IJ.log("    -output Directory: " + outputDir)
IJ.log("                                                           ");
def cal = null

def listofFiles0 = inputDir.listFiles()
for (def h = 0; h < listofFiles0.length; h++) {
    IJ.log(listofFiles0[h].toString())
    def listOfFiles1 = listofFiles0[h].listFiles()
    for (def i = 0; i < listOfFiles1.length; i++) {
        IJ.log(listOfFiles1[i].toString())
        def listOfFiles2 = listOfFiles1[i].listFiles() // ROI list
        for (def j = 0; j < listOfFiles2.length; j++) {
            def roi = new ArrayList<ImagePlus>()
            if (!listOfFiles2[j].getName().contains("ROI0")) {
                IJ.log(listOfFiles2[j].toString())
                def listOfFiles3 = listOfFiles2[j].listFiles() // Scan & Cycle list
                for (def k = 0; k < listOfFiles3.length; k++) {
                    if (listOfFiles3[k].getName().contains("Scan")) {
                        def listOfFiles4s = listOfFiles3[k].listFiles() // Inside Scan
                        for (def l = 0; l < listOfFiles4s.length; l++) {
                            if (listOfFiles4s[l].getName().contains("C-000") && listOfFiles4s[l].getName().contains("0_S_") && !listOfFiles4s[l].getName().contains("DAPI") && !listOfFiles4s[l].getName().contains(".tsv")) {
                                IJ.log(listOfFiles4s[l].toString())
                                def scan = new ImagePlus(listOfFiles4s[l].getAbsolutePath())
                                scan.setTitle(scan.getTitle()) //scan files (except DAPI & .tsv)
                                def fileName = listOfFiles4s[l].getName()
                                def lastDashIndex = fileName.lastIndexOf('-') // Obain last "-"
                                def trimmedName = fileName.substring(lastDashIndex + 1)
                                // Obtain everything after the last "-"
                                scan.setTitle(trimmedName)
                                roi.add(scan)
                            }
                        }
                    }
                    // IF EMPTY CYCLES: if (!listOfFiles3[k].getName().contains("Scan") && !listOfFiles3[k].getName().contains("Cycle3") && !listOfFiles3[k].getName().contains("Cycle5"))
                    // IF NOT EMPTY CYCLES: if (!listOfFiles3[k].getName().contains("Scan"))
                    if (!listOfFiles3[k].getName().contains("Scan")) {
                        def listOfFiles4 = listOfFiles3[k].listFiles() // Inside each cycle
                        for (def m = 0; m < listOfFiles4.length; m++) {
                            if (listOfFiles4[m].getName().contains("C-001") && listOfFiles4[m].getName().contains("_S_") && listOfFiles4[m].getName().contains("DAPI") && !listOfFiles4[m].getName().contains(".tsv")) {
                                IJ.log(listOfFiles4[m].toString())
                                def dapi = new ImagePlus(listOfFiles4[m].getAbsolutePath())
                                cal = dapi.getCalibration()
                                dapi.setTitle(dapi.getTitle()) //dapi channel
                                def fileName = listOfFiles4[m].getName()
                                def lastDashIndex = fileName.lastIndexOf('-') // Obain last "-"
                                def trimmedName = fileName.substring(lastDashIndex + 1)
                                // Obtain everything after the last "-"
                                dapi.setTitle(trimmedName.replaceAll(".tif", ""))
                                roi.add(dapi)
                            }
                            if (!listOfFiles4[m].getName().contains("DAPI") && listOfFiles4[m].getName().contains("_S_") && !listOfFiles4[m].getName().contains(".tsv")) {
                                IJ.log(listOfFiles4[m].toString())
                                def imp = new ImagePlus(listOfFiles4[m].getAbsolutePath()) //rest of channels
                                def fileName = listOfFiles4[m].getName()
                                def trimmedFileName = fileName.substring(2) //Ignore first C-
                                def startPrefix = "A-"
                                def startIndex = trimmedFileName.indexOf(startPrefix) + startPrefix.length()
                                def endIndex = trimmedFileName.indexOf("_C-", startIndex)
                                if (endIndex == -1) {
                                    def lastDashIndex = fileName.lastIndexOf('-') // Obain last "-"
                                    def trimmedName = fileName.substring(lastDashIndex + 1)
                                    // Obtain everything after the last "-"
                                    imp.setTitle(trimmedName.replaceAll(".tif", ""))
                                } else {
                                    def trimmedName = trimmedFileName.substring(startIndex, endIndex)
                                    imp.setTitle(trimmedName)
                                }

                                roi.add(imp)
                            }
                        }
                    }
                }
                def imageArray = new ImagePlus[roi.size()];
                imageArray = roi.toArray(imageArray);
                def merge = RGBStackMerge.mergeChannels(imageArray, false)
                merge.setCalibration(cal)

                def saveDir = outputDir.getAbsolutePath() + File.separator + listOfFiles2[j].getAbsolutePath().replace(inputDir.getAbsolutePath(), "")
                def fileDir = new File(saveDir).getParentFile()
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }
                IJ.saveAsTiff(merge, saveDir)

                //IJ.saveAsTiff(merge, outputDir.getAbsolutePath() + File.separator + listOfFiles2[j].getAbsolutePath().replace(inputDir.getAbsolutePath(), ""))
                //IJ.run(merge, "OME-TIFF...", "save=" + outputDir.getAbsolutePath() + File.separator + listOfFiles2[j].getName() + ".ome.tif" + " export compression=Uncompressed")
            }
        }
    }
}
