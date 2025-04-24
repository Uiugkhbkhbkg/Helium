package helium.ui.fragments.entityinfo

import arc.Core
import arc.func.Boolp
import arc.scene.style.Drawable
import arc.scene.ui.layout.Table
import arc.util.Align
import arc.util.Scaling
import kotlin.reflect.KMutableProperty0

interface ConfigurableDisplay {
  fun getConfigures(): List<ConfigPair>
}

open class ConfigPair(
  val name: String,
  val icon: Drawable,
  val checked: Boolp? = null,
  val callback: Runnable
){
  constructor(
    name: String,
    icon: Drawable,
    bind: KMutableProperty0<Boolean>
  ) : this(
    name,
    icon,
    { bind.get() },
    { bind.set(!bind.get()) }
  )

  open fun localized() = Core.bundle["config.$name"]
  open fun build(table: Table) {
    table.image(icon).size(38f).scaling(Scaling.fit)
    table.row()
    table.add(localized(), 0.8f)
      .width(100f).wrap().labelAlign(Align.center)
  }
}
