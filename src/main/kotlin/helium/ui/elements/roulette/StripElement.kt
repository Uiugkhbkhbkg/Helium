package helium.ui.elements.roulette

import arc.scene.Element

open class StripElement: Element(){
  var centerX = 0f
  var centerY = 0f

  var angleDelta = 0f
  var stripWidth = 0f

  var angle = 0f
  var distance = 0f

  open val minAngleDelta get() = prefAngleDelta
  open val minStripWidth get() = prefStripWidth
  open val prefAngleDelta get() = 0f
  open val prefStripWidth get() = 0f
  open val maxAngleDelta get() = 0f
  open val maxStripWidth get() = 0f
}