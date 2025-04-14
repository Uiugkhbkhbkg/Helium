package helium.ui.fragments.entityinfo.displays

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Font
import arc.graphics.g2d.FontCache
import arc.math.Mat
import arc.math.Mathf
import arc.scene.ui.layout.Scl
import arc.util.Align
import arc.util.Tmp.c1
import helium.graphics.ClipDrawable
import helium.ui.HeAssets
import helium.ui.fragments.entityinfo.EntityInfoDisplay
import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.Side
import mindustry.Vars
import mindustry.core.UI
import mindustry.gen.*
import mindustry.graphics.Pal
import mindustry.ui.Fonts
import kotlin.math.max

class HealthModel: Model<Healthc>{
  override lateinit var entity: Healthc

  var detailWidth = 0f
  var shieldWidth = 0f

  var hovering = false

  var lastHealth = 0f
  var lastShield = 0f
  var lastAlpha = 1f
  var lastScale = 1f
  var lastN = 0
  val detailBuff = StringBuilder()
  val shieldBuff = StringBuilder()

  var detailCache = FontCache(Fonts.outline)
  var shieldCache = FontCache(Fonts.outline)

  var insectHealth = 0f
  var insectShield = 0f

  var shieldEnt: Shieldc? = null

  override fun setup(ent: Healthc) {
    shieldEnt = if (ent is Shieldc) ent else null
    insectHealth = ent.health()
    insectShield = shieldEnt?.shield()?: 0f
  }

  override fun reset() {
    insectHealth = 0f
    insectShield = 0f
    hovering = false
  }
}

class HealthDisplay: EntityInfoDisplay<HealthModel>(::HealthModel){
  private val trans = Mat()

  lateinit var style: HealthBarStyle

  override val layoutSide: Side = Side.TOP
  override fun valid(entity: Posc) = entity is Healthc

  override fun HealthModel.realWidth(prefSize: Float) = prefSize
  override fun HealthModel.realHeight(prefSize: Float) = prefHeight

  override val HealthModel.prefWidth: Float get() = entity.let {
    val minWidth = max(
      style.background.minWidth,
      style.texOffX + detailWidth + Scl.scl(20f) + shieldWidth + style.shieldPadRight
    )

    when(it) {
      is Hitboxc -> max(it.hitSize()*8f, minWidth)
      is Buildingc -> max(it.block().size*Vars.tilesize*8f, minWidth)
      else -> minWidth
    }
  }
  override val HealthModel.prefHeight: Float get() = style.height

  override fun HealthModel?.checkHovering(isHovered: Boolean): Boolean {
    this?.hovering = isHovered
    return isHovered
  }
  override fun HealthModel.shouldDisplay() = entity !is Buildingc || hovering

  override fun HealthModel.update(delta: Float) {
    insectHealth = Mathf.lerp(insectHealth, entity.health(), delta*0.05f)
    shieldEnt?.also {
      insectShield = Mathf.lerp(insectShield, it.shield(), delta*0.05f)
    }
  }

