package skywolf46.wolfradle.gradle.data

import org.apache.bcel.Const

class ClassModifiers(val modifier: Int) {
    fun isAbstract() = modifier.and(Const.ACC_ABSTRACT.toInt()) != 0
}