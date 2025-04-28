package helium.ui

import arc.Core
import arc.func.Boolp
import arc.func.Cons
import arc.func.Prov
import arc.graphics.Color
import arc.scene.style.Drawable
import arc.scene.ui.Dialog
import arc.scene.ui.Image
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.util.Strings
import helium.invoke
import mindustry.gen.Icon
import mindustry.graphics.Pal
import mindustry.ui.Styles

object UIUtils {
  fun showException(e: Throwable, message: String? = null) = showPane(
    Core.bundle["infos.exception"],
    ButtonEntry(Core.bundle["confirm"], Icon.ok) { it.hide() },
    titleColor = Color.crimson,
  ){ t ->
    message?.also {
      t.add(it)
      t.row()
    }
    t.row()
    t.pane(Styles.smallPane) { pane ->
      pane.add(Strings.neatError(e)).color(Color.lightGray).left().fill()
    }.fill().margin(14f).maxHeight(800f)
  }

  fun showError(message: String) = showPane(
    Core.bundle["infos.error"],
    ButtonEntry(Core.bundle["confirm"], Icon.ok) { it.hide() },
        titleColor = Color.crimson,
  ){ t ->
    t.add(message)
    t.row()
  }

  fun showTip(title: String?, message: String, callback: Runnable? = null) = showPane(
    title,
    ButtonEntry(Core.bundle["confirm"], Icon.cancel) {
      callback?.run()
      it.hide()
    },
  ){ it.add(message) }

  fun showConfirm(title: String?, message: String, callback: Runnable) = showPane(
    title,
    cancelBut,
    ButtonEntry(Core.bundle["confirm"], Icon.ok) {
      callback.run()
      it.hide()
    }
  ){ it.add(message) }

  fun showPane(
    title: String?,
    vararg buttons: ButtonEntry,
    titleColor: Color? = Pal.accent,
    build: Cons<Table>
  ): Dialog {
    val dialog = Dialog()
    dialog.cont.table(HeAssets.grayUIAlpha){ info ->
      title?.also {
        info.add(it).color(titleColor).pad(8f)
        info.row()
      }
      info.line(Pal.accent, true, 3f).padTop(4f).padLeft(-6f).padRight(-6f)
      info.row()
      info.table(build).grow().minSize(420f, 120f).pad(12f)
      info.row()
      info.table { but ->
        but.defaults().growX().height(46f).minWidth(72f).pad(4f)
        buttons.forEach { entry ->
          but.button("", Styles.none, Styles.flatt, 32f) {
            entry.clicked(dialog)
          }.margin(6f).also {
            entry.disabled?.also { d -> it.disabled { d.get() } }
            entry.checked?.also { c -> it.checked { c.get() } }
          }.update { b ->
            b.setText(entry.title.get())
            entry.icon?.also { i ->
              val img = b.find<Image>{ it is Image }
              img.setDrawable(i.get())
            }
          }
        }
      }.growX().fillY()
    }.margin(6f).grow()

    return dialog.show()
  }

  fun Table.line(color: Color, horizon: Boolean, stroke: Float) = image().color(color).also {
    if (horizon) it.height(stroke).growX()
    else it.width(stroke).growY()
  } as Cell<Image>
}

data class ButtonEntry(
  val title: Prov<String>,
  val icon: Prov<Drawable>? = null,
  val disabled: Boolp? = null,
  val checked: Boolp? = null,
  val clicked: Cons<Dialog>,
){
  constructor(
    title: String,
    icon: Drawable? = null,
    disabled: Boolp? = null,
    checked: Boolp? = null,
    clicked: Cons<Dialog>,
  ) : this(
    { title },
    icon?.let { { it } },
    disabled,
    checked,
    clicked,
  )
}

val cancelBut = ButtonEntry(Core.bundle["cancel"], Icon.cancel, clicked = { it.hide() })
val closeBut = ButtonEntry(Core.bundle["misc.close"], Icon.cancel, clicked = { it.hide() })
