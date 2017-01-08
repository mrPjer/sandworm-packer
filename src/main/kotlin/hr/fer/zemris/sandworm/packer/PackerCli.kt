package hr.fer.zemris.sandworm.packer

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File


fun main(args: Array<String>) {

    val options = Options().apply {
        addOption("b", "base-image", true, "Define the base image to build upon (e.g. sandworm/base/c_cpp")
        addOption("s", "source-directory", true, "The location from which to take the source and the input files")
    }

    val parser = DefaultParser()

    val commandLine = parser.parse(options, args)

    if (!commandLine.hasOption('b') || !commandLine.hasOption('s')) {
        HelpFormatter().printHelp("packer", options)
        return
    }

    Packer.pack(
            File("src/main/resources/c_sample"),
            commandLine.getOptionValue('b'),
            commandLine.getOptionValue('s')
    )

}
