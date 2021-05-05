package skywolf46.wolfradle.gradle.data

class ClassInformation(modifier: Int, val className: String, val superClass: String?) {
    val modifier = ClassModifiers(modifier)
}