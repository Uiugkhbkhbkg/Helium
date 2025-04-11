package helium.ui.fragments.entityinfo

import arc.Core
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.scene.Element
import arc.scene.Group
import arc.struct.OrderedMap
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Interval
import arc.util.Time
import arc.util.Tmp
import arc.util.pooling.Pool.Poolable
import arc.util.pooling.Pools
import com.oracle.graal.compiler.enterprise.phases.strings.u.it
import helium.He.config
import helium.ui.fragments.entityinfo.Side.*
import mindustry.Vars
import mindustry.gen.Buildingc
import mindustry.gen.Groups
import mindustry.gen.Hitboxc
import mindustry.gen.Posc
import mindustry.input.Binding
import kotlin.math.max

class EntityInfoFrag {
  companion object {
    private val tempEntry = EntityEntry()
  }

  private val displays = Seq<EntityInfoDisplay<Model<*>>>()

  private val displayQueue = OrderedSet<EntityEntry>()
    .apply{ orderedItems().ordered = false }

  private val timer = Interval()
  private var delta = 0f

  private var hold: EntityEntry? = null
  private var mark = false

  private lateinit var infoFill: Element

  @Suppress("UNCHECKED_CAST")
  fun addDisplay(display: EntityInfoDisplay<*>) {
    displays.add(display as EntityInfoDisplay<Model<*>>)
  }

  @Suppress("UNCHECKED_CAST")
  fun addDisplayBefore(display: EntityInfoDisplay<*>, target: EntityInfoDisplay<*>){
    val index = displays.indexOf(target)
    display as EntityInfoDisplay<Model<*>>
    if (index == -1) displays.add(display)
    else displays.insert(index, display)
  }

  @Suppress("UNCHECKED_CAST")
  fun addDisplayAfter(display: EntityInfoDisplay<*>, target: EntityInfoDisplay<*>){
    val index = displays.indexOf(target)
    display as EntityInfoDisplay<Model<*>>
    if (index == -1 || index == displays.size - 1) displays.add(display)
    else displays.insert(index + 1, display)
  }

  fun build(parent: Group){
    infoFill = parent.fill{ _, _, _, _ ->
      draw()
    }.update {
      updateShowing()

      val touched = Core.input.keyTap(Binding.select) && Core.input.alt()
      if (touched) {
        hold = null
        mark = false
      }

      delta += Time.delta
      if (timer.get(config.entityInfoFlushInterval) || touched) {
        update(delta)
        delta = 0f
      }
    }
  }

  private fun updateShowing() {
    Groups.all.forEach { entity ->
      if (entity is Posc && checkShowing(entity)) {
        val e = tempEntry
        e.entity = entity
        e.showing = true
        e.alpha = 0f

        val ent = displayQueue.get(e)
        if (ent == null) {
          val entry = Pools.obtain(EntityEntry::class.java){ EntityEntry() }
          entry.entity = entity
          entry.showing = true

          displays.forEach { display ->
            if (display.valid(entity)) {
              entry.display.put(display, (display as EntityInfoDisplay<*>).obtainModel(entity))
            }
          }

          if (entry.display.isEmpty) Pools.free(entry)
          else displayQueue.add(entry)
        }
        else {
          ent.showing = true
        }
      }
    }

    if (!displayQueue.isEmpty) displayQueue.orderedItems()
      .sort { e -> e.entity!!.dst2(Core.input.mouseWorld()) }

    val itr = displayQueue.iterator()
    var count = 0
    while (itr.hasNext()) {
      val entry = itr.next()
      if (!entry.showing || count > config.entityInfoLimit) entry.alpha = Mathf.approach(entry.alpha, 0f, 0.025f*Time.delta)
      else entry.alpha = 1f

      entry.showing = false
      count++

      if (entry.alpha <= 0.001f) {
        itr.remove()
        Pools.free(entry)
      }
    }
  }

  private fun checkShowing(entity: Posc): Boolean {
    return true
  }

