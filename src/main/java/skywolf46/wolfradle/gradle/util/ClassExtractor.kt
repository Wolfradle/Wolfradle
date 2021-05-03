package skywolf46.wolfradle.gradle.util

import org.apache.bcel.Const
import org.apache.bcel.classfile.ConstantPool
import org.apache.bcel.classfile.Utility
import skywolf46.wolfradle.gradle.data.ClassInformation
import java.io.DataInputStream
import java.io.File

object ClassExtractor {
    private val JVM_CLASSIFIER = 0xCAFEBABE
    fun extract(file: File): ClassInformation? {
        val stream = DataInputStream(file.inputStream())
        // Skipping identifier
        stream.readInt()
//        if (stream.readInt().toLong() != JVM_CLASSIFIER)
//            return null
        // Skip java version
        // Skip minor
        stream.readUnsignedShort()
        // Skip major
        stream.readUnsignedShort()
        // Read constant pool
        val constPool = ConstantPool(stream)
        // Skipping class flag
        val modifier = stream.readUnsignedShort()
        // Now we are talking. Reading class info!
        val classNameIndex = stream.readUnsignedShort()
        val superClassNameIndex = stream.readUnsignedShort()
        val className =
            Utility.compactClassName(constPool.getConstantString(classNameIndex, Const.CONSTANT_Class), false)
        val superClassName =
            if (superClassNameIndex > 0)
                Utility.compactClassName(constPool.getConstantString(superClassNameIndex, Const.CONSTANT_Class), false)
            else null

        // Close stream and return
        stream.close()
        return ClassInformation(modifier, className, superClassName)
    }
}