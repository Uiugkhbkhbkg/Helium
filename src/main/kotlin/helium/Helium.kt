package helium

import arc.Core
import arc.files.Fi
import arc.graphics.g2d.TextureRegion
import arc.scene.style.Drawable
import helium.graphics.ScreenSampler
import mindustry.mod.Mod

class Helium : Mod() {
  init {
    ScreenSampler.resetMark()
  }

  override fun init() {
    ScreenSampler.setup()

    He.init()
  }

  companion object {
    fun getInternalFile(path: String): Fi {
      val paths = path.split('/')
      var res = He.modFile

      paths.forEach {
        res = res.child(it)
      }

      return res
    }

    fun <T: Drawable> getDrawable(spriteName: String): T =
      Core.atlas.getDrawable("${He.INTERNAL_NAME}-$spriteName")
    fun getAtlas(spriteName: String): TextureRegion =
      Core.atlas.find("${He.INTERNAL_NAME}-$spriteName")
  }
}
