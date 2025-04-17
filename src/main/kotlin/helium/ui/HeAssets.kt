package helium.ui

import arc.graphics.Color
import arc.scene.style.Drawable
import arc.scene.style.TextureRegionDrawable
import arc.util.Tmp
import helium.Helium
import mindustry.gen.Tex
import mindustry.graphics.Pal

object HeAssets {
  val lightBlue = Color.valueOf("D3FDFF")

  lateinit var heIcon: Drawable
  lateinit var program: Drawable

  lateinit var transparent: Drawable
  lateinit var grayUIAlpha: Drawable
  lateinit var padGrayUIAlpha: Drawable

  fun load(){
    heIcon = Helium.getDrawable("helium")
    program = Helium.getDrawable("program")

    transparent = (Tex.whiteui as TextureRegionDrawable).tint(Color.clear)
    grayUIAlpha = (Tex.whiteui as TextureRegionDrawable).tint(Tmp.c1.set(Pal.darkerGray).a(0.7f))
    padGrayUIAlpha = ((Tex.whiteui as TextureRegionDrawable).tint(Tmp.c1.set(Pal.darkerGray).a(0.7f))).also {
      it.leftWidth = 8f
      it.rightWidth = 8f
      it.topHeight = 8f
      it.bottomHeight = 8f
    }
  }
}