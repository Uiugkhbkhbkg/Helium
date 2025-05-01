package helium.ui.dialogs.modpacker

import java.io.File


class PackModel {
  var name: String = ""
  var displayName: String = ""
  var description: String = ""
  var version: String = ""
  var author: String = ""
  var minGameVersion: String = "0"

  var icon: File? = null
  var installMessage: String? = null

  var deleteAtExit: Boolean = false
  var disableOther: Boolean = false
  var shouldBackupData: Boolean = false
  var shouldClearOldData: Boolean = false

  var mods = HashSet<ModInfo>()

  fun selected(mod: ModInfo): Boolean {
    return mods.contains(mod)
  }

  fun addMod(mod: ModInfo) {
    mods.add(mod)
  }

  fun removeMod(mod: ModInfo) {
    mods.remove(mod)
  }

  fun listMods(): MutableSet<ModInfo> {
    return mods
  }

  open class Entry : Comparable<Entry> {
    var fi: File? = null
    var to: String? = null

    override fun compareTo(entry: Entry): Int {
      return fi!!.getName().compareTo(entry.fi!!.getName())
    }
  }

  fun check(): Int {
    val dependencies = HashMap<String?, Boolean>()
    for (mod in mods) {
      for (dependence in mod.dependencies) {
        dependencies.putIfAbsent(dependence, false)
      }
      dependencies.put(mod.name, true)
    }

    for (value in dependencies.values) {
      if (!value) return 1
    }

    if (name.isEmpty() || version.isEmpty() || author.isEmpty() || displayName.isEmpty()) return 3

    return 0
  }

  fun genMeta(generator: MetaGenerator): String {
    return generator.genMeta(this)
  }

  companion object {
    fun getStateMessage(stateCode: Int): String? {
      return when (stateCode) {
        1 -> "packer.requireDepend"
        2 -> "packer.fileError"
        3 -> "packer.metaMissing"
        else -> null
      }
    }
  }
}
