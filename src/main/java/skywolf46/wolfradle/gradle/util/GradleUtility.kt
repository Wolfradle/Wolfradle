package skywolf46.wolfradle.gradle.util

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import skywolf46.wolfradle.gradle.data.ClassInformation
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object GradleUtility {

    fun Project.getSourceSet(setName: String): SourceSet {
        val pl = project.convention.getPlugin(JavaPluginConvention::class.java)
        return pl.sourceSets.getByName(setName)
    }

    fun Project.getMainSourceSet() = project.getSourceSet("main")

    fun Project.getClassDirectory(): FileCollection = project.getSourceSet("main").output.classesDirs

    fun Project.getResourceDirectory(): FileCollection =
        project.getSourceSet("main").resources.sourceDirectories

    fun Project.isResourceExists(vararg fileNames: String): Boolean {
        return getResources(project, *fileNames).isNotEmpty()
    }

    fun Project.fromBuildDir(dirName: String, mkdir: Boolean = false): File {
        val fl = layout.buildDirectory.file(dirName).get().asFile
        if (!fl.exists() && mkdir) {
            fl.mkdirs()
        }
        return fl
    }

    fun Project.dependencyOf(vararg configuration: String): List<File> {
        val list = mutableListOf<File>()
        for (x in configuration) {
            val config = configurations.getByName(x) ?: continue
            val lst = config.dependencies
            for (deps in lst) {
                when (deps) {
                    is ProjectDependency -> {
                        // TODO add project dependencies
                    }
                    is SelfResolvingDependency -> {
                        list.addAll(deps.resolve())
                    }
                    else -> {
                        list += config.find { it.name.startsWith("${deps.name}-") } as File
                    }
                }
            }
        }
        return list
    }

    fun Project.getResources(project: Project, vararg fileNames: String): List<File> {
        val lst = mutableListOf<File>()
        val files = FileCollector.parseFileListOf(project.getMainSourceSet().resources.sourceDirectories.singleFile)
        for (x in files) {
            if (fileNames.contains(x.name))
                lst += x
        }
        return lst
    }

    fun Project.findClass(vararg superClass: String): ClassInformation? {
        val find = findAllClass(*superClass)
        return if (find.isEmpty()) null else find[0]
    }

    fun Project.findAllClass(vararg superClass: String): List<ClassInformation> {
        val directory = getClassDirectory()
        if (directory.isEmpty) {
            return emptyList()
        }
        val classes = mutableListOf<ClassInformation>()
        for (x in FileCollector.parseFileListOf(*directory.files.toTypedArray())) {
//            println("Wolfradle-Debug | Scanning ${x.name}")
            if (!x.name.endsWith(".class"))
                continue
            val info = ClassExtractor.extract(x)
//            println("Wolfradle-Debug | ${info.className} SuperClass - ${info.superClass}")
            if (superClass.contains(info.superClass)) {
                classes += info
            }
        }
        return classes
    }
}