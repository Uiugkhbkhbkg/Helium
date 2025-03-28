package helium;

import arc.files.Fi;
import helium.graphics.HeShaders;
import helium.graphics.ScreenSampler;
import helium.ui.HeStyles;
import mindustry.Vars;
import mindustry.mod.Mods;
import mindustry.ui.Styles;

import static arc.Core.settings;
import static helium.ui.HeStyles.uiBlur;

public class He {
  /**本模组的文件位置*/
  public static final Mods.LoadedMod mod = Vars.mods.getMod(Helium.class);
  /**此模组的压缩包对象*/
  public static final Fi modFile = mod.root;

  /**此mod内部名称*/
  public static final String modName = "he";

  /**模组内配置文件存放位置*/
  public static final Fi internalConfigDir = modFile.child("config");
  /**模组文件夹位置*/
  public static final Fi modDirectory = settings.getDataDirectory().child("mods");
  /**模组配置文件夹*/
  public static final Fi configDirectory = modDirectory.child("config").child(modName);

  public static ModConfig config;

  public static void init() {
    config = new ModConfig(
        configDirectory,
        internalConfigDir.child("mod_config.hjson")
    );
    config.load();

    ScreenSampler.setup();
    HeShaders.load();
    HeStyles.load();
  }

  public static void update() {
    uiBlur.blurScl = config.blurLevel;
    uiBlur.blurSpace = config.backBlurLen;
    Styles.defaultDialog.stageBackground = config.enableBlur? HeStyles.BLUR_BACK: Styles.black9;
  }
}
