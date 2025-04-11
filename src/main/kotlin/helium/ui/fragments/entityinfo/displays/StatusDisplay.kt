package helium.ui.fragments.entityinfo.displays

import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.NoneModelDisplay
import helium.ui.fragments.entityinfo.Side
import mindustry.gen.Entityc
import mindustry.gen.Statusc

class StatusDisplay: NoneModelDisplay<Statusc>() {
  override val layoutSide: Side = Side.TOP

  override val Model<Statusc>.prefHeight: Float
    get() = 0f
  override val Model<Statusc>.prefWidth: Float
    get() = 0f

  override fun valid(entity: Entityc) = entity is Statusc
  override fun Model<Statusc>.shouldDisplay(): Boolean {
    TODO("Not yet implemented")
  }

  override fun Model<Statusc>.realHeight(prefSize: Float): Float {
    TODO("Not yet implemented")
  }

  override fun Model<Statusc>.realWidth(prefSize: Float): Float {
    TODO("Not yet implemented")
  }

  override fun Model<Statusc>.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) {

  }

  override fun Model<Statusc>.update(delta: Float) {

  }
}
