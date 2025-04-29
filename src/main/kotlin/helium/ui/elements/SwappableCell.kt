package helium.ui.elements

import arc.input.KeyCode
import arc.math.geom.Rect
import arc.math.geom.Vec2
import arc.scene.Element
import arc.scene.event.InputEvent
import arc.scene.event.InputListener
import arc.scene.event.Touchable
import arc.scene.ui.layout.WidgetGroup
import arc.struct.Seq
import arc.util.Align

private val v1 = Vec2()
private val v2 = Vec2()
private val v3 = Vec2()

class SwappableCell<T: Element>(
  elem: T,
  val group: SwapGroup<T>
): WidgetGroup(){
  private val bound: Rect = Rect()

  private var moveX
    get() = element.translation.x
    set(value) { element.translation.x = value }
  private var moveY
    get() = element.translation.y
    set(value) { element.translation.y = value }

  var element: T = elem
    set(value) {
      removeChild(field)
      field = value
      addChild(value)
    }

  init {
    addChild(elem)
    touchable = Touchable.enabled
    setupListener()
  }

  override fun getPrefWidth() = element.prefWidth
  override fun getPrefHeight() = element.prefHeight

  override fun layout() {
    element.setBounds(0f, 0f, width, height)
  }

  fun get() = element

  private fun setupListener() {
    addListener(object : InputListener(){
      var beginX = 0f
      var beginY = 0f
      var panned = false

      override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: KeyCode?): Boolean {
        if (panned || button != KeyCode.mouseLeft || pointer != 0) return false

        toFront()

        beginX = x
        beginY = y
        return true
      }

      override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
        moveX = x - beginX
        moveY = y - beginY
        panned = true

        super.touchDragged(event, x, y, pointer)
      }

      override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: KeyCode?) {
        if (!panned || button != KeyCode.mouseLeft || pointer != 0) return

        val other = checkOverlap(group)

        moveX = 0f
        moveY = 0f
        if (other != null) {
          val tmp = element
          element = other.element
          other.element = tmp

          validate()
          other.validate()
        }

        panned = false
      }
    })
  }

  private fun absBound(): Rect {
    val botLeft = localToStageCoordinates(v1.setZero())
    val topRight = localToStageCoordinates(v2.set(width, height))

    return bound.set(
      botLeft.x, botLeft.y,
      topRight.x - botLeft.x, topRight.y - botLeft.y
    )
  }

  private fun checkOverlap(group: SwapGroup<T>): SwappableCell<T>? {
    val centX = element.getX(Align.center) + moveX
    val centY = element.getY(Align.center) + moveY

    val stageV = localToStageCoordinates(v3.set(centX, centY))

    group.cells.forEach { e ->
      val b = e.absBound()

      if (b.contains(stageV)) return e
    }

    return null
  }
}

class SwapGroup<T: Element>{
  val cells = Seq<SwappableCell<T>>()

  fun swappable(elem: T) = SwappableCell(elem, this)
    .also { cells.add(it) }
}
