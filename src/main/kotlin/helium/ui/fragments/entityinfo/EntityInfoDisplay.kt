package helium.ui.fragments.entityinfo

import arc.func.Prov
import arc.scene.Element
import arc.util.pooling.Pool.Poolable
import arc.util.pooling.Pools
import mindustry.gen.Posc

@Suppress("UNCHECKED_CAST")
abstract class EntityInfoDisplay<M: Model<*>>(
  private val modelProv: Prov<M>
) {
  private val modelType = modelProv.get()::class.java as Class<M>

  fun obtainModel(ent: Posc): M = (Pools.obtain(modelType, modelProv) as Model<Posc>)
    .also {
      it.entity = ent
      it.setup(ent)
    } as M

  abstract val layoutSide: Side
  open val hoveringOnly: Boolean get() = false
  open fun M?.checkHovering(isHovered: Boolean) = isHovered

  open fun M.drawWorld(alpha: Float){}

  abstract fun valid(entity: Posc): Boolean
  abstract val M.prefWidth: Float
  abstract val M.prefHeight: Float
  open fun M.shouldDisplay() = true
  abstract fun M.realWidth(prefSize: Float): Float
  abstract fun M.realHeight(prefSize: Float): Float
  abstract fun M.update(delta: Float)
  abstract fun M.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float)
}

abstract class Model<E: Any>: Poolable{
  lateinit var entity: E
  lateinit var element: Element

  abstract fun setup(ent: E)
}

enum class Side(val dir: Int){
  CENTER(-1),
  RIGHT(0),
  TOP(1),
  LEFT(2),
  BOTTOM(3)
}

abstract class NoneModelDisplay<T: Posc>: EntityInfoDisplay<Model<T>>(modelProv = Prov {
  object : Model<T>() {
    override fun setup(ent: T) {}
    override fun reset() {}
  }
})

abstract class WorldDrawOnlyDisplay<M: Model<*>>(modelProv: Prov<M>): EntityInfoDisplay<M>(modelProv) {
  override val layoutSide: Side get() = Side.CENTER
  override val M.prefWidth: Float get() = 0f
  override val M.prefHeight: Float get() = 0f
  override fun M.realWidth(prefSize: Float) = 0f
  override fun M.realHeight(prefSize: Float) = 0f
  override fun M.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) {}
  override fun M.drawWorld(alpha: Float) {
    draw(alpha)
  }
  abstract fun M.draw(alpha: Float)
}

interface InputEventChecker<T: Model<*>> {
  fun T.buildListener(): Element
}
