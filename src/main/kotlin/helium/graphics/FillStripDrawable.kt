package helium.graphics

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.util.Tmp

open class FillStripDrawable(
  val color: Color,
  val innerColor: Color = color,
): BaseStripDrawable() {
  override fun draw(
    originX: Float, originY: Float,
    angle: Float, distance: Float,
    angleDelta: Float, stripWidth: Float
  ) {
    DrawUtils.circleStrip(
      originX, originY,
      distance, distance + stripWidth,
      angleDelta, angle,
      Tmp.c1.set(innerColor).mul(Draw.getColor()),
      Tmp.c2.set(color).mul(Draw.getColor())
    )
  }

  open fun tint(color: Color, innerColor: Color = color): FillStripDrawable {
    return FillStripDrawable(color, innerColor)
  }
}