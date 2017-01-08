package hr.fer.zemris.sandworm.packer

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.BuildResponseItem
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.BuildImageResultCallback
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

            val dockerClient = DockerClientBuilder.getInstance().build()

            log("Building source image...")
            val sourceImageName = buildImageWithExtraDirectory(
                    dockerClient,
                    baseImage,
                    "$imagePrefix/source",
                    File(inputDefinitionDirectory, SOURCE_DIR)
            )
            // TODO push image
            log("Built source image $sourceImageName")

            log("Building definition images")
            File(inputDefinitionDirectory, INPUTS_DIR)
                    .listFiles()
                    .filter { it.isDirectory }
                    .map { inputDefinition ->
                        log("Building definition image for definition ${inputDefinition.name}")
                        val imageName = buildImageWithExtraDirectory(
                                dockerClient,
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

        fun buildImageWithExtraDirectory(dockerClient: DockerClient, baseImage: String, imageTag: String, sourceDirectory: File): String {
            val temporaryDirectory = createTempDir()
            sourceDirectory.copyRecursively(temporaryDirectory, true)
            writeCopyingDockerfile(baseImage, temporaryDirectory)

            val builtImageId = buildDockerImage(dockerClient, temporaryDirectory)
            // TODO tag the image

            temporaryDirectory.deleteRecursively()
            return builtImageId
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

        fun buildDockerImage(dockerClient: DockerClient, imageDirectory: File) =
                dockerClient.buildImageCmd(imageDirectory).exec(object : BuildImageResultCallback() {
                    override fun onNext(item: BuildResponseItem) {
                        log(item.toString())
                        super.onNext(item)
                    }
                }).awaitImageId()

        fun log(message: String) {
            // TODO log to logger server
            println(message)
        }

    }

}