  override fun HealthModel.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) {
    val drawW = drawWidth/scale
    val drawH = drawHeight/scale

    val progressHealth = Mathf.clamp(entity.health()/entity.maxHealth())
    val insProgHealth = Mathf.clamp(insectHealth/entity.maxHealth())

    val healthBarWidth = (drawW - style.healthPadLeft - style.healthPadRight)
    val shieldBarWidth = (drawW - style.shieldPadLeft - style.shieldPadRight)

    val teamC = entity.let{ if(it is Teamc) it.team().color else Color.white }

    Draw.z(0f)
    Draw.color(c1.set(style.backgroundColor).mulA(alpha))
    style.background.draw(
      origX, origY, 0f, 0f,
      drawW, drawH, scale, scale, 0f,
      0f, 0f, 0f, 0f
    )

    Draw.color(Pal.lightishGray, alpha)
    style.healthBar.draw(
      origX, origY, 0f, 0f,
      drawW, drawH, scale, scale, 0f,
      style.healthPadLeft.toFloat(),
      (style.healthPadRight + healthBarWidth*(1 - insProgHealth)),
      0f, 0f
    )

    Draw.color(teamC, alpha)
    style.healthBar.draw(
      origX, origY, 0f, 0f,
      drawW, drawH, scale, scale, 0f,
      style.healthPadLeft.toFloat(),
      style.healthPadRight + healthBarWidth*(1 - progressHealth),
      0f, 0f
    )

    shieldWidth = 0f
    style.shieldBar?.also {
      val n = Mathf.ceil(insectShield/entity.maxHealth())
      val insProgShield = (insectShield%entity.maxHealth())/entity.maxHealth()
      val r = if (Vars.state.isPaused) 0f else Mathf.absin(8f, 0.5f)

      if (n > 1) {
        Draw.color(shieldColor(teamC, n - 1), alpha*(1f - r))
        it.draw(
          origX, origY, 0f, 0f,
          drawW, drawH, scale, scale, 0f,
          style.shieldPadLeft + shieldBarWidth*insProgShield,
          style.shieldPadRight.toFloat(),
          0f, 0f
        )
      }

      Draw.color(shieldColor(teamC, n), alpha*(1f - r))
      it.draw(
        origX, origY, 0f, 0f,
        drawW, drawH, scale, scale, 0f,
        style.shieldPadLeft.toFloat(),
        style.shieldPadRight + shieldBarWidth*(1 - insProgShield),
        0f, 0f
      )
    }

    style.foreground?.also {
      Draw.color(c1.set(style.foregroundColor).mulA(alpha))
      it.draw(
        origX, origY, 0f, 0f,
        drawW, drawH, scale, scale, 0f,
        0f, 0f, 0f, 0f
      )
    }

    Draw.z(10f)
    val alp = if (hovering) 1f else alpha*(Mathf.clamp((scale - 0.5f)/0.5f))
    if(alp > 0.001f){
      updateText(drawWidth, scale, alp)
      if (style.shieldBar != null) {
        shieldCache.setPosition(origX, origY)
        shieldCache.draw()
      }
      detailCache.setPosition(origX, origY)
      detailCache.draw()
    }
  }

  private fun shieldColor(teamC: Color, n: Int): Color = when(n % 3) {
    0 -> if (teamC.diff(Pal.reactorPurple) < 0.1f) Pal.heal else Pal.reactorPurple
    1 -> if (teamC.diff(Pal.accent) < 0.1f) HeAssets.lightBlue else Pal.accent
    2 -> if (teamC.diff(Pal.heal) < 0.1f) Pal.lancerLaser else Pal.heal
    else -> throw RuntimeException("wtf?")
  }

  private fun HealthModel.updateText(drawWidth: Float, scale: Float, alpha: Float){
    if (detailCache.font != style.font) detailCache = FontCache(style.font)
    if (shieldCache.font != style.font) shieldCache = FontCache(style.font)

    val health = entity.health()
    val shield = shieldEnt?.shield()?: 0f
    if (scale != lastScale || health != lastHealth || (shield != lastShield)){
      detailBuff.apply {
        clear()
        append("\uE813 ")
        append(Mathf.round(health))
        append("/")
        append(Mathf.round(entity.maxHealth()))
        if (shield > 0) {
          append(" - ")
          append("\uE84D ")
          append(UI.formatAmount(Mathf.round(shield).toLong()))
        }
      }
      detailCache.clear()
      detailCache.color = c1.set(Color.white).a(alpha)
      style.font.apply {
        val pscale = getData().scaleX
        val pint = usesIntegerPositions()
        getData().setScale(scale*style.fontScl)
        setUseIntegerPositions(false)
        detailWidth = detailCache.setText(
          detailBuff,
          style.texOffX*Scl.scl(scale),
          style.texOffY*Scl.scl(scale) + style.font.capHeight,
          0f,
          Align.topLeft,
          false
        ).width
        getData().setScale(pscale)
        setUseIntegerPositions(pint)
      }

      lastHealth = health
      lastShield = shield
    }

    val n = Mathf.ceil((shieldEnt?.shield()?:0f)/entity.maxHealth())

    if (scale != lastScale || lastN != n) {
      shieldBuff.clear()
      if (n > 1) shieldBuff.append("x").append(n)

      val fontColor =
        if (n >= 10000) Color.crimson
        else if (n >= 1000) Color.red
        else if (n >= 100) Pal.accent
        else if (n >= 10) Color.white
        else Color.lightGray
      shieldCache.clear()
      shieldCache.color = c1.set(fontColor).a(alpha)
      style.font.apply {
        val pscale = getData().scaleX
        val pint = usesIntegerPositions()
        getData().setScale(scale*style.shieldFontScl)
        setUseIntegerPositions(false)
        shieldWidth = shieldCache.setText(
          shieldBuff,
          drawWidth - style.shieldsOffX*Scl.scl(scale),
          style.shieldsOffY*Scl.scl(scale) + style.font.capHeight,
          0f,
          Align.topRight,
          false
        ).width
        getData().setScale(pscale)
        setUseIntegerPositions(pint)
      }

      lastN = n
    }

    if (!Mathf.equal(lastAlpha, alpha, 0.001f)){
      detailCache.setAlphas(alpha)
      shieldCache.setAlphas(alpha)
    }

    lastScale = scale
    lastAlpha = alpha
  }
}

class HealthBarStyle(
  val background: ClipDrawable,
  val backgroundColor: Color = Color.white,
  val foreground: ClipDrawable? = null,
  val foregroundColor: Color = Color.white,
  val healthBar: ClipDrawable,
  val healthPadLeft: Int,
  val healthPadRight: Int,
  val shieldBar: ClipDrawable? = null,
  val shieldPadLeft: Int = 0,
  val shieldPadRight: Int = 0,
  val font: Font = Fonts.outline,
  val fontScl: Float = 0.8f,
  val texOffX: Int,
  val texOffY: Int,
  val shieldFontScl: Float = 0.7f,
  val shieldsOffX: Int,
  val shieldsOffY: Int,
) {
  val height: Float = background.minHeight
}
