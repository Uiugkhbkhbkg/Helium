package helium.ui.fragments.entityinfo.displays

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Rect
import arc.struct.Bits
import arc.util.Time
import arc.util.Tmp
import helium.He
import helium.He.entityRangeRenderer
import helium.graphics.DrawUtils
import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.WorldDrawOnlyDisplay
import mindustry.game.Team
import mindustry.gen.*
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.world.blocks.defense.ForceProjector.ForceBuild
import mindustry.world.blocks.defense.MendProjector.MendBuild
import mindustry.world.blocks.defense.OverdriveProjector.OverdriveBuild
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild
import mindustry.world.blocks.units.RepairTower
import mindustry.world.blocks.units.RepairTurret
import mindustry.world.meta.BlockStatus

class EntityRangeModel: Model<Ranged> {
  override lateinit var entity: Ranged

  var building: Building? = null
  var vis = 0f
  var range = 0f

  var hovering = false
  var isUnit = false
  var isTurret = false
  var isRepair = false
  var isOverdrive = false

  var timeOffset = 0f
  var phaseOffset = 0f
  var phaseScl = 0f

  val color = Color()
  var alpha = 0f

  var layerID = 0
  var layerOffset = 0f

  override fun setup(ent: Ranged) {
    timeOffset = Mathf.random(240f)
    phaseOffset = Mathf.random(360f)
    phaseScl = Mathf.random(0.9f, 1.1f)

    if (ent is Building) building = ent

    when(ent) {
      is Unitc -> isUnit = true
      is BaseTurretBuild -> isTurret = true
      is RepairTurret.RepairPointBuild -> isRepair = true
      is RepairTower.RepairTowerBuild -> isRepair = true
      is MendBuild -> isRepair = true
      is OverdriveBuild -> isOverdrive = true
    }

    layerID = when{
      isUnit || isTurret -> {
        color.set(ent.team().color)
        alpha = 0.1f
        ent.team().id
      }
      isRepair -> {
        color.set(Pal.heal)
        alpha = 0.075f
        260
      }
      isOverdrive -> {
        color.set(0.731f, 0.522f, 0.425f, 1f)
        alpha = 0.075f
        261
      }
      else -> 300
    }
    layerOffset = layerID*0.01f
  }

  override fun reset() {
    hovering = false
    isUnit = false
    isTurret = false
    isRepair = false
    isOverdrive = false
    layerID = 0
    layerOffset = 0f
    color.set(Color.clear)
    alpha = 0f
    vis = 0f
  }
}

class EntityRangeDisplay: WorldDrawOnlyDisplay<EntityRangeModel>(::EntityRangeModel) {
  companion object {
    private var coneDrawing = false

    private val teamBits = Bits(Team.all.size)
    private var dashes = 0f

    fun resetMark(){
      teamBits.clear()
      coneDrawing = false
      dashes = 0f
    }
  }

  override fun EntityRangeModel.shouldDisplay() = vis > 0 && He.config.let {
    !it.hiddenTeams.contains(entity.team().id)
    && (
      ((isUnit || isTurret) && it.showAttackRange)
      || (isRepair && it.showHealRange)
      || (isOverdrive && it.showOverdriveRange)
    )
  }


  override val worldRender: Boolean get() = true
  override val screenRender: Boolean get() = false

  override fun valid(entity: Posc): Boolean = entity is Ranged && entity !is ForceBuild
  override fun enabled() = He.config.let {
    it.showAttackRange || it.showHealRange || it.showOverdriveRange
  }

  override fun EntityRangeModel.checkWorldClip(worldViewport: Rect) = (range*2).let { clipSize ->
    worldViewport.overlaps(
      entity.x - clipSize/2, entity.y - clipSize/2,
      clipSize, clipSize
    )
  }

  override fun EntityRangeModel?.checkHovering(isHovered: Boolean): Boolean {
    this?.hovering = isHovered
    return isHovered
  }

