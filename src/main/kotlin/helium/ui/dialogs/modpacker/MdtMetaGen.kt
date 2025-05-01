package helium.ui.dialogs.modpacker

import arc.util.serialization.Jval

class MdtMetaGen : MetaGenerator {
  override fun genMeta(model: PackModel): String {
    val res: Jval = Jval.newObject()
    res.add("name", model.name)
    res.add("displayName", model.displayName)
    res.add("description", model.description)
    res.add("version", model.version)
    res.add("author", model.author)
    res.add("minGameVersion", model.minGameVersion)

    if (model.installMessage != null) res.add("installMessage", model.installMessage)

    res.add("deleteAtExit", Jval.valueOf(model.deleteAtExit))
    res.add("disableOther", Jval.valueOf(model.disableOther))
    res.add("shouldBackupData", Jval.valueOf(model.shouldBackupData))
    res.add("shouldClearOldData", Jval.valueOf(model.shouldClearOldData))

    res.add("hidden", Jval.valueOf(true))

    val modList: Jval = Jval.newArray()
    for (mod in model.listMods()) {
      val modInfo: Jval = Jval.newObject()
      modInfo.add("name", mod.name)
      modInfo.add("version", mod.version)
      modInfo.add("author", mod.author)
      modInfo.add("file", mod.file!!.getName())
      modList.add(modInfo)
    }
    res.add("mods", modList)

    res.add("main", "main.Main")

    return res.toString(Jval.Jformat.formatted)
  }
}
