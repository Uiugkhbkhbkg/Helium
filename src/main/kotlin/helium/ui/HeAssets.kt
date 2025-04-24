package helium.ui

import arc.graphics.Color
import arc.scene.style.Drawable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.layout.Scl
import arc.util.Tmp
import helium.Helium
import helium.graphics.EdgeLineStripDrawable
import helium.graphics.FillStripDrawable
import helium.graphics.StripDrawable
import mindustry.gen.Tex
import mindustry.graphics.Pal

object HeAssets {
  val lightBlue = Color.valueOf("D3FDFF")

  lateinit var heIcon: Drawable
  lateinit var program: Drawable

  lateinit var transparent: Drawable
  lateinit var grayUI: Drawable
  lateinit var grayUIAlpha: Drawable
  lateinit var darkGrayUI: Drawable
  lateinit var darkGrayUIAlpha: Drawable
  lateinit var padGrayUIAlpha: Drawable
  lateinit var slotsBack: Drawable

  lateinit var whiteStrip: StripDrawable
  lateinit var whiteEdge: StripDrawable
  lateinit var innerLight: StripDrawable
  lateinit var outerLight: StripDrawable

  fun load(){
    heIcon = Helium.getDrawable("helium")
    program = Helium.getDrawable("program")

    slotsBack = Helium.getDrawable("slots-back")

    val white = Tex.whiteui as TextureRegionDrawable
    transparent = white.tint(Color.clear)
    grayUI = white.tint(Pal.darkerGray)
    grayUIAlpha = white.tint(Tmp.c1.set(Pal.darkerGray).a(0.7f))
    darkGrayUI = white.tint(Pal.darkestGray)
    darkGrayUIAlpha = white.tint(Tmp.c1.set(Pal.darkestGray).a(0.7f))
    padGrayUIAlpha = white.tint(Tmp.c1.set(Pal.darkerGray).a(0.7f)).also {
      it.leftWidth = 8f
      it.rightWidth = 8f
      it.topHeight = 8f
      it.bottomHeight = 8f
    }

    whiteStrip = FillStripDrawable(Color.white)
    whiteEdge = EdgeLineStripDrawable(Scl.scl(3f), Color.white)
    innerLight = FillStripDrawable(Color.white.cpy().a(0f), Color.white)
    outerLight = FillStripDrawable(Color.white, Color.white.cpy().a(0f))
  }
}