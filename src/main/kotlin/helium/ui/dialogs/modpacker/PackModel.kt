package helium.ui.dialogs.modpacker

import arc.Core
import arc.files.Fi
import arc.scene.style.Drawable
import arc.struct.ObjectSet
import arc.struct.Seq
import mindustry.gen.Icon
import mindustry.mod.Mods
import java.util.Locale.getDefault

class PackModel {
  var name: String = ""
  var displayName: String = ""
  var description: String = ""
  var version: String = ""
  var author: String = ""
  var minGameVersion: String = "0"

  var icon: Fi? = null
  var installMessage: String? = null
  var type: Type = Type.Distribute

  var skipRepeat = false
  var rawBackup = false
  var force = false
  var uncheck = false

  val mods = ObjectSet<Mods.LoadedMod>()
  val fileEntries = Seq<Entry>()

  open class Entry(
    val fi: Fi,
    var to: String? = null
  ) : Comparable<Entry> {
    override fun compareTo(other: Entry): Int {
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
