package helium.ui.dialogs

import arc.Core
import arc.func.*
import arc.graphics.Color
import arc.math.Mathf
import arc.scene.event.HandCursorListener
import arc.scene.event.Touchable
import arc.scene.ui.Button
import arc.scene.ui.Label
import arc.scene.ui.Slider
import arc.scene.ui.Tooltip
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.util.Strings
import helium.ui.dialogs.ModConfigDialog.ConfigLayout
import mindustry.graphics.Pal
import mindustry.ui.Styles

class ConfigSepLine(
  name: String,
  private var string: String,
  private var lineColor: Color = Pal.accent,
  private var lineColorBack: Color = Pal.accentBack
): ConfigLayout(name) {
  override fun build(table: Table) {
    table.stack(
      Table { t: Table ->
        t.image().color(lineColor).pad(0f).grow()
        t.row()
        t.image().color(lineColorBack).pad(0f).height(4f).growX()
      },
      Table { t: Table ->
        t.left().add(string, Styles.outlineLabel).fill().left().padLeft(5f)
      }
    ).grow().pad(-5f).padBottom(4f).padTop(4f)
    table.row()
  }
}

abstract class ConfigEntry(name: String) : ConfigLayout(name) {
  protected var str: Prov<String>? = null
  protected var tip: Prov<String>? = null
  protected var disabled = Boolp { false }

  init {
    if (Core.bundle.has("settings.tip.$name")) {
      tip = Prov { Core.bundle["settings.tip.$name"] }
    }
  }

  override fun build(table: Table) {
    table.left().add(Core.bundle["settings.item.$name"]).left().padLeft(4f)
    table.right().table { t: Table ->
      t.clip = false
      t.right().defaults().right().padRight(0f)
      if (str != null) t.add("").update { l -> l.setText(str!!.get()) }

      buildCfg(t)
    }.growX().height(60f).padRight(4f)

    if (tip != null) {
      table.addListener(Tooltip { ta ->
        ta.add(
          tip!!.get()
        ).update { l: Label -> l.setText(tip!!.get()) }
      }.apply { allowMobile = true })
    }
  }

  abstract fun buildCfg(table: Table)
}

class ConfigButton(
  name: String,
  private var button: Prov<Button>
): ConfigEntry(name) {
  override fun buildCfg(table: Table) {
    table.add(button.get()).width(180f).growY().pad(4f).get().setDisabled(disabled)
  }
}

class ConfigTable(
  name: String,
  private var table: Cons<Table>,
  private var handler: Cons<Cell<Table>>
): ConfigEntry(name) {
  override fun buildCfg(table: Table) {
    handler[table.table { t: Table ->
      t.clip = false
      this.table[t]
    }]
  }
}

class ConfigCheck(
  name: String,
  private var click: Boolc,
  private var checked: Boolp
): ConfigEntry(name) {
  override fun buildCfg(table: Table) {
    table.check("", checked.get(), click)
      .update { c -> c.isChecked = checked.get() }
      .get().also {
        it.setDisabled(disabled)
        table.touchable = Touchable.enabled
        table.addListener(it.clickListener)
        table.addListener(HandCursorListener())
        it.removeListener(it.clickListener)
      }
  }
}

class ConfigSlider : ConfigEntry {
  private var slided: Floatc
  private var curr: Floatp
  private var show: Func<Float, String>
  private var min: Float
  private var max: Float
  private var step: Float

  constructor(
    name: String,
    slided: Floatc,
    curr: Floatp,
    min: Float, max: Float, step: Float
  ) : super(name) {
    var s = step
    this.slided = slided
    this.curr = curr
    this.min = min
    this.max = max
    this.step = s

    val fix: Int
    s %= 1f
    var i = 0
    while (true) {
      if (Mathf.zero(s)) {
        fix = i
        break
      }
      s *= 10f
      s %= 1f
      i++
    }

    this.show = Func { f -> Strings.autoFixed(f, fix) }
  }

  constructor(
    name: String,
    show: Func<Float, String>,
    slided: Floatc,
    curr: Floatp,
    min: Float, max: Float, step: Float
  ) : super(name) {
    this.show = show
    this.slided = slided
    this.curr = curr
    this.min = min
    this.max = max
    this.step = step
  }

  override fun buildCfg(table: Table) {
    if (str == null) {
      table.add("").update { l: Label ->
        l.setText(show[curr.get()])
      }.padRight(0f)
    }
    table.slider(min, max, step, curr.get(), slided).width(360f).padLeft(4f).update { s: Slider ->
      s.setValue(curr.get())
      s.isDisabled = disabled.get()
    }
  }
}