package helium.ui.elements.roulette

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.scene.Element
import arc.scene.event.Touchable
import arc.scene.ui.layout.WidgetGroup
import arc.util.Align
import helium.graphics.StripDrawable

open class StripWrap(
  elem: Element = Element(),
  var background: StripDrawable? = null
): WidgetGroup(), StripElement {
  companion object {
    val tmp: Vec2 = Vec2()
  }

  override var angleDelta = 0f
  override var stripWidth = 0f
  override var angle = 0f
  override var distance = 0f

  var element: Element = this
    set(value) {
      field.remove()
      addChild(value)
      field = value
    }

  init {
    element = elem
  }

  override fun draw() {
    drawBackground()
    super.draw()
  }

  open fun drawBackground() {
    Draw.color(color, color.a * parentAlpha)

    background?.draw(
      centerX, centerY,
      angle, distance,
      angleDelta, stripWidth,
    )
  }

  override fun layout() {
    val centDelta = angleDelta/2
    val centWidth = distance + stripWidth/2

    val dx = Angles.trnsx(angle + centDelta, centWidth)
    val dy = Angles.trnsy(angle + centDelta, centWidth)

    element.pack()
    element.setPosition(
      width/2f + dx,
      height/2f + dy,
      Align.center
    )
  }

  override fun hit(x: Float, y: Float, touchable: Boolean): Element? {
    if (touchable && this.touchable == Touchable.disabled) return null
    val point = tmp
    val childrenArray = children.items
    for (i in children.size - 1 downTo 0) {
      val child = childrenArray[i]
      if (!child.visible || (child.cullable && cullingArea != null && !cullingArea.overlaps(
          child.x + child.translation.x,
          child.y + child.translation.y,
          child.width,
          child.height
        ))
      ) continue
      child.parentToLocalCoordinates(point.set(x, y))
      val hit = child.hit(point.x, point.y, touchable)
      if (hit != null) return hit
    }

    if (touchable && this.touchable != Touchable.enabled) return null

    val angle = Angles.angle(x - width/2, y - height/2)
    val dst = Mathf.dst(x - width/2, y - height/2)
    val halfAngle = this.angle + angleDelta/2

    return if (Angles.near(angle, halfAngle, angleDelta/2) && dst > distance && dst < distance + stripWidth) this
    else null
  }

  override var centerX: Float
    get() = getX(Align.center)
    set(value) { x = value - width/2f }
  override var centerY: Float
    get() = getY(Align.center)
    set(value) { y = value - height/2f }
}