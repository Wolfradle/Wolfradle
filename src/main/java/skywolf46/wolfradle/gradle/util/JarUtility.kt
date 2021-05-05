package skywolf46.wolfradle.gradle.util

import skywolf46.wolfradle.gradle.data.ClassInformation
import java.io.DataInputStream
import java.io.File
import java.util.jar.JarFile

object JarUtility {
    fun extract(jarFile: File, vararg fileName: String): DataInputStream? {
        val jf = JarFile(jarFile)
        for (x in fileName) {
            val entry = jf.getJarEntry(x)
            return if (entry == null) continue else DataInputStream(jf.getInputStream(entry))
        }
        return null
    }

    fun extractWithImplementation(jarFile: File, vararg acceptableParent: String) =
        extractAllWithImplementation(jarFile, *acceptableParent).let {
            return@let if (it.isEmpty()) null else it[0]
        }

    // TODO Add cached extraction to extract superclass of superclass
    fun extractAllWithImplementation(jarFile: File, vararg acceptableParent: String): List<ClassInformation> {
        val checkList = listOf(*acceptableParent)
        val extracted = mutableListOf<ClassInformation>()
        val jf = JarFile(jarFile)
        for (x in jf.entries()) {
            if (x.name.endsWith(".class")) {
                DataInputStream(jf.getInputStream(x)).use {
                    val inform = ClassExtractor.extract(it)
                    if (checkList.contains(inform.superClass)) {
                        extracted += inform
                    }
                }
            }
        }
        return extracted
    }

    fun contains(jarFile: File, fileName: String): Boolean {
        return JarFile(jarFile).getJarEntry(fileName) != null
    }
}