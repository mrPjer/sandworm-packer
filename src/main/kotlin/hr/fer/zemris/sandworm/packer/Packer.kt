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

            log("packer/init", "Checking directory structure")
            if (!hasProperDirectoryStructure(inputDefinitionDirectory)) {
                log("packer/init", "Directory structure not OK!")
                throw IllegalArgumentException("Input definition is not properly formed!")
            }

            log("packer/init", "Directory structure OK!")

            val dockerClient = DockerClientBuilder.getInstance().build()

            log("packer/build/source", "Building source image...")
            val sourceImageName = buildImageWithExtraDirectory(
                    dockerClient,
                    baseImage,
                    "$imagePrefix/source",
                    File(inputDefinitionDirectory, SOURCE_DIR),
                    { log("packer/build/source", it) }
            )
            // TODO push image
            log("packer/build/source", "Built source image $sourceImageName")

            log("packer/build/definition", "Building definition images")
            File(inputDefinitionDirectory, INPUTS_DIR)
                    .listFiles()
                    .filter { it.isDirectory }
                    .map { inputDefinition ->
                        val tag = "packer/build/definition/${inputDefinition.name}"
                        log(
                                tag,
                                "Building definition image for definition ${inputDefinition.name}"
                        )
                        val imageName = buildImageWithExtraDirectory(
                                dockerClient,
                                sourceImageName,
                                "$imagePrefix/compiled/${inputDefinition.name}",
                                inputDefinition,
                                { message -> log(tag, message) }
                        )
                        log(
                                tag,
                                "Built definition image $imageName"
                        )
                        imageName
                    }.forEach { imageName -> log("packer/build/definition", "Pushing image $imageName") }
        }

        fun hasProperDirectoryStructure(inputDefinitionDirectory: File) =
                inputDefinitionDirectory.isDirectory
                        && File(inputDefinitionDirectory, SOURCE_DIR).isDirectory
                        && File(inputDefinitionDirectory, INPUTS_DIR).isDirectory

        fun buildImageWithExtraDirectory(dockerClient: DockerClient, baseImage: String, imageTag: String, sourceDirectory: File, log: (String) -> Unit): String {
            val temporaryDirectory = createTempDir()
            sourceDirectory.copyRecursively(temporaryDirectory, false)
            writeCopyingDockerfile(baseImage, temporaryDirectory)

            val builtImageId = buildDockerImage(dockerClient, temporaryDirectory, imageTag, log)

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
            RUN ./compile.sh
            """.trim()

            outputFile.writeText(dockerfileContents)

            return outputFile
        }

        fun buildDockerImage(dockerClient: DockerClient, imageDirectory: File, tag: String, log: (String) -> Unit) =
                dockerClient
                        .buildImageCmd(imageDirectory)
                        .withTag(tag.trim().replace(' ', '_'))
                        .exec(object : BuildImageResultCallback() {
                            override fun onNext(item: BuildResponseItem) {
                                log(item.stream.toString())
                                super.onNext(item)
                            }
                        }).awaitImageId()

        fun log(tag: String, message: String) {
            // TODO log to logger server
            println("$tag :: $message")
        }

    }

}
