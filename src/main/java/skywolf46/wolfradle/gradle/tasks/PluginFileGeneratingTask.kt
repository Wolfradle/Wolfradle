package skywolf46.wolfradle.gradle.tasks

import org.apache.bcel.Const
import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.pattern.PatternMatcherFactory
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import skywolf46.wolfradle.gradle.Wolfradle
import skywolf46.wolfradle.gradle.util.ClassExtractor
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.lang.IllegalStateException


open class PluginFileGeneratingTask : DefaultTask() {
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

        val resDir = sourceSet.output.classesDirs.singleFile
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