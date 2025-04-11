package helium.ui.fragments.entityinfo

import arc.Core
import arc.func.Prov
import arc.math.geom.Vec2
import arc.util.pooling.Pool.Poolable
import arc.util.pooling.Pools
import com.oracle.graal.compiler.enterprise.phases.strings.u.it
import mindustry.gen.Entityc
import mindustry.gen.Posc
import mindustry.mod.Mod

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
  abstract fun valid(entity: Posc): Boolean

  abstract val M.prefWidth: Float
  abstract val M.prefHeight: Float
  abstract fun M.shouldDisplay(): Boolean
  abstract fun M.realWidth(prefSize: Float): Float
  abstract fun M.realHeight(prefSize: Float): Float
  abstract fun M.update(delta: Float)
  abstract fun M.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float)
}

abstract class Model<E: Posc>: Poolable{
  lateinit var entity: E

  abstract fun setup(ent: E)

  val x: Float get() = entity.x
  val y: Float get() = entity.y

  val drawPos: Vec2 get() = Core.camera.project(x, y)
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
