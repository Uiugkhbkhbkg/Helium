package helium.graphics

import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.math.geom.Vec2
import kotlin.math.abs
import kotlin.math.min

object DrawUtils {
  private val v1 = Vec2()
  private val v2 = Vec2()
  private val v3 = Vec2()

  fun dashCircle(
    x: Float,
    y: Float,
    radius: Float,
    scaleFactor: Float = 1.8f,
    dashes: Int = 8,
    totalDashDeg: Float = 180f,
    rotate: Float = 0f,
  ) {
    var dashRem = totalDashDeg
    if (Mathf.equal(dashRem, 0f)) return

    var sides = 40 + (radius*scaleFactor).toInt()
    if (sides%2 == 1) sides++

    v1.set(0f, 0f)
    val per = if (dashRem < 0) -360f/sides else 360f/sides
    dashRem = min(abs(dashRem.toDouble()), 360.0).toFloat()

    val rem = 360 - dashRem
    val dashDeg = dashRem/dashes
    val empDeg = rem/dashes

    Lines.beginLine()
    v1.set(radius, 0f).setAngle(rotate + 90)
    Lines.linePoint(v1.x + x, v1.y + y)

    var drawing = true
    for (i in 0..<sides) {
      if (i*abs(per.toDouble())%(dashDeg + empDeg) > dashDeg) {
        if (drawing) {
          Lines.endLine()
          drawing = false
        }
        continue
      }

      if (!drawing) Lines.beginLine()
      drawing = true
      v1.set(radius, 0f).setAngle(rotate + per*(i + 1) + 90)
      val x1 = v1.x
      val y1 = v1.y

      Lines.linePoint(x1 + x, y1 + y)
    }
    if (drawing) Lines.endLine()
  }
}