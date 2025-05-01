package helium.ui.dialogs.modpacker

import mindustry.mod.Mods.LoadedMod
import java.io.File
import java.util.*

class ModInfo {
  var name: String? = null
  var version: String? = null
  var author: String? = null
  var file: File? = null
  var dependencies = mutableListOf<String>()

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false
    val modInfo = o as ModInfo
    return name == modInfo.name && version == modInfo.version && author == modInfo.author && file == modInfo.file && dependencies == modInfo.dependencies
  }

  override fun hashCode(): Int {
    return Objects.hash(name, version, author, file, dependencies)
  }

  companion object {
    private var map: HashMap<LoadedMod?, ModInfo>? = null

    fun asLoaded(mod: LoadedMod?): ModInfo {
      if (map == null) {
        map = HashMap<LoadedMod?, ModInfo>()
      }

      return map!!.computeIfAbsent(mod) { m: LoadedMod? ->
        val res = ModInfo()
        res.name = m!!.name
        res.version = m.meta.version
        res.author = m.meta.author
        res.dependencies = ArrayList(m.meta.dependencies.list())

        res.file = m.file.file()
        res
      }
    }
  }
}
