package skywolf46.wolfradle.gradle.tasks

import groovy.lang.Closure
import org.apache.bcel.Const
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.internal.file.copy.DefaultCopySpec
import org.gradle.api.internal.file.copy.DestinationRootCopySpec
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Property
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.work.Incremental
import skywolf46.wolfradle.gradle.Wolfradle
import skywolf46.wolfradle.gradle.util.ClassExtractor
import java.io.File
import java.io.FileWriter


abstract class PluginFileGeneratingTask : DefaultTask() {

    @TaskAction
    fun generatePlugin() {
//        if (property("disabled") == true) {
//            println("Wolfradle | Task disabled; Skipping.")
//            return
//        }
        println("Wolfradle | Distinction duplicate dependencies...")
        val force = Wolfradle.forceDependation.distinct()
        val soft = Wolfradle.softDependation.distinct()
        println("Wolfradle | Generating plugin data file...")
        val pl = project.convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSet = pl.sourceSets.getByName("main")
        val res = sourceSet.resources.sourceDirectories
        if (!res.isEmpty) {
            if (res.singleFile.isDirectory) {
                for (x in res.singleFile.listFiles()) {
                    if (x.name == "plugin.yml") {
                        println("Wolfradle | Original plugin.yml detected. File will not be generated.")
                        return
                    }
                }
            }
        }
        val cls = mutableListOf<File>()
        for (x in sourceSet.output.classesDirs) {
            parseFileListOf(x, cls)
        }
        var mainClass: String? = null
        for (x in cls) {
            if (x.name.endsWith(".class")) {
                val extr = ClassExtractor.extract(x)
                if (extr == null) {
                    continue
                }
//                println("Class ${x.name} -> Package ${extr.superClass}")
                if (extr.modifier.and(Const.ACC_ABSTRACT.toInt()) != 0) {
                    println("Wolfradle | JavaPlugin found in ${extr.className}, but class is abstract. Ignoring.")
                    continue
                }
                if (extr.superClass?.equals("org.bukkit.plugin.java.JavaPlugin") == true) {
                    mainClass = extr.className
                    break
                }
            }
        }
        if (mainClass == null) {
            throw IllegalStateException("Error: JavaPlugin implmentation not found in project ${project.name}")
        }
//        val resDir = File(sourceSet.output.resourcesDir!!.parentFile, "wolfradle/resources")
//        val resDir = destinationDir
//        val resDir = sourceSet.output.resourcesDir!!
        val resDir = project.layout.buildDirectory.file("wolfradle/resources").get().asFile
//        val resDir = getOutputDir().asFile.get()
        if (!resDir.exists())
            resDir.mkdirs()
        val file = File(resDir, "plugin.yml")
        val stream = FileWriter(file, false)
        stream.appendLine("name: ${mainClass.substring(mainClass.lastIndexOf(".") + 1)}")
        stream.appendLine("version: ${project.version}")
        stream.appendLine("main: ${mainClass}")
        stream.appendLine("description: Plugin file generated with Wolfradle")
        if (force.isNotEmpty()) {
            stream.appendLine("depend:")
            for (x in force)
                stream.appendLine("  - ${x.pluginName}")
        }
        if (soft.isNotEmpty()) {
            stream.appendLine("softdepend:")
            for (x in soft)
                stream.appendLine("  - ${x.pluginName}")
        }
        stream.flush()
        stream.close()

        println("Wolfradle | Generated plugin.yml in ${file.path}")
        val proc: Jar = project.tasks.getByPath("jar") as Jar
        proc.from(file)
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