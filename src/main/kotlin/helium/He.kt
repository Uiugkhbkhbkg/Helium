package helium

import arc.Core
import arc.files.Fi
import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Table
import helium.graphics.HeShaders
import helium.graphics.g2d.AttackRangeExtractor
import helium.ui.HeStyles
import helium.ui.dialogs.ConfigCheck
import helium.ui.dialogs.ConfigSepLine
import helium.ui.dialogs.ConfigSlider
import helium.ui.dialogs.ModConfigDialog
import helium.ui.fragments.entityinfo.EntityInfoFrag
import helium.ui.fragments.entityinfo.displays.AttackRangeDisplay
import helium.ui.fragments.entityinfo.displays.DetailsDisplay
import helium.ui.fragments.entityinfo.displays.HealthDisplay
import helium.ui.fragments.entityinfo.displays.StatusDisplay
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.mod.Mods.LoadedMod
import mindustry.ui.Styles
import mindustry.ui.dialogs.SettingsMenuDialog

object He {
  private val settingsMenu = SettingsMenuDialog::class.java.getDeclaredField("menu")
    .also { it.isAccessible = true }

  /**本模组的文件位置 */
  private val mod: LoadedMod = Vars.mods.getMod(Helium::class.java)

  /**此模组的压缩包对象 */
  val modFile: Fi = mod.root

  /**此mod内部名称 */
  const val INTERNAL_NAME: String = "he"
  const val MOD_NAME: String = "Helium"

  /**模组内配置文件存放位置 */
  val internalConfigDir: Fi = modFile.child("config")
  /**模组文件夹位置 */
  val modDirectory: Fi = Core.settings.dataDirectory.child("mods")
  /**模组配置文件夹 */
  val configDirectory: Fi = modDirectory.child("config").child(INTERNAL_NAME)

  lateinit var config: ModConfig

  lateinit var entityInfo: EntityInfoFrag
  lateinit var healthBarDisplay: HealthDisplay
  lateinit var statusDisplay: StatusDisplay
  lateinit var detailsDisplay: DetailsDisplay
  lateinit var attackRangeDisplay: AttackRangeDisplay

  lateinit var attackRenderer: AttackRangeExtractor

  lateinit var configDialog: ModConfigDialog

  fun init() {
    config = ModConfig(
      configDirectory,
      internalConfigDir.child("mod_config.hjson")
    )
    config.load()

    HeShaders.load()
    HeStyles.load()

    entityInfo = EntityInfoFrag()
    entityInfo.build(Vars.ui.hudGroup)
    setupDisplays(entityInfo)

    attackRenderer = AttackRangeExtractor()

    configDialog = ModConfigDialog()
    setupSettings(configDialog)

    //add config entry
    Vars.ui.settings.shown {
      val table = settingsMenu[Vars.ui.settings] as Table
      table.button(
        Core.bundle["settings.helium"],
        HeStyles.heIcon,
        Styles.flatt,
        32f
      ) { configDialog.show() }.marginLeft(8f).row();
    }
  }

  fun update() {
    HeStyles.uiBlur.blurScl = config.blurScl
    HeStyles.uiBlur.blurSpace = config.blurSpace
    Styles.defaultDialog.stageBackground = if (config.enableBlur) HeStyles.BLUR_BACK else Styles.black9
  }

  fun drawWorld() {
    entityInfo.drawWorld()

    Draw.drawRange(Layer.light - 2, 0.5f, {
      attackRenderer.capture()
    }) {
      attackRenderer.render()
    }
  }

  private fun setupDisplays(infos: EntityInfoFrag) {
    infos.addDisplay(HealthDisplay().also { healthBarDisplay = it })
    infos.addDisplay(StatusDisplay().also { statusDisplay = it })

    infos.addDisplay(DetailsDisplay().also { detailsDisplay = it })
    infos.addDisplay(AttackRangeDisplay().also { attackRangeDisplay = it })

    healthBarDisplay.style = HeStyles.test
  }

  private fun setupSettings(conf: ModConfigDialog) {
    conf.addConfig(
      "basic", Icon.settings,
      ConfigSepLine(
        "basic",
        Core.bundle["settings.basic.backBlur"],
        Pal.accent,
        Pal.accentBack
      ),
      ConfigCheck(
        "enableBlur",
        { config.enableBlur = it },
        { config.enableBlur },
      ),
      ConfigSlider(
        "blurScl",
        { f -> config.blurScl = f.toInt() },
        { config.blurScl.toFloat() },
        1f, 8f, 1f
      ),
      ConfigSlider(
        "blurSpace",
        { f -> config.blurSpace = f },
        { config.blurSpace },
        0.5f, 8f, 0.25f
      ),
    )
  }
}
