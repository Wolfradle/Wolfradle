package skywolf46.wolfradle.gradle.data

data class DependencyData(val pluginName: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DependencyData
        if (pluginName != other.pluginName) return false
        return true
    }

    override fun hashCode(): Int {
        return pluginName.hashCode()
    }
}