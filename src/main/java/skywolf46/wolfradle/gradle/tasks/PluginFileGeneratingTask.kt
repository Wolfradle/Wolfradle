package skywolf46.wolfradle.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskAction
import java.io.File


open class PluginFileGeneratingTask : DefaultTask() {
    @TaskAction
    fun generatePlugin(project: Project) {
        println("Generating plugin data file...")
        val ssc = project.convention.getPlugin(
            JavaPluginConvention::class.java).sourceSets
        val classesDir: FileCollection = ssc.getByName("main").output.classesDirs
        val lstFile = mutableListOf<File>()
        for (x in classesDir) {
            parseFileListOf(x, lstFile)
        }
    }
}

fun parseFileListOf(file: File, lst: MutableList<File>) {
    if (!file.isDirectory) {
        if (file.extension == "class")
            lst.add(file)
    } else {
        for (x in file.listFiles())
            parseFileListOf(x, lst)
    }
}