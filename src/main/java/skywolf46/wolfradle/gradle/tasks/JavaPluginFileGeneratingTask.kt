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
import skywolf46.wolfradle.gradle.data.DependencyData
import skywolf46.wolfradle.gradle.util.*
import skywolf46.wolfradle.gradle.util.GradleUtility.dependencyOf
import skywolf46.wolfradle.gradle.util.GradleUtility.findAllClass
import skywolf46.wolfradle.gradle.util.GradleUtility.fromBuildDir
import skywolf46.wolfradle.gradle.util.GradleUtility.isResourceExists
import java.io.File
import java.io.FileWriter


abstract class JavaPluginFileGeneratingTask : DefaultTask() {

    init {
        group = "wolfradle"
    }

    @Suppress("DuplicatedCode")
    @TaskAction
    fun generatePlugin() {
        if (project.isResourceExists("plugin.yml")) {
            println("Wolfradle | Original plugin.yml detected. File will not be generated.")
            didWork = false
            return
        }
        var mainClass: String? = null
        for (x in project.findAllClass("org.bukkit.plugin.java.JavaPlugin")) {
            if (x.modifier.isAbstract()) {
                println("Wolfradle | JavaPlugin found in \"${x.className}\", but class is abstract. Ignoring.")
                continue
            }
            mainClass = x.className
            break
        }
        if (mainClass == null) {
            println("Wolfradle | JavaPlugin implementation not found! Skipping.")
            didWork = false
            return
        }
        println("Wolfradle | JavaPlugin implementation detected on \"${mainClass}\".")
        println("Wolfradle | Analysing dependencies")
        val force = mutableListOf<DependencyData>()
        val soft = mutableListOf<DependencyData>()
        for (x in project.dependencyOf("wolfy")) {
            PluginUtil.extractJavaPluginInfo(x)?.run {
                force += this
                println("Wolfradle | Analysed ${pluginName}(${x.name}) as force dependency")
            }
        }
        for (x in project.dependencyOf("compile", "compileOnly")) {
            PluginUtil.extractJavaPluginInfo(x)?.run {
                if (force.contains(this))
                    return@run
                soft += this
                println("Wolfradle | Analysed ${pluginName}(${x.name}) as soft dependency")
            }
        }
        val resDir = project.fromBuildDir("wolfradle/resources", true)
        val file = File(resDir, "plugin.yml")
        if (!file.exists())
            file.createNewFile()
        FileWriter(file, false).use { stream ->
            stream.appendLine("name: ${mainClass.substring(mainClass.lastIndexOf(".") + 1)}")
            stream.appendLine("version: ${project.version}")
            stream.appendLine("main: ${mainClass}")
            stream.appendLine("api-version: 1.13")
            stream.appendLine("description: Plugin file generated with Wolfradle")
            if (force.isNotEmpty()) {
                stream.appendLine("depend:")
                for (x in force.distinct())
                    stream.appendLine("  - ${x.pluginName}")
            }
            if (soft.isNotEmpty()) {
                stream.appendLine("softdepend:")
                for (x in soft.distinct())
                    stream.appendLine("  - ${x.pluginName}")
            }
            stream.flush()
        }
        println("Wolfradle | Generated plugin.yml in ${file.path}")
        val proc: Jar = project.tasks.getByPath("jar") as Jar
        proc.from(file)
        didWork = true
    }

}

