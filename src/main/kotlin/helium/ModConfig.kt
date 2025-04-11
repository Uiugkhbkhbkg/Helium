package helium

import arc.files.Fi
import arc.util.Log
import arc.util.serialization.Jval
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter

private typealias RArray = java.lang.reflect.Array

class ModConfig(configDir: Fi, internalSource: Fi) {
  companion object {
    private const val configVersion = 0
    private val configs = ModConfig::class.java.declaredFields
      .filter { it.getAnnotation(ConfigItem::class.java) != null }
      .sortedBy { it.getAnnotation(ConfigItem::class.java).order }
  }

  private var lastContext: String? = null

  private val configFile: Fi = configDir.child("mod_config.hjson")
  private val configBack: Fi = configDir.child("mod_config.hjson.bak")
  private val internalConfigFile: Fi = internalSource

  @ConfigItem(order = 0) var loadInfo = false
  @ConfigItem(order = 1) var enableBlur = false
  @ConfigItem(order = 2) var blurScl = 0
  @ConfigItem(order = 3) var blurSpace = 0f

  @ConfigItem(order = 4) var entityInfoFlushInterval = 0f
  @ConfigItem(order = 5) var entityInfoLimit = 0
  @ConfigItem(order = 6) var entityInfoScale = 0f

  fun load() {
    if (!configFile.exists()) {
      internalConfigFile.copyTo(configFile)
      Log.info("Configuration file is not exist, copying the default configuration")
      load(configFile)
    } else {
      if (!load(configFile)) {
        val backup = configBack
        configFile.copyTo(backup)
        internalConfigFile.copyTo(configFile)
        Log.info("default configuration file version updated, old config should be override(backup file for old file was created)")
        load(configFile)
        val tmp = lastContext
        load(backup, true)
        lastContext = tmp
        save()
      }
    }

    if (loadInfo) printConfig()
  }

  fun printConfig() {
    val results = StringBuilder()

    configs.forEach { cfg ->
      try {
        results.append("  ")
            .append(cfg.name)
            .append(" = ")
            .append(cfg.get(this))
            .append(";")
            .append(System.lineSeparator())
      } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
      }
    }

    Log.info("Mod config loaded! The config data:[${System.lineSeparator()}$results]")
  }

  fun load(file: Fi): Boolean = load(file, false)

  fun load(file: Fi, loadOld: Boolean): Boolean {
    val sb = StringBuilder()
    file.reader().use { reader ->
      val part = CharArray(8192)
      var n: Int
      while (reader.read(part, 0, part.size).also { n = it } != -1) {
        sb.appendRange(part, 0, n)
      }
    }

    lastContext = sb.toString()
    val config = Jval.read(lastContext!!)

    val old = config.get("configVersion").asInt() != configVersion
    if (!loadOld && old) return false

    configs.forEach { cfg ->
      if (!config.has(cfg.name)) return@forEach

      val temp = config.get(cfg.name).toString()
      try {
        cfg.set(this, warp(cfg.type, temp))
      } catch (e: IllegalArgumentException) {
        Log.err(e.toString())
      } catch (e: IllegalAccessException) {
        Log.err(e.toString())
      }
    }

    return !old
  }

  fun save() {
    try {
      save(configFile)
    } catch (e: IOException) {
      Log.err(e.toString())
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun save(file: Fi) {
    val tree = Jval.newObject()
    val map = tree.asObject()
    map.put("configVersion", Jval.valueOf(configVersion))

    configs.forEach { cfg ->
      val key = cfg.name
      val obj = cfg.get(this)
      when {
        obj == null -> when {
          CharSequence::class.java.isAssignableFrom(cfg.type) -> map.put(key, Jval.valueOf(""))
          cfg.type.isArray -> map.put(key, Jval.newArray())
          cfg.type.isEnum -> map.put(key, Jval.valueOf(firstEnum(cfg.type as Class<Enum<*>>).name))
          else -> throw RuntimeException("Unhandled null type")
        }
        else -> map.put(key, pack(obj))
      }
    }

    val writer = StringWriter()
    tree.writeTo(writer, Jval.Jformat.formatted)

    val str = writer.buffer.toString()
    val r1 = BufferedReader(StringReader(str))
    val r2 = BufferedReader(StringReader(lastContext!!))

    file.writer(false).use { write ->
      var line: String?
      while (r2.readLine().also { line = it } != null) {
        var i: Int
        var after = ""
        line?.let { currentLine ->
          i = currentLine.indexOf("//")
          if (i != -1) {
            if (currentLine.substring(0, i).trim().isEmpty()) {
              write.write(currentLine)
            } else {
              after = currentLine.substring(i)
            }
          } else if (currentLine.isEmpty()) {
            write.write("")
          }

          if (currentLine.isNotEmpty() && (i == -1 || after.isNotEmpty())) {
            write.write(r1.readLine())
          }

          write.write(after)
          write.write(System.lineSeparator())
          write.flush()
        }
      }
    }
    r1.close()
    r2.close()
  }

  private fun pack(value: Any): Jval {
    return when (value) {
      is Int -> Jval.valueOf(value)
      is Byte -> Jval.valueOf(value.toInt())
      is Short -> Jval.valueOf(value.toInt())
      is Boolean -> Jval.valueOf(value)
      is Long -> Jval.valueOf(value)
      is Char -> Jval.valueOf(value.code)
      is Float -> Jval.valueOf(value)
      is Double -> Jval.valueOf(value)
      is CharSequence -> Jval.valueOf(value.toString())
      else -> when {
        value.javaClass.isArray -> packArray(value)
        value.javaClass.isEnum -> Jval.valueOf((value as Enum<*>).name)
        else -> throw RuntimeException("invalid type: ${value.javaClass}")
      }
    }
  }

  private fun packArray(array: Any): Jval {
    if (!array.javaClass.isArray) throw RuntimeException("given object was not an array")

    val len = RArray.getLength(array)
    val res = Jval.newArray()
    val arr = res.asArray()
    (0 until len).forEach { i ->
        arr.add(pack(RArray.get(array, i)))
    }
    return res
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> warp(type: Class<T>, value: String): T {
    return when (type) {
      Int::class.java -> value.toInt() as T
      Byte::class.java -> value.toByte() as T
      Short::class.java -> value.toShort() as T
      Boolean::class.java -> value.toBoolean() as T
      Long::class.java -> value.toLong() as T
      Char::class.java -> value[0] as T
      Float::class.java -> value.toFloat() as T
      Double::class.java -> value.toDouble() as T
      else -> when {
        CharSequence::class.java.isAssignableFrom(type) -> value as T
        type.isArray -> toArray(type, value)
        type.isEnum -> (type as Class<Enum<*>>).enumConstants.find { it.name == value } as T
        else -> throw RuntimeException("invalid type: $type")
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> toArray(type: Class<T>, value: String): T {
    if (!type.isArray) throw RuntimeException("class $type was not an array")
    val a = Jval.read(value).asArray()
    val eleType = type.componentType
    val res = RArray.newInstance(eleType, a.size)
    (0 until a.size).forEach { i ->
        RArray.set(res, i, warp(eleType, a[i].toString()))
    }
    return res as T
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T: Enum<*>> firstEnum(type: Class<T>): T {
    if (!type.isEnum) throw RuntimeException("class $type was not an enum")

    return (type.getMethod("values").invoke(null) as Array<T>)[0]
  }

  fun reset() {
    configFile.copyTo(configBack)
    configFile.delete()
    Log.info("[INFO][${He.MOD_NAME}] mod config has been reset, old config file saved to file named \"mod_config.hjson.bak\"")
    load()
  }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ConfigItem(val order: Int)
