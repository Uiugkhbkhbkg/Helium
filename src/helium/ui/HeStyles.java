package helium.ui;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import helium.graphics.Blur;
import mindustry.ui.Styles;

public class HeStyles {
  public static Drawable BLUR_BACK;

  public static Blur uiBlur = new Blur(Blur.DEf_B);

  public static void load(){
    BLUR_BACK = new TextureRegionDrawable(Core.atlas.white()) {
      @Override
      public void draw(float x, float y, float width, float height) {
        uiBlur.directDraw(() -> super.draw(x, y, width, height));

        Styles.black5.draw(x, y, width, height);
      }

      @Override
      public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        uiBlur.directDraw(() -> super.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation));

        Styles.black5.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation);
      }
    };
  }
}
