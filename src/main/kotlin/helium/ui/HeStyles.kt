package helium.ui

import arc.Core
import arc.Events
import arc.graphics.Color
import arc.scene.style.Drawable
import arc.scene.style.NinePatchDrawable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.Dialog.DialogStyle
import arc.util.Tmp
import helium.Helium
import helium.graphics.Blur
import helium.graphics.DEf_B
import helium.graphics.ScaledNinePatchClipDrawable
import helium.ui.fragments.entityinfo.displays.HealthBarStyle
import mindustry.game.EventType
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.ui.Fonts
import mindustry.ui.Styles

object HeStyles {
  val lightBlue = Color.valueOf("D3FDFF")

  lateinit var BLUR_BACK: Drawable
  lateinit var heIcon: Drawable
  lateinit var transparent: Drawable
  lateinit var grayUIAlpha: Drawable

  lateinit var transparentBack: DialogStyle

  lateinit var test: HealthBarStyle

  var uiBlur: Blur = Blur(*DEf_B)

  private var drawingCounter = 0
  private var lastDialogs = 0

  fun load() {
    Events.run(EventType.Trigger.uiDrawBegin) { drawingCounter = 0 }
    Events.run(EventType.Trigger.uiDrawEnd) { lastDialogs = drawingCounter }

    BLUR_BACK = object : TextureRegionDrawable(Core.atlas.white()) {
      override fun draw(x: Float, y: Float, width: Float, height: Float) {
        drawingCounter++
        if (drawingCounter == lastDialogs) uiBlur.directDraw {
          super.draw(x, y, width, height)
        }

        Styles.black5.draw(x, y, width, height)
      }

      override fun draw(
        x: Float, y: Float, originX: Float, originY: Float,
        width: Float, height: Float,
        scaleX: Float, scaleY: Float,
        rotation: Float
      ) {
        drawingCounter++
        if (drawingCounter == lastDialogs) uiBlur.directDraw {
          super.draw(
            x, y, originX, originY,
            width, height,
            scaleX, scaleY,
            rotation
          )
        }

        Styles.black5.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation)
      }
    }

    heIcon = Helium.getDrawable("helium")

    transparent = (Tex.whiteui as TextureRegionDrawable).tint(Color.clear)
    grayUIAlpha = (Tex.whiteui as TextureRegionDrawable).tint(Tmp.c1.set(Pal.darkerGray).a(0.7f))

    transparentBack = object : DialogStyle() {
      init {
        stageBackground = transparent
        titleFont = Fonts.outline
        background = transparent
        titleFontColor = Pal.accent
      }
    }

    test = HealthBarStyle(
      background = ScaledNinePatchClipDrawable(Helium.getDrawable("background") as NinePatchDrawable),
      backgroundColor = Color.white.cpy().a(0.6f),
      healthBar = ScaledNinePatchClipDrawable(Helium.getDrawable("healthbar") as NinePatchDrawable),
      healthPadLeft = 6,
      healthPadRight = 6,
      shieldBar = ScaledNinePatchClipDrawable(Helium.getDrawable("shieldbar") as NinePatchDrawable),
      shieldPadLeft = 5,
      shieldPadRight = 5,
      font = Fonts.outline,
      texOffX = 15,
      texOffY = 17,
      shieldsOffX = 15,
      shieldsOffY = 17
    )
  }
}