  override fun EntityRangeModel.draw(alpha: Float) {
    val a = (alpha*vis).let { if (it >= 0.999f) 1f else Interp.pow3Out.apply(it) }
    val radius = range*a
    val layer = Layer.light - 3 + layerOffset
    val size = entity.let {
      when(it) {
        is Hitboxc -> it.hitSize()*1.44f
        is Buildingc -> it.hitSize()*1.44f
        else -> 16f
      }
    }

    if (!teamBits.get(layerID)){
      teamBits.set(layerID)
      Draw.drawRange(layer, 0.0045f, {
        entityRangeRenderer.capture()
      }) {
        entityRangeRenderer.alpha = this.alpha
        entityRangeRenderer.render()
      }
    }

    Draw.z(layer + 0.001f)
    Draw.color(Color.black, color, a)
    DrawUtils.fillCircle(entity.x, entity.y, radius - 1f)

    Draw.z(layer + 0.002f)
    val r = (Time.time*phaseScl + timeOffset)%240/240f
    val inner = Interp.pow3.apply(r)
    val outer = Interp.pow3Out.apply(r)

    DrawUtils.innerLightCircle(
      entity.x, entity.y,
      inner*radius, outer*radius,
      Tmp.c1.set(Color.white).a(0f), Color.white, 1
    )

    val strokeOff = radius/16f
    val ang = Time.time*phaseScl + phaseOffset
    for (i in 0 until 4) {
      val dir = Geometry.d4(i)
      val offX = dir.x*size*inner
      val offY = dir.y*size*inner
      val toX = dir.x*(radius - strokeOff - 8f)*outer
      val toY = dir.y*(radius - strokeOff - 8f)*outer

      val off = Tmp.v1.set(offX, offY).rotate(ang)
      val to = Tmp.v2.set(toX, toY).rotate(ang)

      Lines.stroke(strokeOff*(1 - inner)*a, Color.white)
      Lines.line(
        entity.x + off.x, entity.y + off.y,
        entity.x + to.x, entity.y + to.y
      )
    }

    Draw.z(layer + 0.003f)
    Lines.stroke(1f, Color.black)
    DrawUtils.lineCircle(entity.x, entity.y, radius + 1f)

    if (hovering) {
      Draw.z(Layer.light + 5)
      Draw.color(entity.team().color, 0.1f + Mathf.absin(8f, 0.15f))
      if (isTurret && entity is TurretBuild) drawTurretAttackCone(entity as TurretBuild)
      else if (isUnit) drawUnitAttackCone(entity as Unitc)
    }
  }

  private fun drawUnitAttackCone(unit: Unitc) {
    unit.mounts().forEach { weapon ->
      val type = weapon.weapon
      val coneAngle = type.shootCone
      val weaponRot = if (weapon.rotate) weapon.rotation else type.baseRotation
      val dir = weaponRot + unit.rotation()
      val off = Tmp.v1.set(type.x, type.y).rotate(unit.rotation() - 90)
      off.add(Tmp.v2.set(type.shootX, type.shootY).rotate(unit.rotation() - 90 + weaponRot))

      val dx = unit.x() + off.x
      val dy = unit.y() + off.y

      DrawUtils.circleFan(
        dx, dy, type.range(),
        coneAngle*2, dir - coneAngle
      )
    }
  }

  private fun drawTurretAttackCone(turretBuild: TurretBuild) {
    val block = turretBuild.block as Turret
    val dir = turretBuild.buildRotation()
    val coneAngle = block.shootCone
    val offset = Tmp.v1.set(block.shootX, block.shootY).rotate(turretBuild.buildRotation() - 90)

    DrawUtils.circleFan(
   turretBuild.x() + offset.x, turretBuild.y() + offset.y,
      block.range, coneAngle*2, dir - coneAngle
    )
  }

  override fun EntityRangeModel.update(delta: Float) {
    range = entity.range()
    val to = building?.let {
      if (it.status() != BlockStatus.noInput) 1f else 0f
    }?:1f
    if (!Mathf.equal(vis, to)) vis = Mathf.approach(vis, to, delta*0.04f)
  }
}
