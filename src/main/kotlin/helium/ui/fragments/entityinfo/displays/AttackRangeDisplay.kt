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
import arc.struct.Bits
import arc.util.Time
import arc.util.Tmp
import helium.He.attackRenderer
import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.WorldDrawOnlyDisplay
import mindustry.game.Team
import mindustry.gen.Buildingc
import mindustry.gen.Hitboxc
import mindustry.gen.Posc
import mindustry.graphics.Layer
import mindustry.logic.Ranged

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
  companion object {
    private val teamBits = Bits(Team.all.size)
    private var dashes = 0f

    fun resetTeamMark(){
      teamBits.clear()
      dashes = 0f
    }
  }

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
    val team = entity.team()
    val radius = entity.range()*alpha
    val layer = Layer.light - 3 + team.id/100f
    val size = entity.let {
      when(it) {
        is Hitboxc -> it.hitSize()*1.44f
        is Buildingc -> it.hitSize()*1.44f
        else -> 16f
      }
    }

    if (!teamBits.get(team.id)){
      teamBits.set(team.id)
      Draw.drawRange(layer, 0.0005f, {
        attackRenderer.capture()
      }) {
        attackRenderer.render()
      }
    }

    Draw.z(layer + 0.0001f)
    Draw.color(Color.black, entity.team().color, alpha)
    Fill.poly(entity.x, entity.y, 36, radius - 1f)

    Draw.z(layer + 0.0002f)
    val r = (Time.globalTime + Mathf.randomSeed(entity.id().toLong(), 240f))%240/240f
    val inner = Interp.pow3.apply(r)
    val outer = Interp.pow3Out.apply(r)

    Fill.lightInner(
      entity.x, entity.y, 24,
      inner*radius, outer*radius, 0f,
      Tmp.c1.set(Color.white).a(0f), Color.white
    )

    val offAng = Mathf.randomSeed(entity.id().toLong() + 1, 360f)
    for (i in 0 until 4) {
      val dir = Geometry.d4(i)
      val offX = dir.x*size*inner
      val offY = dir.y*size*inner
      val toX = dir.x*(radius - 8f)*outer
      val toY = dir.y*(radius - 8f)*outer

      Lines.stroke(8f*(1 - inner)*alpha, Color.white)
      Lines.line(
        entity.x + Angles.trnsx(Time.globalTime + offAng, offX, offY),
        entity.y + Angles.trnsy(Time.globalTime + offAng, offX, offY),
        entity.x + Angles.trnsx(Time.globalTime + offAng, toX, toY),
        entity.y + Angles.trnsy(Time.globalTime + offAng, toX, toY),
      )
    }

    Draw.z(layer + 0.0003f)
    Lines.stroke(1f, Color.black)
    Lines.poly(entity.x, entity.y, 36, radius + 1f)
  }

  override fun AttackRangeModel.update(delta: Float) {}
}