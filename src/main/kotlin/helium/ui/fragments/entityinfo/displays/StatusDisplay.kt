package helium.ui.fragments.entityinfo.displays

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.scene.ui.layout.Scl
import arc.struct.Seq
import arc.util.Align
import arc.util.Tmp
import helium.ui.fragments.entityinfo.EntityInfoDisplay
import helium.ui.fragments.entityinfo.Model
import helium.ui.fragments.entityinfo.Side
import mindustry.Vars
import mindustry.gen.Posc
import mindustry.gen.Statusc
import mindustry.graphics.Pal
import mindustry.type.StatusEffect
import mindustry.ui.Fonts
import kotlin.math.max

private val iconSize = Scl.scl(26f)
private val iconPadding = Scl.scl(4f)

class StatusModel: Model<Statusc>(){
  var singleWidth = 1f

  val statusList = Seq<StatusEffect>()

  override fun setup(ent: Statusc) {
    singleWidth = iconSize
  }
  override fun reset() {
    statusList.clear()
    singleWidth = iconSize
  }
}

class StatusDisplay: EntityInfoDisplay<StatusModel>(::StatusModel) {
  override val layoutSide: Side = Side.TOP

  override val StatusModel.prefHeight: Float
    get() = iconSize
  override val StatusModel.prefWidth: Float
    get() = iconSize

  override fun valid(entity: Posc) = entity is Statusc

  override fun StatusModel.shouldDisplay(): Boolean {
    return Vars.content.statusEffects().any{ entity.hasEffect(it) }
  }

  override fun StatusModel.realHeight(prefSize: Float): Float{
    val n = Mathf.ceil(prefSize/(singleWidth + iconPadding))
    return n*iconSize
  }
  override fun StatusModel.realWidth(prefSize: Float) = prefSize

  override fun StatusModel.draw(alpha: Float, scale: Float, origX: Float, origY: Float, drawWidth: Float, drawHeight: Float) {
    var offX = 0f
    var offY = 0f

    val drawW = drawWidth*scale
    val singW = singleWidth*scale

    var newSingleW = 0f

    var rh = iconSize
    statusList.forEach { eff ->
      if (offX + singW >= drawW) {
        offX = iconPadding
        offY += iconSize*scale
        rh += (iconSize + iconPadding)*scale
      }
      newSingleW = max(drawStatus(eff, alpha, scale, origX + offX, origY + offY), newSingleW)
      offX += singW + iconPadding*scale
    }

    singleWidth = newSingleW
  }

  private fun StatusModel.drawStatus(
    status: StatusEffect,
    alpha: Float, scale: Float,
    origX: Float, origY: Float,
  ): Float {
    val icon = status.uiIcon
    val iconHeight = iconSize*scale
    val iconWidth = iconSize*(icon.width.toFloat()/icon.height.toFloat())*scale

    var dw = iconWidth/scale

    Draw.color(Color.white, alpha)
    Draw.rect(icon, origX + iconWidth/2, origY + iconHeight/2, iconWidth, iconHeight)

    if (status.permanent) return dw
    val time = entity.getDuration(status)
    if (time > 0) {
      val timeWidth = Fonts.outline.draw(
        formatTime(time),
        origX, origY + Fonts.outline.lineHeight*scale*0.7f,
        Tmp.c1.set(Pal.accent).a(alpha),
        scale*0.7f,
        false,
        Align.bottomLeft
      ).width/scale

      dw = max(dw, timeWidth)
    }

    return dw
  }

  private fun formatTime(ticks: Float): String {
    val seconds = ticks/60f
    if (seconds > 60){
      val minutes = seconds / 60
      if (minutes > 60){
        val hours = minutes / 60
        return "${Mathf.round(hours)}h"
      } else {
        return "${Mathf.round(minutes)}m"
      }
    }
    else {
      return "${Mathf.round(seconds)}s"
    }
  }

  override fun StatusModel.update(delta: Float) {
    val list = statusList
    list.clear()
    Vars.content.statusEffects().forEach { eff ->
      if (entity.hasEffect(eff)) {
        list.add(eff)
      }
    }
  }
}
