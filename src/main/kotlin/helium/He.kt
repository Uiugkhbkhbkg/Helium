package helium

import arc.Core
import arc.Events
import arc.files.Fi
import arc.scene.ui.layout.Table
import helium.graphics.HeShaders
import helium.graphics.g2d.EntityRangeExtractor
import helium.ui.HeAssets
import helium.ui.HeStyles
import helium.ui.dialogs.ConfigCheck
import helium.ui.dialogs.ConfigSepLine
import helium.ui.dialogs.ConfigSlider
import helium.ui.dialogs.ModConfigDialog
import helium.ui.fragments.entityinfo.EntityInfoFrag
import helium.ui.fragments.entityinfo.displays.DetailsDisplay
import helium.ui.fragments.entityinfo.displays.EntityRangeDisplay
import helium.ui.fragments.entityinfo.displays.HealthDisplay
import helium.ui.fragments.entityinfo.displays.StatusDisplay
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Icon
import mindustry.graphics.Pal
import mindustry.ui.Styles
import mindustry.ui.dialogs.SettingsMenuDialog

object He {
  private val settingsMenu = SettingsMenuDialog::class.java.getDeclaredField("menu")
    .also { it.isAccessible = true }

  /**本模组的文件位置 */
  private val mod = Vars.mods.getMod(Helium::class.java)

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

  lateinit var config: HeConfig

  lateinit var entityInfo: EntityInfoFrag
  lateinit var healthBarDisplay: HealthDisplay
  lateinit var statusDisplay: StatusDisplay
  lateinit var detailsDisplay: DetailsDisplay
  lateinit var entityRangeDisplay: EntityRangeDisplay

  lateinit var entityRangeRenderer: EntityRangeExtractor

  lateinit var configDialog: ModConfigDialog

  fun init() {
    config = HeConfig(
      configDirectory,
      internalConfigDir.child("mod_config.hjson")
    )
    config.load()

    HeAssets.load()
    HeShaders.load()
    HeStyles.load()

    entityInfo = EntityInfoFrag()
    entityInfo.build(Vars.ui.hudGroup)
    setupDisplays(entityInfo)

    entityRangeRenderer = EntityRangeExtractor()

    configDialog = ModConfigDialog()
    setupSettings(configDialog)

    setupGlobalListeners()

    //add config entry
    Vars.ui.settings.shown {
      val table = settingsMenu[Vars.ui.settings] as Table
      table.button(
        Core.bundle["settings.helium"],
        HeAssets.heIcon,
        Styles.flatt,
        32f
      ) { configDialog.show() }.marginLeft(8f).row();
    }
  }

  private fun setupGlobalListeners() {
    Events.run(EventType.Trigger.update) { update() }
    Events.run(EventType.Trigger.draw) { drawWorld() }

    Events.on(EventType.ResetEvent::class.java) { entityInfo.reset() }
  }

  private fun update() {
    HeStyles.uiBlur.blurScl = config.blurScl
    HeStyles.uiBlur.blurSpace = config.blurSpace
    Styles.defaultDialog.stageBackground = if (config.enableBlur) HeStyles.BLUR_BACK else Styles.black9
  }

  private fun drawWorld() {
    EntityRangeDisplay.resetMark()
    entityInfo.drawWorld()
  }

  private fun setupDisplays(infos: EntityInfoFrag) {
    infos.addDisplay(HealthDisplay().also { healthBarDisplay = it })
    infos.addDisplay(StatusDisplay().also { statusDisplay = it })

    infos.addDisplay(DetailsDisplay().also { detailsDisplay = it })
    infos.addDisplay(EntityRangeDisplay().also { entityRangeDisplay = it })

    healthBarDisplay.style = HeStyles.test
  }

  private fun setupSettings(conf: ModConfigDialog) {
    conf.addConfig(
      "basic", Icon.settings,
      ConfigSepLine(
        "backBlur",
        Core.bundle["settings.basic.backBlur"],
        Pal.accent,
        Pal.accentBack
      ),
      ConfigCheck(
        "enableBlur",
        config::enableBlur
      ),
      ConfigSlider(
        "blurScl",
        config::blurScl,
        1, 8, 1
      ),
      ConfigSlider(
        "blurSpace",
        config::blurSpace,
        0.5f, 8f, 0.25f
      ),

      ConfigSepLine(
        "entityInfo",
        Core.bundle["settings.basic.entityInfo"],
        Pal.accent,
        Pal.accentBack
      ),
      ConfigCheck(
        "enableEntityInfoDisplay",
        config::enableEntityInfoDisplay
      ),
      ConfigCheck(
        "enableHealthBarDisplay",
        config::enableHealthBarDisplay
      ),
      ConfigCheck(
        "enableUnitStatusDisplay",
        config::enableUnitStatusDisplay
      ),
      ConfigSlider(
        "entityInfoScale",
        config::entityInfoScale,
        0.5f, 2f, 0.1f
      ),
      ConfigSlider(
        "entityInfoAlpha",
        config::entityInfoAlpha,
        0.4f, 1f, 0.05f
      ),
      ConfigCheck(
        "showAttackRange",
        config::showAttackRange
      ),
      ConfigCheck(
        "showHealRange",
        config::showHealRange
      ),
      ConfigCheck(
        "showOverdriveRange",
        config::showOverdriveRange
      ),
    )
    conf.addConfig(
      "debug", HeAssets.program,
      ConfigSepLine(
        "logging",
        Core.bundle["settings.debug.logging"],
      ),
      ConfigCheck(
        "loadingInfo",
        config::loadInfo
      )
    )
  }
}
