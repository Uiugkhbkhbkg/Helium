package helium.ui.dialogs.modpacker

import arc.util.serialization.Jval

object MetaGenerator {
  fun genMeta(model: PackModel): String {
    val res: Jval = Jval.newObject()
    res.add("name", model.name)
    res.add("displayName", model.displayName)
    res.add("description", model.description)
    res.add("version", model.version)
    res.add("author", model.author)
    res.add("minGameVersion", model.minGameVersion)

    if (model.installMessage != null) res.add("installMessage", model.installMessage)


    res.add("hidden", Jval.valueOf(true))

    val modList: Jval = Jval.newArray()
    for (mod in model.mods) {
      val modInfo: Jval = Jval.newObject()
      modInfo.add("name", mod.name)
      modInfo.add("version", mod.meta.version)
      modInfo.add("author", mod.meta.author)
      modInfo.add("file", mod.file!!.name())
      modList.add(modInfo)
    }
    res.add("mods", modList)

    res.add("main", "main.Main")

    return res.toString(Jval.Jformat.formatted)
  }
}
