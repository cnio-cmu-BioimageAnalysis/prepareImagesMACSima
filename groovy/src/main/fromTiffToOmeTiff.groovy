import qupath.lib.io.PathIO
import qupath.lib.projects.Project
import qupath.lib.gui.scripting.QPEx
import qupath.lib.images.servers.ImageServer
import qupath.lib.common.GeneralTools
import qupath.lib.objects.PathObject
import qupath.lib.images.writers.ImageWriterTools

// Obtener el proyecto actual
def project = getProject() 
def projectBaseDir = project.getBaseDirectory()

// Crear un directorio para los archivos exportados
def omeDir = new File(projectBaseDir, "ome.tif")
if (!omeDir.exists()) omeDir.mkdir()

// Loop para recorrer cada imagen en el proyecto
project.getImageList().each { imageEntry ->
    def imageData = imageEntry.readImageData()  // Usar readImageData() para cargar la imagen
    if (imageData == null) {
        print "No image data for: ${imageEntry.getName()}"
        return
    }

    // Obtener nombre de la imagen
    def imageName = GeneralTools.getNameWithoutExtension(imageEntry.getImageName())

    // Definir ruta de salida
    def pathOutput = buildFilePath(omeDir.getAbsolutePath(), "${imageName}.ome.tif")

    // Escribir la imagen completa (con downsampling a 1)
    def server = imageData.getServer()  // Obtener el servidor de la imagen
    def requestFull = RegionRequest.createInstance(server, 1)  // Crear la solicitud de región con downsampling 1
    writeImageRegion(server, requestFull, pathOutput)  // Exportar la imagen completa

    print "Finished exporting: ${imageName}.ome.tif"
}

print 'All images have been exported to ome.tif directory!'
