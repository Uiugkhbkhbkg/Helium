package helium.ui.fragments.entityinfo

import arc.func.Prov
import arc.math.geom.Rect
import arc.scene.Element
import arc.scene.ui.layout.Table
import arc.struct.Bits
import arc.util.pooling.Pool.Poolable
import arc.util.pooling.Pools
import mindustry.gen.Building
import mindustry.gen.Drawc
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
  open val screenRender: Boolean get() = true
  open val worldRender: Boolean get() = false

  open val maxSizeMultiple: Int get() = 6
  open val minSizeMultiple: Int get() = 2

  abstract fun valid(entity: Posc): Boolean
  abstract fun enabled(): Boolean

  abstract fun buildConfig(table: Table)

  open fun M?.checkHolding(isHold: Boolean, mouseHovering: Boolean) = isHold
  open fun M.checkWorldClip(worldViewport: Rect) = (entity as Posc).let {
    val clipSize = when(it){
      is Drawc -> it.clipSize()
      is Building -> it.block.clipSize
      else -> 10f
    }
    worldViewport.overlaps(it.x - clipSize/2, it.y - clipSize/2, clipSize, clipSize)
  }
  open fun M.checkScreenClip(screenViewport: Rect, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) =
    screenViewport.overlaps(
      origX, origY,
      drawWidth, drawHeight
    )
  open fun M.drawWorld(alpha: Float){}

  abstract val M.prefWidth: Float
  abstract val M.prefHeight: Float
  open fun M.shouldDisplay() = true
  abstract fun M.realWidth(prefSize: Float): Float
  abstract fun M.realHeight(prefSize: Float): Float
  abstract fun M.update(delta: Float)
  abstract fun M.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float)
}

interface Model<E: Any>: Poolable{
  var disabledTeam: Bits
  var entity: E
  fun setup(ent: E)
}

enum class Side(val dir: Int){
  CENTER(-1),
  RIGHT(0),
  TOP(1),
  LEFT(2),
  BOTTOM(3)
}

abstract class NoneModelDisplay<T: Posc>: EntityInfoDisplay<Model<T>>(modelProv = Prov {
  object : Model<T> {
    override lateinit var disabledTeam: Bits
    override lateinit var entity: T
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

interface InputCheckerModel<E: Any>: Model<E> {
  var element: Element
}

interface InputEventChecker<T: InputCheckerModel<*>>{
  fun T.buildListener(): Element
}
