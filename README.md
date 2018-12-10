
# ImageResizer

This is a Java tool for mapping/synchronizing a folder hierarchy into a new folder hierarchy with the following properties:
- Only images and videos are kept
- The exif date of images are prepended to their filename
- The images gets down-scaled and compressed



**Note** This program is still in progress.

## Overview

- **Use case** Describes the real-life setting for which I use this program.
- **How to use** How to compile and run the program.
- **Source code overview** A short overview of the classes and important methods.
- **Licenses and attribution** Licenses and attribution to third party libraries used in this project.
- **The "name" ImageResizer** About the name of the project.

## Use case

TODO

## How to use

Make sure you are in the root directory of the project.

### Compile

To compile the program, you need to have `ant` installed.

- `ant clean` for cleaning the build directory.
- `ant compile` for compiling.
- `ant jar` for compiling and packaing into a `.jar` file.
- `ant run ...` for running. See more about the needed arguments in the next section.
- `ant` for all the above.

### Run

The program is invoked as `java -jar TheProgram.jar src dst m1 m2 q` where

- `src` is the path to the source folder of images.
- `dst` is the path to the destination folder, where compressed images will be.
- `m1` is `1` if create/delete file messsages should be printed, otherwise `0`.
- `m2` is `1` if the folders entered and exited should be printed, otherwise is `0`.
- `q` is `M` (for mobile) or `T` (for TV).

For more details about the arguments, check the section [Use case](#Use-case).

The `.jar` file can be moved to any folder.

## Source code overview

TODO

## Licenses and attribution

This software uses third party libraries distributed under their own terms, see LICENSES-3RD-PARTY.txt. The third party libraries mentioned above are the following:

- Apache Commons IO: https://commons.apache.org/proper/commons-io/index.html
- metadata-extractor: https://github.com/drewnoakes/metadata-extractor
- Thumbnailator: https://github.com/coobird/thumbnailator

A big THANK YOU to them! Without their software, I couldn't have made this program. 

The binary forms of them are in the `lib/` folder. The files, as they are here, has not been modified at all, with one exception: for Apache Commons IO, I have put the original `docs/` folder into a corresponding `docs.zip` file.

## The "name" ImageResizer

The program is an image resizing program. The "name" ImageResizer is more like an adjective, describing what the progam does, rather than a name of it. By this, I mean that I don't want to infringe on anyone having the rights for the name ImageResizer.
