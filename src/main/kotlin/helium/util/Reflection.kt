@file:Suppress("UNCHECKED_CAST")

package helium.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified T> T.type() = T::class.java

// field accessors
class FieldAccessor<O, T>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): T = field.get(instance) as T
  operator fun setValue(instance: O, property: KProperty<*>, value: T) = field.set(instance, value)
}
class ByteAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Byte = field.getByte(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Byte) = field.setByte(instance, value)
}
class ShortAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Short = field.getShort(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Short) = field.setShort(instance, value)
}
class IntAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Int = field.getInt(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Int) = field.setInt(instance, value)
}
class LongAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Long = field.getLong(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Long) = field.setLong(instance, value)
}
class FloatAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Float = field.getFloat(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Float) = field.setFloat(instance, value)
}
class DoubleAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Double = field.getDouble(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Double) = field.setDouble(instance, value)
}
class BooleanAccessor<O>(private val field: Field){
  operator fun getValue(instance: O, property: KProperty<*>): Boolean = field.getBoolean(instance)
  operator fun setValue(instance: O, property: KProperty<*>, value: Boolean) = field.setBoolean(instance, value)
}

inline fun <reified O, reified T> accessField(name: String) =
  FieldAccessor<O, T>(O::class.java.getDeclaredField(name).also {
    if (!it.type.isAssignableFrom(T::class.java))
      throw IllegalArgumentException("field $it type is not instance of ${T::class.java}")
    it.isAccessible = true
  })
inline fun <reified O> accessByte(name: String) =
  ByteAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Byte::class.java)
      throw IllegalArgumentException("field $it type is not byte")
    it.isAccessible = true
  })
inline fun <reified O> accessShort(name: String) =
  ShortAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Short::class.java)
      throw IllegalArgumentException("field $it type is not short")
    it.isAccessible = true
  })
inline fun <reified O> accessInt(name: String) =
  IntAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Int::class.java)
      throw IllegalArgumentException("field $it type is not int")
    it.isAccessible = true
  })
inline fun <reified O> accessLong(name: String) =
  LongAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Long::class.java)
      throw IllegalArgumentException("field $it type is not long")
    it.isAccessible = true
  })
inline fun <reified O> accessFloat(name: String) =
  FloatAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Float::class.java)
      throw IllegalArgumentException("field $it type is not float")
    it.isAccessible = true
  })
inline fun <reified O> accessDouble(name: String) =
  DoubleAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Double::class.java)
      throw IllegalArgumentException("field $it type is not double")
    it.isAccessible = true
  })
inline fun <reified O> accessBoolean(name: String) =
  BooleanAccessor<O>(O::class.java.getDeclaredField(name).also {
    if (it.type != Boolean::class.java)
      throw IllegalArgumentException("field $it type is not boolean")
    it.isAccessible = true
  })

fun <O: Any> KClass<O>.accessByte(name: String) =
  ByteAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Byte::class.java)
      throw IllegalArgumentException("field $it type is not byte")
    it.isAccessible = true
  })
fun <O: Any> KClass<O>.accessShort(name: String) =
  ShortAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Short::class.java)
      throw IllegalArgumentException("field $it type is not short")
    it.isAccessible = true
  })
fun <O: Any> KClass<O>.accessInt(name: String) =
  IntAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Int::class.java)
      throw IllegalArgumentException("field $it type is not int")
    it.isAccessible = true
  })
fun <O: Any> KClass<O>.accessLong(name: String) =
  LongAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Long::class.java)
      throw IllegalArgumentException("field $it type is not long")
    it.isAccessible = true
  })
fun <O: Any> KClass<O>.accessFloat(name: String) =
  FloatAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Float::class.java)
      throw IllegalArgumentException("field $it type is not float")
    it.isAccessible = true
  })
fun <O: Any> KClass<O>.accessDouble(name: String) =
  DoubleAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Double::class.java)
      throw IllegalArgumentException("field $it type is not double")
    it.isAccessible = true
  })
fun <O: Any> KClass<O>.accessBoolean(name: String) =
  BooleanAccessor<O>(this.java.getDeclaredField(name).also {
    if (it.type != Boolean::class.java)
      throw IllegalArgumentException("field $it type is not boolean")
    it.isAccessible = true
  })

