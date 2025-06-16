package helium.ui.dialogs.modpacker

import arc.Core
import arc.files.Fi
import arc.files.ZipFi
import arc.func.Cons2
import arc.graphics.Texture
import arc.struct.Seq
import arc.util.io.Streams
import arc.util.serialization.Jval
import helium.He
import helium.util.CLIENT_ONLY
import helium.util.JAR_MOD
import helium.util.JS_MOD
import mindustry.Vars
import java.io.IOException
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

object ModpackStat{
  const val METAINFO_INVALID = 0b0001
  const val FILES_INVALID = 0b0010
  const val MODS_INVALID = 0b0100
  const val MODS_DEPEND_INVALID = 0b1000

  fun Int.isCorrect() = this == 0
  fun Int.metainfoError() = this and METAINFO_INVALID != 0
  fun Int.filesError() = this and FILES_INVALID != 0
  fun Int.modsError() = this and MODS_INVALID != 0
  fun Int.modDependenciesError() = this and MODS_DEPEND_INVALID != 0
}

object ModpackUtil {
  private fun genMeta(model: PackModel): String {
    val res: Jval = Jval.newObject()
    res.add("isModpack", Jval.valueOf(true))

    res.add("name", model.name.ifBlank { model.displayName })
    res.add("displayName", model.displayName)
    res.add("description", model.description)
    res.add("version", model.version)
    res.add("author", model.author)

    res.add("minGameVersion", "${model.enabled().maxOf { it.minMajorVersion }}")
    res.add("hidden", Jval.valueOf(true))

    res.add("skipRepeat", Jval.valueOf(model.skipRepeat))
    res.add("rawBackup", Jval.valueOf(model.rawBackup))
    res.add("force", Jval.valueOf(model.force))
    res.add("uncheck", Jval.valueOf(model.uncheck))

    res.add("packType", model.type.name)

    val modList: Jval = Jval.newArray()
    for (mod in model.enabled()) {
      val modInfo: Jval = Jval.newObject()
      modInfo.add("name", mod.name)
      modInfo.add("version", mod.version)
      modInfo.add("author", mod.author)
      modInfo.add("file", "${mod.name}-${mod.version}.${mod.fi.extension()}")
      modList.add(modInfo)
    }
    res.add("mods", modList)

    res.add("main", "main.Installer")

    return res.toString(Jval.Jformat.formatted)
  }

  fun checkModel(model: PackModel): Int {
    var res = 0

    ModpackStat.apply {
      if (model.displayName.isBlank() || model.version.isBlank() || model.author.isBlank()) res = METAINFO_INVALID
      for (entry in model.fileEntries) {
        if (!entry.fi.exists() || entry.to == null) res = res or FILES_INVALID
      }
      if (!model.uncheck){
        val mods = model.enabled()
        mods.forEach { mod ->
          if (mod.dependencies.any { dep -> mods.find { it.name == dep } == null }) {
            res = res or MODS_DEPEND_INVALID
            return@apply
          }
        }
      }
    }

    return res
  }

  fun genFile(model: PackModel, toFile: Fi){
    val input = JarInputStream(He.modpackModel.read())
    val output = JarOutputStream(toFile.write())

    output.use { write ->
      input.use { read ->
        while (true){
          val ent = read.nextEntry
          if (ent == null) break
          write.putNextEntry(JarEntry(ent))
          Streams.copy(read, write)
          read.closeEntry()
          write.closeEntry()
        }
      }

      val meta = genMeta(model)
      val metaEnt = JarEntry("mod.json")
      write.putNextEntry(metaEnt)
      write.write(meta.toByteArray(Charsets.UTF_8))
      write.closeEntry()

      model.icon?.let { i ->
        if (!i.exists()) return@let
        write.putNextEntry(JarEntry("icon.png"))
        write.write(i.readBytes())
        write.closeEntry()
      }

      model.enabled().forEach{ m ->
        write.putNextEntry(JarEntry("mods/${m.name}-${m.version}.${m.fi.extension()}"))
        write.write(m.fi.readBytes())
        write.closeEntry()
      }

      model.fileEntries.forEach { ent ->
        write.putNextEntry(JarEntry("assets/${ent.to!!.let { if (it.isBlank()) "" else "$it/" }}${ent.fi.name()}"))
        write.write(ent.fi.readBytes())
        write.closeEntry()
      }
    }
  }

