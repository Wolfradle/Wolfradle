package skywolf46.wolfradle.gradle.util

import org.gradle.api.artifacts.Dependency
import org.yaml.snakeyaml.Yaml
import skywolf46.wolfradle.gradle.data.DependencyData
import java.io.File

object PluginUtil {
    fun extractYamlInfo(
        dep: Dependency?,
        file: File,
        vararg yamlName: String,
    ): DependencyData? {
        JarUtility.extract(file, *yamlName)?.use {
            val yaml = Yaml()
            val loaded = yaml.load(it) as Map<String, Any>
            if (!loaded.containsKey("name")) {
                if (dep != null)
                    System.err.println("Wolfradle | Plugin ${file.name} (Known as ${dep.group}:${dep.name}:${dep.version}) not contains valid $yamlName; No name defined")
                return null
            }

            if (!loaded.containsKey("main")) {
                if (dep != null)
                    System.err.println("Wolfradle | Plugin ${file.name} (Known as ${dep.group}:${dep.name}:${dep.version}) not contains valid $yamlName; No main class defined")
                return null
            }
            val cls = JarUtility.extractWithImplementation(file, loaded["main"].toString())
            if (cls == null) {
                if (dep != null)
                    System.err.println("Wolfradle | Plugin ${file.name} (Known as ${dep.group}:${dep.name}:${dep.version}) defined main class, but main class is not exists. It can cause error at runtime.")
            }
            return DependencyData(loaded["name"].toString())
        } ?: kotlin.run {
            return null
        }
    }


    fun extractJavaPluginInfo(file: File): DependencyData? = extractYamlInfo(null, file, "plugin.yml")
    fun extractBungeePluginInfo(file: File): DependencyData? = extractYamlInfo(null, file, "bungee.yml", "plugin.yml")


}