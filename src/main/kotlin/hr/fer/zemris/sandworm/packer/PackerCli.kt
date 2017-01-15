package hr.fer.zemris.sandworm.packer

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File
import java.util.*


fun main(args: Array<String>) {

    val options = Options().apply {
        addOption("b", "base-image", true, "Define the base image to build upon (e.g. sandworm/base/c_cpp")
        addOption("s", "source-directory", true, "The location from which to take the source and the input files")
        addOption("p", "image-prefix", true, "The tag prefix of the generated images (e.g. sandworm/images/some_id)")
        addOption("l", "logger-endpoint", true, "The (optional) location of a remote sandworm logger (e.g. http://localhost:8080)")
        addOption("i", "task-id", true, "A (preferably unique) ID of this task run which will be passed to the remote logger")
    }

    val parser = DefaultParser()

    val commandLine = parser.parse(options, args)

    val hasAllOptions = arrayOf('b', 's', 'p').all { commandLine.hasOption(it) }
    if (!hasAllOptions) {
        HelpFormatter().printHelp("packer", options)
        return
    }

    val taskId = commandLine.getOptionValue("i") ?: UUID.randomUUID().toString()

    val endpoint = commandLine.getOptionValue("l")
    val logger = endpoint?.let({ RemoteLogger(taskId, endpoint) })

    Packer(logger).pack(
            File(commandLine.getOptionValue('s')),
            commandLine.getOptionValue('b'),
            commandLine.getOptionValue('p')
    )

}
