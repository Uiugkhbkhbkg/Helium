package helium.ui.dialogs.modpacker

import arc.Core
import arc.files.Fi
import arc.files.ZipFi
import arc.graphics.Texture
import arc.scene.style.Drawable
import arc.struct.ObjectSet
import arc.struct.Seq
import helium.util.ModStat.checkModStat
import mindustry.gen.Icon
import mindustry.mod.Mods
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

  fun enabled() = mods.filter { it.enabled }

  open class ModEntry(
    val fi: Fi,
    val name: String,
    val displayName: String,
    val shortDesc: String,
    val description: String?,
    val version: String,
    val author: String,
    val dependencies: Seq<String>,
    val iconTexture: Texture?,
    var minMajorVersion: Int,

    var stat: Int
  ){
    var enabled = false
    val root = ZipFi(fi)

    constructor(loadedMod: Mods.LoadedMod): this(
      loadedMod.file,
      loadedMod.name,
      loadedMod.meta.displayName,
      loadedMod.meta.shortDescription(),
      loadedMod.meta.description,
      loadedMod.meta.version,
      loadedMod.meta.author,
      loadedMod.meta.dependencies,
      loadedMod.iconTexture,
      loadedMod.minMajor,

      checkModStat(loadedMod)
    )

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

  enum class Type(val icon: Drawable){
    Distribute(Icon.boxSmall),
    Shadow(Icon.bookSmall),
    Override(Icon.starSmall);

    val localized: String
      get() = Core.bundle["modpack.${name.lowercase(getDefault())}.name"]
    val description: String
      get() = Core.bundle["modpack.${name.lowercase(getDefault())}.description"]
  }
}
