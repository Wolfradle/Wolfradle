package skywolf46.wolfradle.gradle

import groovy.lang.Closure
import groovy.lang.GroovyObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.plugins.ExtraPropertiesExtension
import skywolf46.wolfradle.gradle.data.DependencyData
import skywolf46.wolfradle.gradle.tasks.BungeePluginFileGeneratingTask
import skywolf46.wolfradle.gradle.tasks.JavaPluginFileGeneratingTask
import skywolf46.wolfradle.gradle.util.PluginUtil
import java.io.File
import java.net.URI

class Wolfradle : Plugin<Project> {
    companion object {
        val softDependation = mutableListOf<DependencyData>()
        val forceDependation = mutableListOf<DependencyData>()
    }

    override fun apply(project: Project) {
        softDependation.clear()
        forceDependation.clear()
        println("Wolfradle | Waiting for Java plugin initialize")
        project.plugins.apply("java")
//        project.plugins.apply("org.jetbrains.kotlin.jvm")
        println("Wolfradle | Setting up Skywolf repositories")
        project.repositories.maven {
            it.url = URI("http://dja.kr:55201/spigot")
            it.isAllowInsecureProtocol = true
        }
        project.repositories.maven {
            it.url = URI("http://dja.kr:55201/releases")
            it.isAllowInsecureProtocol = true
        }

        println("Wolfradle | Registering task")
        project.tasks.register("generateJavaPluginFile", JavaPluginFileGeneratingTask::class.java)
        project.tasks.register("generateBungeePluginFile", BungeePluginFileGeneratingTask::class.java)
        project.tasks.getByPath("processResources").dependsOn("generateJavaPluginFile")
        project.tasks.getByPath("processResources").dependsOn("generateBungeePluginFile")
        println("Wolfradle | Registering extension methods")

        val compileDeps = project.configurations.getByName("compileOnly").dependencies
//        val compileOnlyDeps = project.configurations.getByName("compileOnly").dependencies
        val wolfyDepsConf = project.configurations.create("wolfy")
        val wolfyDeps = wolfyDepsConf.dependencies
        project.configurations.add(wolfyDepsConf)
        project.dependencies.groovyExtension.set("kotlin", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:${version ?: "1.4.32"}"))
            }
        })


        project.dependencies.groovyExtension.set("wolfyCommand", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:commandannotation:${version ?: "latest.release"}"))
            }
        })

        project.dependencies.groovyExtension.set("spigot", object : Closure<Any>(this, this) {
            fun doCall(version: String) = "org.spigotmc:spigot:$version"
        })

        project.dependencies.groovyExtension.set("spigotApi", object : Closure<Any>(this, this) {
            fun doCall(version: String) = run {
                wolfyDeps.add(project.dependencies.create("org.spigotmc:spigot:$version"))
            }
        })


        project.dependencies.groovyExtension.set("bungeeApi", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("org.spigotmc:bungeecord:${version ?: "latest.release"}"))
            }
        })

        project.dependencies.groovyExtension.set("wolfyBasePackage", object : Closure<Any>(this, this) {
            fun doCall(addLoader: Boolean) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:exutil:latest.release"))
                wolfyDeps.add(project.dependencies.create("skywolf46:commandannotation:latest.release"))
                if (addLoader) {
                    wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-core:latest.release"))
                    wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-file:latest.release"))
                    wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-mysql:latest.release"))
                }
            }
        })

        project.dependencies.groovyExtension.set("wolfyUtil", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:exutil:${version ?: "latest.release"}"))
            }
        })


        project.dependencies.groovyExtension.set("wolfyMessage", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:placeholders:${version ?: "latest.release"}"))
            }
        })

        project.dependencies.groovyExtension.set("wolfyUI", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:iui:${version ?: "latest.release"}"))
            }
        })



        project.dependencies.groovyExtension.set("wolfyUIPackage", object : Closure<Any>(this, this) {
            fun doCall(addLoader: Boolean) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:exutil:latest.release"))
                wolfyDeps.add(project.dependencies.create("skywolf46:iui:latest.release"))
                wolfyDeps.add(project.dependencies.create("skywolf46:commandannotation:latest.release"))
                if (addLoader) {
                    wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-core:latest.release"))
                    wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-file:latest.release"))
                    wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-mysql:latest.release"))
                }
            }
        })

        project.dependencies.groovyExtension.set("wolfyLoader", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-core:${version ?: "latest.release"}"))
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-file:${version ?: "latest.release"}"))
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-mysql:${version ?: "latest.release"}"))
            }
        })

        project.dependencies.groovyExtension.set("wolfyFileLoader", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-core:${version ?: "latest.release"}"))
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-file:${version ?: "latest.release"}"))
            }
        })

        project.dependencies.groovyExtension.set("wolfySQLLoader", object : Closure<Any>(this, this) {
            fun doCall(version: String?) = run {
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-core:${version ?: "latest.release"}"))
                wolfyDeps.add(project.dependencies.create("skywolf46:asyncdataloader-mysql:${version ?: "latest.release"}"))
                compileDeps.add(project.dependencies.create("skywolf46:asyncdataloader-file:${version ?: "latest.release"}"))
            }
        })


        println("Wolfradle | Adding dependency listener")

        project.gradle.addListener(object : DependencyResolutionListener {
            override fun beforeResolve(p0: ResolvableDependencies) {
                compileDeps.addAll(wolfyDeps)
                project.gradle.removeListener(this)
            }

            override fun afterResolve(p0: ResolvableDependencies) {

            }

        })
    }
}

internal val Any.groovyExtension get() = (this as GroovyObject).getProperty("ext") as ExtraPropertiesExtension