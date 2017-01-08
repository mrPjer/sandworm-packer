package hr.fer.zemris.sandworm.packer

import java.io.File

class Packer {

    companion object {

        const val SOURCE_DIR = "source"
        const val INPUTS_DIR = "inputs"
        const val DOCKERFILE = "Dockerfile"

        fun pack(inputDefinitionDirectory: File, baseImage: String, imagePrefix: String) {

            log("Checking directory structure")
            if (!hasProperDirectoryStructure(inputDefinitionDirectory)) {
                log("Directory structure not OK!")
                throw IllegalArgumentException("Input definition is not properly formed!")
            }

            log("Directory structure OK!")

            log("Building source image...")
            val sourceImageName = buildImageWithSource(baseImage, "$imagePrefix/source", File(inputDefinitionDirectory, SOURCE_DIR))
            log("Built source image $sourceImageName")

            log("Building definition images")
            File(inputDefinitionDirectory, INPUTS_DIR)
                    .listFiles()
                    .filter { it.isDirectory }
                    .map { inputDefinition ->
                        log("Building definition image for definition ${inputDefinition.name}")
                        val imageName = buildImageWithSourceAndDefinition(
                                sourceImageName,
                                "$imagePrefix/compiled/${inputDefinition.name}",
                                inputDefinition
                        )
                        log("Built definition image $imageName")
                        imageName
                    }.forEach { imageName ->
                log("Pushing image $imageName")
                // TODO push image
            }

        }

        fun hasProperDirectoryStructure(inputDefinitionDirectory: File) =
                inputDefinitionDirectory.isDirectory
                        && File(inputDefinitionDirectory, SOURCE_DIR).isDirectory
                        && File(inputDefinitionDirectory, INPUTS_DIR).isDirectory

        fun buildImageWithSource(baseImage: String, imageTag: String, sourceDirectory: File): String {
            val temporaryDirectory = createTempDir()
            sourceDirectory.copyRecursively(temporaryDirectory, true)
            writeCopyingDockerfile(baseImage, temporaryDirectory)

            // TODO docker build and tag

            temporaryDirectory.deleteRecursively()
            return imageTag
        }

        fun buildImageWithSourceAndDefinition(baseImage: String, imageTag: String, inputDefinitionsDirectory: File): String {
            val temporaryDirectory = createTempDir()

            inputDefinitionsDirectory.copyRecursively(temporaryDirectory, true)

            writeCopyingDockerfile(baseImage, temporaryDirectory)

            // TODO docker build and tag

            temporaryDirectory.deleteRecursively()

            return imageTag
        }

        fun writeCopyingDockerfile(baseImage: String, targetDirectory: File): File {
            val outputFile = File(targetDirectory, DOCKERFILE)

            if (outputFile.exists()) {
                outputFile.deleteRecursively()
            }

            val dockerfileContents = """
            FROM $baseImage
            COPY . .
            """.trim()

            outputFile.writeText(dockerfileContents)

            return outputFile
        }

        fun log(message: String) {
            // TODO log to logger server
            println(message)
        }

    }

}