  private fun update(delta: Float) {
    displayQueue.reversed().forEach { e -> e.display.forEach { it.key.apply {
      if (it.value.shouldDisplay()) it.value.update(delta)
    } } }
  }

  private fun draw() {
    val scale = config.entityInfoScale
    displayQueue.reversed().forEach { e ->
      var offsetLeft = 0f
      var offsetRight = 0f
      var offsetTop = 0f
      var offsetBottom = 0f

      var topWidth = 0f
      var bottomWidth = 0f
      var leftHeight = 0f
      var rightHeight = 0f
      var centerWith = 0f
      var centerHeight = 0f
      e.display.forEach f@{ entry ->
        val display = entry.key
        val model = entry.value

        display.apply {
          if (!model.shouldDisplay()) return@f
          when (display.layoutSide) {
            CENTER -> {
              centerWith = max(model.prefWidth, centerWith)
              centerHeight = max(model.prefHeight, centerHeight)
            }
            RIGHT -> rightHeight = max(model.prefHeight, rightHeight)
            TOP -> topWidth = max(model.prefWidth, topWidth)
            LEFT -> leftHeight = max(model.prefHeight, leftHeight)
            BOTTOM -> bottomWidth = max(model.prefWidth, bottomWidth)
          }
        }
      }

      val origin = Core.camera.project(e.entity!!.x, e.entity!!.y)
      val origX = origin.x
      val origY = origin.y

      val sizeOrig = Core.camera.project(0f, 0f).x
      val sizeOff = Core.camera.project(e.size, 0f).x - sizeOrig

      e.display.forEach f@{ entry ->
        val display = entry.key
        val model = entry.value

        val dir = Geometry.d4(display.layoutSide.dir)
        val offset = Tmp.v1.set(sizeOff*dir.x, sizeOff*dir.y)

        display.apply {
          if (!model.shouldDisplay()) return@f
          when (layoutSide) {
            CENTER -> {
              val disW = centerWith*scale
              val disH = centerHeight*scale
              model.draw(e.alpha, scale, origX - disW/2, origY - disH/2, disW, disH)
            }
            RIGHT -> {
              val disW = model.realWidth(rightHeight)
              val disH = model.realHeight(rightHeight)
              model.draw(e.alpha, scale, origX + offset.x + offsetRight, origY - disH/2, disW, disH)
              offsetRight += disW*scale
            }
            TOP -> {
              val disW = model.realWidth(topWidth)
              val disH = model.realHeight(topWidth)
              model.draw(e.alpha, scale, origX - disW/2, origY + offset.y + offsetTop, disW, disH)
              offsetTop += disH*scale
            }
            LEFT -> {
              val disW = model.realWidth(leftHeight)
              val disH = model.realHeight(leftHeight)
              model.draw(e.alpha, scale, origX - offset.x - disW - offsetLeft, origY - disH/2, disW, disH)
              offsetLeft += disW*scale
            }
            BOTTOM -> {
              val disW = model.realWidth(bottomWidth)
              val disH = model.realHeight(bottomWidth)
              model.draw(e.alpha, scale, origX - disW/2, origY - offset.y - disH - offsetBottom, disW, disH)
              offsetBottom += disH*scale
            }
          }
        }
      }
    }
  }

  class EntityEntry : Poolable {
    var entity: Posc? = null
    var alpha = 0f
    var showing = false

    val display = OrderedMap<EntityInfoDisplay<Model<*>>, Model<*>>()

    val size: Float get() = entity?.let {
      when (it) {
        is Hitboxc -> it.hitSize()/1.44f
        is Buildingc -> it.block().size*Vars.tilesize/2f
        else -> 10f
      }
    }?: 0f

    override fun reset() {
      entity = null
      alpha = 0f
      showing = false
      display.values().forEach { Pools.free(it) }
      display.clear()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is EntityEntry) return false
      return entity === other.entity
    }

    override fun hashCode(): Int {
      return entity.hashCode()
    }
  }
}
