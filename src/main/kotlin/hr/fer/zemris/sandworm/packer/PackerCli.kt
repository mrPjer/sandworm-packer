package hr.fer.zemris.sandworm.packer

import java.io.File

fun main(args: Array<String>) {

    Packer.pack(
            File("src/main/resources/c_sample"),
            "sandworm/base/c_cpp",
            "sandworm/images/"
    )

}
