package helium.ui.elements.roulette

open class Strip<E: StripElement>(
  val element: E
){
  var layer = 0

  var minDelta = 0f
  var maxDelta = 0f
  var minWidth = 0f
  var maxWidth = 0f

  var padOffLeft = 0f
  var padOffRight = 0f
  var padOuter = 0f
  var padInner = 0f

  var fillDelta = false
  var fillWidth = false
}