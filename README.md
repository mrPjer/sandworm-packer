Packer
======

A library to build executable Docker images based on a source file and a list of input directories.

Quickstart
----------

1. Build with `./gradlew shadowJar`
2. Run `java -jar build/libs/packer-1.0-SNAPSHOT-all.jar --base-image sandworm/base/c_cpp --source-directory src/main/resources/c_sample --image-prefix sandworm/images/test_image`
3. Run the compiled Docker images with `docker run --rm -it sandworm/images/test_image/compiled/hello_petar` and `docker run --rm -it sandworm/images/test_image/compiled/hello_world`

Directory format
----------------

Right now, Packer expects a directory containing the source and the input files in the following structure:

```
source/
    main.c
    some_other_source_file.c
inputs/
    input description/
        stdin
        some_other_file
```

Base images
-----------

See the project [sandworm-images](../sandworm-images) for a list of available base images.
