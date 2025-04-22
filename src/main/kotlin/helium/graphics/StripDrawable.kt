package helium.graphics

interface StripDrawable {
  fun draw(originX: Float, originY: Float, stripWidth: Float, angleDelta: Float) =
    draw(originX, originY, 0f, 0f, stripWidth, angleDelta)
  fun draw(originX: Float, originY: Float, angle: Float, distance: Float, stripWidth: Float, angleDelta: Float)
  var leftOff: Float
  var rightOff: Float
  var outerWidth: Float
  var innerWidth: Float
  var minOffset: Float
  var minWidth: Float
}