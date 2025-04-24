package helium.graphics

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.util.Tmp


class EdgeLineStripDrawable(
  val stroke: Float,
  val lineColor: Color,
  color: Color = Color.clear,
  innerColor: Color = color,
): FillStripDrawable(color, innerColor) {
  override fun draw(
    originX: Float,
    originY: Float,
    angle: Float,
    distance: Float,
    angleDelta: Float,
    stripWidth: Float,
  ) {
    super.draw(originX, originY, angle, distance, angleDelta, stripWidth)
    Draw.color(Tmp.c1.set(lineColor).mul(Draw.getColor()).toFloatBits())
    Lines.stroke(stroke)
    if (stripWidth <= 0) {
      Lines.arc(
        originX, originY,
        distance, angleDelta/360f,
        angle
      )
    }
    else if (angleDelta <= 0) {
      val cos = Mathf.cosDeg(angle)
      val sin = Mathf.sinDeg(angle)
      val inOffX = distance * cos
      val inOffY = distance * sin
      val outOffX = (distance + stripWidth) * cos
      val outOffY = (distance + stripWidth) * sin
      Lines.line(
        originX + inOffX,
        originY + inOffY,
        originX + outOffX,
        originY + outOffY,
      )
    }
    else DrawUtils.circleFrame(
      originX, originY,
      distance, distance + stripWidth,
      angleDelta, angle,
      padCap = true
    )
  }

  fun tint(
    stroke: Float,
    lineColor: Color,
    color: Color = this.color,
    innerColor: Color = this.innerColor,
  ): EdgeLineStripDrawable {
    return EdgeLineStripDrawable(stroke, lineColor, color, innerColor)
  }
}