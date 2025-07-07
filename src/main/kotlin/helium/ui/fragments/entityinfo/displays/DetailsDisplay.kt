package helium.ui.fragments.entityinfo.displays

import arc.Core
import arc.math.Interp
import arc.math.geom.Rect
import arc.scene.Element
import arc.scene.ui.layout.Table
import arc.util.Scaling
import helium.ui.HeAssets
import helium.ui.fragments.entityinfo.*
import helium.util.ifInst
import mindustry.gen.Building
import mindustry.gen.Icon
import mindustry.gen.Posc
import mindustry.gen.Teamc
import mindustry.type.Category
import mindustry.ui.Displayable
import mindustry.ui.Styles

class DetailsDisplayProvider: DisplayProvider<Displayable, DetailsDisplay>(){
  override val typeID: Int get() = 782376428
  override val hoveringOnly: Boolean get() = true
  override fun buildConfig(table: Table) {
    table.image(Icon.menu).size(64f).scaling(Scaling.fit)
    table.row()
    table.add(Core.bundle["infos.entityDetails"], Styles.outlineLabel)
  }

  override fun targetGroup() = listOf(
    TargetGroup.build,
    TargetGroup.unit
  )

  override fun valid(entity: Posc) = entity is Displayable
  override fun enabled() = true

  override fun provide(
    entity: Displayable,
    id: Int,
  ) = DetailsDisplay(entity, id).apply {
    hovering = false
    teamc = null
  }
}

class DetailsDisplay(
  entity: Displayable,
  id: Int
): EntityInfoDisplay<Displayable>(entity, id), InputEventChecker {
  override lateinit var element: Element
  override val typeID: Int get() = 782376428

  var teamc: Teamc? = null

  var hovering = false
  var clipped = false

  override val layoutSide: Side get() = Side.BOTTOM

  override val prefWidth: Float
    get() = element.prefWidth
  override val prefHeight: Float
    get() = element.prefHeight

  override val minSizeMultiple: Int get() = -1
  override val maxSizeMultiple: Int get() = -1

  override fun buildListener(): Element {
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

  override fun checkScreenClip(
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

  override fun realWidth(prefSize: Float) = element.prefWidth
  override fun realHeight(prefSize: Float) = element.prefHeight

  override fun draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) {
    val drawW = drawWidth/scale
    val drawH = drawHeight/scale
    val r = Interp.pow4Out.apply(alpha)
    element.scaleX = scale*r
    element.scaleY = scale
    element.setBounds(origX + drawWidth/2*(1 - r), origY, drawW, drawH)
  }

  override fun update(delta: Float, alpha: Float, isHovering: Boolean, isHolding: Boolean) {
    hovering = isHovering || isHolding
    element.visible = !(alpha <= 0f || !clipped)
  }
}