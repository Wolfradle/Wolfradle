package skywolf46.wolfradle.gradle

import groovy.lang.Closure
import groovy.lang.GroovyObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.classloader.VisitableURLClassLoader
import org.gradle.plugins.ide.idea.model.ModuleDependency
import skywolf46.wolfradle.gradle.tasks.PluginFileGeneratingTask
import java.io.File
import java.net.URI

class Wolfradle : Plugin<Project> {
    override fun apply(p0: Project) {
        println("Waiting for Java plugin initialize")
        p0.plugins.apply("java")
        println("Setting up Skywolf repositories")
        p0.repositories.maven {
            it.url = URI("http://dja.kr:55201/spigot")
        }
        p0.repositories.maven {
            it.url = URI("http://dja.kr:55201/releases")
        }
        println("Registering task")
        p0.tasks.register("generatePluginFile", PluginFileGeneratingTask::class.java)
        p0.tasks.getByPath("assemble").dependsOn("generatePluginFile")
        println("Registering extension methods")
        val compileDeps = p0.getConfigurations().getByName("compile").getDependencies()
        p0.dependencies.groovyExtension.set("spigot", object : Closure<Any>(this, this) {
            fun doCall(version: String) = "org.spigotmc:spigot:$version"
        })

        p0.dependencies.groovyExtension.set("spigotApi", object : Closure<Any>(this, this) {
            fun doCall(version: String) = run {
                compileDeps.add(p0.dependencies.create("org.spigotmc:spigot:$version"))
            }
        })


        p0.dependencies.groovyExtension.set("wolfyBase", object : Closure<Any>(this, this) {
            fun doCall(addLoader: Boolean) = run {
                compileDeps.add(p0.dependencies.create("skywolf46:exutil:latest.release"))
                compileDeps.add(p0.dependencies.create("skywolf46:commandannotation:latest.release"))
                if (addLoader) {
                    compileDeps.add(p0.dependencies.create("skywolf46:asyncdataloader-core:latest.release"))
                    compileDeps.add(p0.dependencies.create("skywolf46:asyncdataloader-file:latest.release"))
                    compileDeps.add(p0.dependencies.create("skywolf46:asyncdataloader-mysql:latest.release"))
                }
            }
        })


        p0.dependencies.groovyExtension.set("wolfyUI", object : Closure<Any>(this, this) {
            fun doCall(addLoader: Boolean) = run {
                compileDeps.add(p0.dependencies.create("skywolf46:exutil:latest.release"))
                compileDeps.add(p0.dependencies.create("skywolf46:iui:latest.release"))
                compileDeps.add(p0.dependencies.create("skywolf46:commandannotation:latest.release"))
                if (addLoader) {
                    compileDeps.add(p0.dependencies.create("skywolf46:asyncdataloader-core:latest.release"))
                    compileDeps.add(p0.dependencies.create("skywolf46:asyncdataloader-file:latest.release"))
                    compileDeps.add(p0.dependencies.create("skywolf46:asyncdataloader-mysql:latest.release"))
                }
            }
        })


//        println("Configuring buildscript classpath")
//        p0.buildscript.repositories.maven {
//            it.url = URI("http://dja.kr:55201/")
//        }
//        val buildDeps = p0.buildscript.configurations.getByName("classpath").dependencies
//        println("Adding Wolfradle to classpath")
//        buildDeps.add(p0.buildscript.dependencies.create("skywolf46:wolfgradle:1.0.30"))
//        println(p0.buildscript.classLoader)
//        val load = p0.buildscript.classLoader as VisitableURLClassLoader
//        load.addURL(Wolfradle::class.java.protectionDomain.codeSource.location)
    }
}

internal val Any.groovyExtension get() = (this as GroovyObject).getProperty("ext") as ExtraPropertiesExtension