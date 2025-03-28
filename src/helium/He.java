package helium;

import arc.files.Fi;
import helium.graphics.HeShaders;
import helium.graphics.ScreenSampler;
import helium.ui.HeStyles;
import mindustry.Vars;
import mindustry.mod.Mods;
import mindustry.ui.Styles;

public class He {
  /**本模组的文件位置*/
  public static final Mods.LoadedMod mod = Vars.mods.getMod(Helium.class);
  /**此模组的压缩包对象*/
  public static final Fi modFile = mod.root;

  public static boolean enableBlur = true;

  public static void init() {
    ScreenSampler.setup();
    HeShaders.load();
    HeStyles.load();
  }

  public static void update() {
    Styles.defaultDialog.stageBackground = enableBlur? HeStyles.BLUR_BACK: Styles.black9;
  }
}
