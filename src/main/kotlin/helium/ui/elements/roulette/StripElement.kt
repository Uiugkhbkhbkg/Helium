package helium.ui.elements.roulette

interface StripElement{
  var centerX: Float
  var centerY: Float

  var angleDelta: Float
  var stripWidth: Float

  var angle: Float
  var distance: Float

  fun setRange(angleDelta: Float, stripWidth: Float): StripElement {
    this.angleDelta = angleDelta
    this.stripWidth = stripWidth
    return this
  }

  fun setCPosition(angle: Float, distance: Float): StripElement {
    this.angle = angle
    this.distance = distance
    return this
  }

  fun setCBounds(angle: Float, distance: Float, angleDelta: Float, stripWidth: Float): StripElement {
    this.angle = angle
    this.distance = distance
    this.angleDelta = angleDelta
    this.stripWidth = stripWidth
    return this
  }

  val minAngleDelta get() = prefAngleDelta
  val minStripWidth get() = prefStripWidth
  val prefAngleDelta get() = 0f
  val prefStripWidth get() = 0f
  val maxAngleDelta get() = 0f
  val maxStripWidth get() = 0f
}