// method invoker
class MethodInvoker0<O, R>(private val method: Method){
  operator fun O.invoke() =
    method.invoke(this) as R
}
class MethodInvoker1<O, P1, R>(private val method: Method){
  operator fun O.invoke(p1: P1) =
    method.invoke(this, p1) as R
}
class MethodInvoker2<O, P1, P2, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2) =
    method.invoke(this, p1, p2) as R
}
class MethodInvoker3<O, P1, P2, P3, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3) =
    method.invoke(this, p1, p2, p3) as R
}
class MethodInvoker4<O, P1, P2, P3, P4, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4) =
    method.invoke(this, p1, p2, p3, p4) as R
}
class MethodInvoker5<O, P1, P2, P3, P4, P5, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5) =
    method.invoke(this, p1, p2, p3, p4, p5) as R
}
class MethodInvoker6<O, P1, P2, P3, P4, P5, P6, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6) =
    method.invoke(this, p1, p2, p3, p4, p5, p6) as R
}
class MethodInvoker7<O, P1, P2, P3, P4, P5, P6, P7, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7) as R
}
class MethodInvoker8<O, P1, P2, P3, P4, P5, P6, P7, P8, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8) as R
}
class MethodInvoker9<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9) as R
}
class MethodInvoker10<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) as R
}
class MethodInvoker11<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) as R
}
class MethodInvoker12<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) as R
}
class MethodInvoker13<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) as R
}
class MethodInvoker14<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) as R
}
class MethodInvoker15<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) as R
}
class MethodInvoker16<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) as R
}
class MethodInvoker17<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) as R
}
class MethodInvoker18<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) as R
}
class MethodInvoker19<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) as R
}
class MethodInvoker20<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R>(private val method: Method){
  operator fun O.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20) =
    method.invoke(this, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) as R
}

inline fun <reified O, reified R> accessMethod(name: String) =
  MethodInvoker0<O, R>(O::class.java.getDeclaredMethod(name).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified R> accessMethod(name: String) =
  MethodInvoker1<O, P1, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified R> accessMethod(name: String) =
  MethodInvoker2<O, P1, P2, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified R> accessMethod(name: String) =
  MethodInvoker3<O, P1, P2, P3, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java,
    P2::class.java,
    P3::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified R> accessMethod(name: String) =
  MethodInvoker4<O, P1, P2, P3, P4, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified R> accessMethod(name: String) =
  MethodInvoker5<O, P1, P2, P3, P4, P5, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified R> accessMethod(name: String) =
  MethodInvoker6<O, P1, P2, P3, P4, P5, P6, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified R> accessMethod(name: String) =
  MethodInvoker7<O, P1, P2, P3, P4, P5, P6, P7, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified R> accessMethod(name: String) =
  MethodInvoker8<O, P1, P2, P3, P4, P5, P6, P7, P8, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified R> accessMethod(name: String) =
  MethodInvoker9<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified R> accessMethod(name: String) =
  MethodInvoker10<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified R> accessMethod(name: String) =
  MethodInvoker11<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified R> accessMethod(name: String) =
  MethodInvoker12<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified R> accessMethod(name: String) =
  MethodInvoker13<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified R> accessMethod(name: String) =
  MethodInvoker14<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified P15, reified R> accessMethod(name: String) =
  MethodInvoker15<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java, P15::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified P15, reified P16, reified R> accessMethod(name: String) =
  MethodInvoker16<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java, P15::class.java,
    P16::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified P15, reified P16, reified P17, reified R> accessMethod(name: String) =
  MethodInvoker17<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java, P15::class.java,
    P16::class.java, P17::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified P15, reified P16, reified P17, reified P18, reified R> accessMethod(name: String) =
  MethodInvoker18<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java, P15::class.java,
    P16::class.java, P17::class.java, P18::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified P15, reified P16, reified P17, reified P18, reified P19, reified R> accessMethod(name: String) =
  MethodInvoker19<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java, P15::class.java,
    P16::class.java, P17::class.java, P18::class.java, P19::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })
inline fun <reified O, reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified P9, reified P10, reified P11, reified P12, reified P13, reified P14, reified P15, reified P16, reified P17, reified P18, reified P19, reified P20, reified R> accessMethod(name: String) =
  MethodInvoker20<O, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R>(O::class.java.getDeclaredMethod(
    name,
    P1::class.java, P2::class.java, P3::class.java, P4::class.java, P5::class.java,
    P6::class.java, P7::class.java, P8::class.java, P9::class.java, P10::class.java,
    P11::class.java, P12::class.java, P13::class.java, P14::class.java, P15::class.java,
    P16::class.java, P17::class.java, P18::class.java, P19::class.java, P20::class.java
  ).also {
    checkReturnType(it, R::class.java)
    it.isAccessible = true
  })

fun checkReturnType(met: Method, retType: Class<*>) {
  if (!retType.isAssignableFrom(met.returnType))
    throw IllegalArgumentException("method returned type ${met.returnType} is not instance of $retType")
}
