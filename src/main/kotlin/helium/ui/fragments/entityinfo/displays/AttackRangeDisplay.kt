package helium.ui.fragments.entityinfo.displays

import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.WorldDrawOnlyDisplay
import mindustry.gen.Posc
import mindustry.logic.Ranged

class AttackRangeModel: Model<Ranged>() {
  var hovering = false

  override fun setup(ent: Ranged) {
  }
  override fun reset() {
    hovering = false
  }
}

class AttackRangeDisplay: WorldDrawOnlyDisplay<AttackRangeModel>(::AttackRangeModel) {
  override fun valid(entity: Posc): Boolean {
    TODO("Not yet implemented")
  }

  override fun AttackRangeModel?.checkHovering(isHovered: Boolean): Boolean {
    this?.hovering = isHovered
    return isHovered
  }

  override fun AttackRangeModel.draw(alpha: Float) {

  }


  override fun AttackRangeModel.update(delta: Float) {
    TODO("Not yet implemented")
  }
}