  private fun checkModStat(meta: Jval, file: Fi): Int {
    var res = 0b0

    if (meta.getBool("hidden", false)) res = res or CLIENT_ONLY
    if (meta.has("main")) res = res or JAR_MOD
    val root = ZipFi(file)
    if (root.child("scripts").exists()) {
      val allScripts = root.child("scripts").findAll { f -> f.extEquals("js") }
      val main = if (allScripts.size == 1) allScripts.first() else root.child("scripts").child("main.js")
      if (main.exists() && !main.isDirectory()) {
        res = res or JS_MOD
      }
    }

    return res
  }

  @Throws(IOException::class)
  fun readModpackFile(model: PackModel, file: Fi){
    val tmp = Vars.tmpDirectory.child("tmp.jar")
    file.copyTo(tmp)
    val fi = ZipFi(tmp)

    val metaFile = fi.child("mod.json")
    if (!metaFile.exists()) throw IOException("Not a mod file")

    val meta = Jval.read(metaFile.reader())
    if (!meta.getBool("isModpack", false)) throw IOException("Not a modpack file")

    model.name = meta.getString("name")
    model.displayName = meta.getString("displayName")
    model.description = meta.getString("description")
    model.version = meta.getString("version")
    model.author = meta.getString("author")

    model.icon = fi.child("icon.png").let { if (it.exists()) it else null }
    model.type = PackModel.Type.valueOf(meta.getString("packType"))

    model.skipRepeat = meta.getBool("skipRepeat", false)
    model.rawBackup = meta.getBool("rawBackup", false)
    model.force = meta.getBool("force", false)
    model.uncheck = meta.getBool("uncheck", false)

    val mods = fi.child("mods")
    try {
      meta.get("mods").asArray().forEach { m ->
        val fiName = m.getString("file")
        val f = mods.child(fiName)
        val tmpMod = Vars.tmpDirectory.child(fiName)
        f.copyTo(tmpMod)
        val open = ZipFi(tmpMod)
        if (!f.exists()) throw IOException("Mod pack error! Missing listed mod.")

        val metaFi = open.child("mod.json").let { if (it.exists()) it else open.child("mod.hjson") }
        if (!metaFi.exists()) throw IOException("Mod pack error! Listing illegal file.")

        val mod = Vars.mods.getMod(m.getString("name"))
        if (mod != null) {
          model.mods.find { c -> c.name == mod.name }?.enabled = true
        }
        else {
          val meta = Jval.read(metaFi.reader())

          val modEnt = PackModel.ModEntry(
            fi = tmpMod,
            name = m.getString("name"),
            version = m.getString("version"),
            author = m.getString("author"),
            displayName = meta.getString("displayName"),
            description = meta.getString("description"),
            dependencies = meta.get("dependencies")?.asArray()?.map { it.asString() }?: Seq(),
            minMajorVersion = meta.getString("minGameVersion", "0")
              .split(".")[0]
              .toInt(),
            stat = checkModStat(meta, tmpMod),
            shortDesc = Core.bundle["dialog.modPacker.inModPack"],
            iconTexture = open.child("icon.png").let { if (it.exists()) Texture(it) else null }
          )
          model.mods.add(modEnt)
          modEnt.enabled = true
        }
      }
    }
    catch (e: Throwable) {
      throw IOException("Mod pack error! a mod file was error.", e)
    }

    fun each(path: String, fi: Fi, cons: Cons2<String, Fi>){
      fi.list()?.forEach {
        if (it.isDirectory) each("$path/${it.name()}", it, cons)
        else cons.get(path, it)
      }
    }

    each("", fi.child("assets")){ p, f ->
      model.fileEntries.add(PackModel.FileEntry(
        f,
        p.replaceFirst("assets", "modpack").substring(1, p.length))
      )
    }

    tmp.delete()
  }
}
