package skywolf46.wolfradle.gradle.util

import org.gradle.api.artifacts.Dependency
import org.yaml.snakeyaml.Yaml
import skywolf46.wolfradle.gradle.data.DependencyData
import java.io.File
import java.util.jar.JarFile

object PluginUtil {
    fun extractPluginInfo(dep: Dependency?, file: File): DependencyData? {
        val jf = JarFile(file)
        val entry = jf.getJarEntry("plugin.yml") ?: return null
        val stream = jf.getInputStream(entry)
        val yaml = Yaml()
        val loaded = yaml.load(stream) as Map<String, Object>
        if (!loaded.containsKey("name")) {
            if (dep != null)
                System.err.println("Wolfradle | Plugin ${file.name} (Known as ${dep.group}:${dep.name}:${dep.version}) not contains plugin.yml; Identified as illegal plugin.")
            return null
        }
        return DependencyData(loaded["name"].toString())
    }


    fun extractPluginInfo(file: File): DependencyData? = extractPluginInfo(null, file)
}