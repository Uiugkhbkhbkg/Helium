package helium.ui.fragments.entityinfo.displays

import arc.Core
import arc.math.Interp
import arc.math.Mathf
import arc.math.geom.Rect
import arc.scene.Element
import arc.scene.ui.layout.Table
import arc.struct.Bits
import arc.util.Scaling
import helium.ui.HeAssets
import helium.ui.fragments.entityinfo.EntityInfoDisplay
import helium.ui.fragments.entityinfo.InputCheckerModel
import helium.ui.fragments.entityinfo.InputEventChecker
import helium.ui.fragments.entityinfo.Side
import helium.util.ifInst
import mindustry.gen.Building
import mindustry.gen.Icon
import mindustry.gen.Posc
import mindustry.gen.Teamc
import mindustry.type.Category
import mindustry.ui.Displayable
import mindustry.ui.Styles

class DetailsModel: InputCheckerModel<Displayable> {
  override lateinit var element: Element
  override lateinit var disabledTeam: Bits
  override lateinit var entity: Displayable

  var teamc: Teamc? = null

  var fadeOut = 0f
  var hovering = false
  var clipped = false

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

  override val minSizeMultiple: Int get() = -1
  override val maxSizeMultiple: Int get() = -1

  override fun DetailsModel.buildListener(): Element {
    val tab = Table(HeAssets.padGrayUIAlpha).also {
      it.isTransform = true
      it.originX = 0f
      it.originY = 0f
    }
    entity.display(tab)

    entity.ifInst<Building> { build ->
      if (
        (build.block.category == Category.distribution || build.block.category == Category.liquid)
        && build.block.displayFlow
      ){
        tab.update {
          if (!hovering) {
            build.flowItems()?.stopFlow()
            build.liquids?.stopFlow()
          }
          else {
            build.flowItems()?.updateFlow()
            build.liquids?.updateFlow()
          }
        }
      }
    }

    tab.marginBottom(tab.background.bottomHeight)

    return tab
  }

  override fun valid(entity: Posc) = entity is Displayable
  override fun enabled() = true

  override fun buildConfig(table: Table) {
    table.image(Icon.menu).size(64f).scaling(Scaling.fit)
    table.row()
    table.add(Core.bundle["infos.entityDetails"], Styles.outlineLabel)
  }

  override fun DetailsModel.checkScreenClip(
    screenViewport: Rect,
    origX: Float,
    origY: Float,
    drawWidth: Float,
    drawHeight: Float,
  ): Boolean {
    val res = screenViewport.overlaps(
      origX, origY,
      drawWidth, drawHeight
    )
    clipped = res
    return res
  }

  override fun DetailsModel?.checkHolding(isHold: Boolean, mouseHovering: Boolean): Boolean {
    if (this != null) {
      val res = mouseHovering && teamc?.let { !disabledTeam.get(it.team().id) } ?: true
      if (res) {
        hovering = true
        return true
      }
      hovering = false
      return fadeOut > 0f
    }
    return mouseHovering
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
    element.visible = !(fadeOut <= 0f || !clipped)
  }
}