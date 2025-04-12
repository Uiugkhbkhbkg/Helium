package helium.ui.fragments.entityinfo.displays

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Interp
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Rect
import arc.util.Time
import arc.util.Tmp
import helium.graphics.DrawUtils
import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.WorldDrawOnlyDisplay
import mindustry.gen.Posc
import mindustry.graphics.Layer
import mindustry.logic.Ranged
import kotlin.math.roundToInt

class AttackRangeModel: Model<Ranged> {
  override lateinit var entity: Ranged
  var hovering = false

  override fun setup(ent: Ranged) {
  }

  override fun reset() {
    hovering = false
  }
}

class AttackRangeDisplay: WorldDrawOnlyDisplay<AttackRangeModel>(::AttackRangeModel) {
  override fun valid(entity: Posc): Boolean = entity is Ranged
  override fun AttackRangeModel.checkWorldClip(worldViewport: Rect) = (entity.range()*2).let { clipSize ->
    worldViewport.overlaps(
      entity.x - clipSize/2, entity.y - clipSize/2,
      clipSize, clipSize
    )
  }

  override fun AttackRangeModel?.checkHovering(isHovered: Boolean): Boolean {
    this?.hovering = isHovered
    return isHovered
  }

  override fun AttackRangeModel.draw(alpha: Float) {
    Draw.z(Layer.light - 2f)
    Lines.stroke(2f, Color.gray)
    DrawUtils.dashCircle(
      entity.x, entity.y, entity.range() + 1f,
      dashes = (entity.range()*Mathf.PI2/40f).roundToInt(),
      rotate = Time.globalTime/10
    )

    Draw.z(Layer.light - 1.9f)
    Draw.color(entity.team().color)
    Fill.circle(entity.x, entity.y, entity.range() - 2f)

    Draw.z(Layer.light - 1.8f)
    val r = (Time.globalTime + Mathf.randomSeed(entity.id().toLong(), 240f))%240/240f
    val inner = Interp.pow3.apply(r)
    val outer = Interp.pow3Out.apply(r)
    val rad = entity.range()

    Fill.lightInner(
      entity.x, entity.y, 24,
      inner*rad, outer*rad, 0f,
      Tmp.c1.set(Color.white).a(0f), Color.white
    )

    val offAng = Mathf.randomSeed(entity.id().toLong() + 1, 360f)
    for (i in 0 until 4) {
      val dir = Geometry.d4(i)
      val offX = dir.x*16f*inner
      val offY = dir.y*16f*inner
      val toX = dir.x*rad*outer
      val toY = dir.y*rad*outer

      Lines.stroke(8f*(1 - inner), Color.white)
      Lines.line(
        entity.x + Angles.trnsx(Time.globalTime + offAng, offX, offY),
        entity.y + Angles.trnsy(Time.globalTime + offAng, offX, offY),
        entity.x + Angles.trnsx(Time.globalTime + offAng, toX, toY),
        entity.y + Angles.trnsy(Time.globalTime + offAng, toX, toY),
      )
    }
  }

  override fun AttackRangeModel.update(delta: Float) {}
}