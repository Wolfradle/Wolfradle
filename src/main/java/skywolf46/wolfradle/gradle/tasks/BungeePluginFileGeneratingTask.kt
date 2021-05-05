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


abstract class BungeePluginFileGeneratingTask : DefaultTask() {

    init {
        group = "wolfradle"
    }

    @Suppress("DuplicatedCode")
    @TaskAction
    fun generatePlugin() {
        if (project.isResourceExists("bungee.yml")) {
            println("Wolfradle | Original bungee.yml detected. File will not be generated.")
            didWork = false
            return
        }
        var mainClass: String? = null
        for (x in project.findAllClass("net.md_5.bungee.api.plugin.Plugin")) {
            if (x.modifier.isAbstract()) {
                println("Wolfradle | Bungeecord Plugin implementation found in ${x.className}, but class is abstract. Ignoring.")
                continue
            }
            mainClass = x.className
            break
        }
        if (mainClass == null) {
            println("Wolfradle | Bungeecord Plugin implementation not found! Skipping.")
            didWork = false
            return
        }
        val force = mutableListOf<DependencyData>()
        val soft = mutableListOf<DependencyData>()
        println("Wolfradle | Bungeecord Plugin implementation detected on \"${mainClass}\".")
        println("Wolfradle | Analysing dependencies")

        for (x in project.dependencyOf("wolfy")) {
            PluginUtil.extractBungeePluginInfo(x)?.run {
                force += this
                println("Wolfradle | Analysed ${pluginName}(${x.name}) as force dependency")
                if (JarUtility.contains(x, "plugin.yml") && !JarUtility.contains(x, "bungee.yml")) {
                    println("Wolfradle | Plugin ${pluginName}(${x.name}) not including bungee.yml, but plugin.yml. Plugin may not be a Bungeecord plugin.")
                    println("Wolfradle | SuperClass analysing feature will be added to wolfradle later. Wolfradle cannot detect plugin availability in this release.")
                }
            }
        }
        for (x in project.dependencyOf("compile", "compileOnly")) {
            PluginUtil.extractBungeePluginInfo(x)?.run {
                if (force.contains(this))
                    return@run
                soft += this
                println("Wolfradle | Analysed ${pluginName}(${x.name}) as soft dependency")
                if (JarUtility.contains(x, "plugin.yml") && !JarUtility.contains(x, "bungee.yml")) {
                    println("Wolfradle | Plugin ${pluginName}(${x.name}) not including bungee.yml, but plugin.yml. Plugin may not be a Bungeecord plugin.")
                    println("Wolfradle | SuperClass analysing feature will be added to wolfradle later. Wolfradle cannot detect plugin availability in this release.")
                }
            }
        }
        val resDir = project.fromBuildDir("wolfradle/resources", true)
        val file = File(resDir, "bungee.yml")
        if (!file.exists())
            file.createNewFile()
        FileWriter(file, false).use { stream ->
            stream.appendLine("name: ${mainClass.substring(mainClass.lastIndexOf(".") + 1)}")
            stream.appendLine("version: ${project.version}")
            stream.appendLine("main: ${mainClass}")
            stream.appendLine("description: Plugin file generated with Wolfradle")
            if (force.isNotEmpty()) {
                stream.appendLine("depend:")
                for (x in force.distinct())
                    stream.appendLine("  - $x")
            }
            if (soft.isNotEmpty()) {
                stream.appendLine("softdepend:")
                for (x in soft.distinct())
                    stream.appendLine("  - $x")
            }
            stream.flush()
        }
        println("Wolfradle | Generated plugin.yml in ${file.path}")
        val proc: Jar = project.tasks.getByPath("jar") as Jar
        proc.from(file)
        didWork = true
    }

}
