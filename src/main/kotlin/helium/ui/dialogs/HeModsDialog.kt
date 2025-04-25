package helium.ui.dialogs

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.TextureRegion
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.Button
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Table
import arc.util.Scaling
import helium.ui.HeAssets
import helium.ui.elements.HeCollapser
import mindustry.Vars
import mindustry.Vars.modGuideURL
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.mod.Mods
import mindustry.ui.Styles
import mindustry.ui.dialogs.BaseDialog

class HeModsDialog: BaseDialog(Core.bundle["mods"]) {

  private lateinit var disabled: Table
  private lateinit var enabled: Table

  private var searchStr = ""

  init {
    rebuild()

    resized(::rebuild)
  }

  fun rebuild(){
    cont.clearChildren()

    cont.table { main ->
      if (Core.graphics.isPortrait){

      }
      else {
        main.table { search ->
          search.image(Icon.zoom).size(64f).scaling(Scaling.fit)
          search.field(""){ searchStr = it }.growX()
        }.growX().fillY().colspan(3).padLeft(16f).padRight(16f)
        main.row()
        main.table { left ->
          left.table(HeAssets.grayUIAlpha){ list ->
            list.add(Core.bundle["dialog.mods.enabled"]).color(Pal.accent)
            list.row()
            list.image().color(Pal.accent).height(4f).growX().padTop(6f).padBottom(6f)
            list.row()
            list.top().pane { enabled ->
              this.enabled = enabled
            }.width(Core.graphics.width/2.8f/Scl.scl()).fillY().top()
          }.fillX().growY()
          left.row()
          left.button(Core.bundle["dialog.mods.disableAll"], Icon.none, Styles.grayt, 32f){

          }.padTop(6f).growX().margin(8f)
        }.fillX().growY()
        main.image().color(Color.gray).width(4f).growY().padLeft(6f).padRight(6f)
        main.table { right ->
          right.table(HeAssets.grayUIAlpha){ list ->
            list.add(Core.bundle["dialog.mods.disabled"]).color(Pal.accent)
            list.row()
            list.image().color(Pal.accent).height(4f).growX().padTop(6f).padBottom(6f)
            list.row()
            list.top().pane { disabled ->
              this.disabled = disabled
            }.width(Core.graphics.width/2.8f/Scl.scl()).fillY().top()
          }.fillX().growY()
          right.row()
          right.button(Core.bundle["dialog.mods.enableAll"], Icon.box, Styles.grayt, 32f){

          }.padTop(6f).growX().margin(8f)
        }.fillX().growY()
        main.row()
        main.image().color(Pal.gray).height(4f).growX().pad(-8f).padTop(6f).padBottom(6f).colspan(3)
        main.row()
        main.table { buttons ->
          buttons.table { top ->
            top.defaults().growX().height(54f).pad(4f)
            top.button(Core.bundle["mods.browser"], Icon.planet, Styles.flatBordert, 46f){  }
            top.button(Core.bundle["mod.import.file"], Icon.file, Styles.flatBordert, 46f){  }
            top.button(Core.bundle["mod.import.github"], Icon.download, Styles.flatBordert, 46f){  }
          }.growX().fillY().padBottom(6f)
          buttons.row()
          buttons.table { bot ->
            bot.defaults().growX().height(62f).pad(4f)
            bot.button(Core.bundle["back"], Icon.leftOpen, Styles.grayt, 46f)
            { hide() }
            bot.button(Core.bundle["dialog.mods.checkUpdate"], Icon.up, Styles.grayt, 46f)
            {  }
            bot.button(Core.bundle["dialog.mods.generateList"], Icon.list, Styles.grayt, 46f)
            {  }
            bot.button(Core.bundle["mods.openfolder"], Icon.save, Styles.grayt, 46f)
            {
              if (Core.app.isMobile)
              else Core.app.openFolder(Vars.modDirectory.absolutePath())
            }
            bot.button(Core.bundle["mods.guide"], Icon.link, Styles.grayt, 46f)
            { Core.app.openURI(modGuideURL) }
          }.growX().fillY()
        }.growX().fillY().colspan(3)
      }
    }.fillX().growY()

    rebuildMods()
  }

  fun rebuildMods(){
    enabled.clearChildren()
    disabled.clearChildren()

    Vars.mods.list().filter { searchStr.isBlank() || it.name.contains(searchStr) }.forEach { mod ->
      val addToTarget = if (mod.enabled()) enabled else disabled
      val modTab = buildModTab(mod)

      addToTarget.add(modTab).growX().fillY().pad(4f)
      addToTarget.row()
    }
  }

  private fun buildModTab(mod: Mods.LoadedMod): Table {
    val res = Button(Styles.grayt)

    res.table(Tex.buttonSelect) { icon ->
      icon.image(mod.iconTexture?.let { TextureRegionDrawable(TextureRegion(it)) }?:Tex.nomap)
        .scaling(Scaling.fit).size(80f)
    }.pad(10f).margin(4f).fill()
    res.stack(
      Table{ info ->
        info.left().top().margin(12f).marginLeft(6f).defaults().left()
        info.add(mod.meta.displayName).color(Pal.accent)
        info.row()
        info.add(mod.meta.version, 0.8f).color(Color.lightGray)
        info.row()
        info.add(mod.meta.shortDescription()).growY()
      },
      Table{ over ->
        over.right().top().defaults().size(48f)
        over.button(Icon.rightOpen, Styles.clearNonei){}
        over.button(Icon.rightOpen, Styles.clearNonei){}
      }
    ).grow()
    res.row()
    res.add(HeCollapser(collX = false, collY = true){ details ->

    }).growX().fillY().colspan(2)

    return res
  }
}

class HeModsBrowser: BaseDialog(Core.bundle["mods.browser"]) {

}