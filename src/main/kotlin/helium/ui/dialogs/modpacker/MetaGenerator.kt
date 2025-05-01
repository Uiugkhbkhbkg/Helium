package helium.ui.dialogs.modpacker

fun interface MetaGenerator {
  fun genMeta(model: PackModel): String
}
