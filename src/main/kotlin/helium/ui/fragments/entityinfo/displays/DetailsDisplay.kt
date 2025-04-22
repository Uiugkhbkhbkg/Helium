package helium.ui.fragments.entityinfo.displays

import arc.Core
import arc.graphics.Color
import arc.math.Interp
import arc.math.Mathf
import arc.scene.Element
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Table
import arc.struct.Bits
import arc.util.Align
import helium.ui.HeAssets
import helium.ui.fragments.entityinfo.EntityInfoDisplay
import helium.ui.fragments.entityinfo.InputCheckerModel
import helium.ui.fragments.entityinfo.InputEventChecker
import helium.ui.fragments.entityinfo.Side
import mindustry.gen.Icon
import mindustry.gen.Posc
import mindustry.gen.Teamc
import mindustry.ui.Displayable
import mindustry.ui.Fonts

class DetailsModel: InputCheckerModel<Displayable> {
  override lateinit var element: Element
  override lateinit var disabledTeam: Bits
  override lateinit var entity: Displayable

  var teamc: Teamc? = null

  var fadeOut = 0f
  var hovering = false

  override fun setup(ent: Displayable) {
    if (ent is Teamc) teamc = ent
  }
  override fun reset() {
    fadeOut = 0f
    hovering = false
    teamc = null
  }
}

class DetailsDisplay: EntityInfoDisplay<DetailsModel>(::DetailsModel), InputEventChecker<DetailsModel> {
  override val layoutSide: Side get() = Side.BOTTOM
  override val hoveringOnly: Boolean get() = true

  override val DetailsModel.prefWidth: Float
    get() = element.prefWidth
  override val DetailsModel.prefHeight: Float
    get() = element.prefHeight

  override fun DetailsModel.buildListener(): Element {
    val tab = Table(HeAssets.padGrayUIAlpha).also {
      it.isTransform = true
      it.originX = 0f
      it.originY = 0f
    }
    entity.display(tab)
    tab.marginBottom(tab.background.bottomHeight)
    return tab
  }

  override fun valid(entity: Posc) = entity is Displayable
  override fun enabled() = true
  override fun drawConfig(centX: Float, centerY: Float) {
    val size = Scl.scl(64f)
    val off = Scl.scl(16f)
    Icon.menu.draw(
      centX - size/2f, centerY - size/2f + off,
      size, size,
    )

    Fonts.outline.draw(
      Core.bundle["infos.entityDetails"],
      centX, centerY - off,
     Color.white, 0.9f, true,
     Align.center
    )
  }

  override fun DetailsModel?.checkHovering(isHovered: Boolean): Boolean {
    if (this != null) {
      val res = isHovered && teamc?.let { !disabledTeam.get(it.team().id) }?: true
      if (res) {
        hovering = true
        return true
      }
      hovering = false
      return fadeOut > 0f
    }
    return isHovered
  }

  override fun DetailsModel.realWidth(prefSize: Float) = element.prefWidth
  override fun DetailsModel.realHeight(prefSize: Float) = element.prefHeight

  override fun DetailsModel.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) {
    val drawW = drawWidth/scale
    val drawH = drawHeight/scale
    val r = Interp.pow4Out.apply(fadeOut)
    element.scaleX = scale*r
    element.scaleY = scale
    element.setBounds(origX + drawWidth/2*(1 - r), origY, drawW, drawH)
  }

  override fun DetailsModel.update(delta: Float) {
    fadeOut = Mathf.approach(fadeOut, if (hovering) 1f else 0f, delta*0.06f)
    if (fadeOut <= 0f) element.visible = false
    else element.visible = true
  }
}