package main

import arc.Core
import arc.files.Fi
import arc.struct.ObjectSet
import arc.struct.Seq
import java.util.Locale.getDefault

class PackModel {
  var name: String = ""
  var displayName: String = ""
  var description: String = ""
  var version: String = ""
  var author: String = ""

  var icon: Fi? = null
  var type: Type = Type.Distribute

  var skipRepeat = false
  var rawBackup = false
  var force = false
  var uncheck = false

  val mods = ObjectSet<ModEntry>()
  val fileEntries = Seq<FileEntry>()

  open class ModEntry(
    val fi: Fi,
    val name: String,
    val version: String,
    val author: String
  ){
    override fun hashCode(): Int {
      return name.hashCode() + author.hashCode() * 31
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      other as ModEntry
      return name == other.name && author == other.author
    }
  }

  open class FileEntry(
    val fi: Fi,
    var to: String? = null
  ) : Comparable<FileEntry> {
    override fun compareTo(other: FileEntry): Int {
      return fi.name().compareTo(other.fi.name())
    }
  }

  enum class Type{
    Distribute,
    Shadow,
    Override;

    val localized: String
      get() = Core.bundle["modpack.${name.lowercase(getDefault())}.name"]
    val description: String
      get() = Core.bundle["modpack.${name.lowercase(getDefault())}.description"]
  }
}
