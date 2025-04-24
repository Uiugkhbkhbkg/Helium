package helium.ui.elements.roulette

import arc.scene.Element
import arc.scene.ui.layout.WidgetGroup
import arc.struct.Seq
import helium.graphics.StripDrawable

class Roulette(
  val background: StripDrawable? = null,
): WidgetGroup() {
  private val stripChildren = Seq<StripElement>()
  private val strips = Seq<Strip<*>>()

  private var layers = 0
  private val layerCache = Seq<Strip<*>>()

  fun <E> add(elem: E): Strip<E> where E : Element, E: StripElement{
    val res = Strip(elem)

    addChild(elem)
    strips.add(res).sort { it.layer.toFloat() }
    res.layer = layers

    return res
  }

  fun newLayer(){
    layers++
  }

  override fun addChild(actor: Element) {
    if (actor is StripElement) stripChildren.add(actor)
    super.addChild(actor)
  }

  override fun removeChild(actor: Element, unfocus: Boolean): Boolean {
    if (actor is StripElement) stripChildren.remove(actor)
    return super.removeChild(actor, unfocus)
  }

  override fun draw() {
    drawShapeLines()
    super.draw()
  }

  override fun layout() {
    layerCache.clear()
    var currentLayer = 0
    var offsetDistance = 0

    var layerWidth = 0f
    var totalMinDelta = 0f
    strips.forEach { str ->
      if (str.layer != currentLayer) {
        layerCache.forEach { s ->
          totalMinDelta += s.element.minAngleDelta
        }

        layerCache.clear()
        currentLayer = str.layer
      }

      layerCache.add(str)
    }
  }

  private fun drawShapeLines() {
    background?.draw(
      x + width/2, y + width/2,
      width/2, 360f
    )
  }
}

