package helium

import arc.Core
import arc.Events
import arc.files.Fi
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.scene.style.Drawable
import helium.graphics.ScreenSampler
import mindustry.game.EventType
import mindustry.mod.Mod

class Helium : Mod() {
  init {
    ScreenSampler.resetMark()
  }

  override fun init() {
    ScreenSampler.setup()

    He.init()
    Events.run(EventType.Trigger.update) { He.update() }
  }

  companion object {
    fun getInternalFile(path: String): Fi {
      return He.modFile.child(path)
    }

    fun <T: Drawable> getDrawable(spriteName: String): T =
      Core.atlas.getDrawable("${He.INTERNAL_NAME}-$spriteName")
    fun getAtlas(spriteName: String): TextureRegion =
      Core.atlas.find("${He.INTERNAL_NAME}-$spriteName")
  }
}
