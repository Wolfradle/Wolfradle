package skywolf46.wolfradle.gradle.util

import java.io.File

object FileCollector {
    fun parseFileListOf(vararg file: File): List<File> {
        val files = mutableListOf<File>()
        for (x in file)
            parseFileListOf(x, files)
        return files
    }

    fun parseFileListOf(file: File, lst: MutableList<File>): List<File> {
        if (!file.isDirectory) {
            if (file.extension == "class")
                lst.add(file)
        } else {
            for (x in file.listFiles())
                parseFileListOf(x, lst)
        }
        return lst
    }
}