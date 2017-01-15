package hr.fer.zemris.sandworm.packer

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File


fun main(args: Array<String>) {

    val options = Options().apply {
        addOption("b", "base-image", true, "Define the base image to build upon (e.g. sandworm/base/c_cpp")
        addOption("s", "source-directory", true, "The location from which to take the source and the input files")
        addOption("p", "image-prefix", true, "The tag prefix of the generated images (e.g. sandworm/images/some_id)")
    }

    val parser = DefaultParser()

    val commandLine = parser.parse(options, args)

    val hasAllOptions = arrayOf('b', 's', 'p').all { commandLine.hasOption(it) }
    if (!hasAllOptions) {
        HelpFormatter().printHelp("packer", options)
        return
    }

    Packer(RemoteLogger("http://localhost:8080")).pack(
            File(commandLine.getOptionValue('s')),
            commandLine.getOptionValue('b'),
            commandLine.getOptionValue('p')
    )

}
