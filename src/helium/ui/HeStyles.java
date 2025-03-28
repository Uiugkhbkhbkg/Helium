package helium.ui;

import arc.Core;
import arc.Events;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import helium.graphics.Blur;
import mindustry.game.EventType;
import mindustry.ui.Styles;

public class HeStyles {
  public static Drawable BLUR_BACK;

  public static Blur uiBlur = new Blur(Blur.DEf_B);

  private static int drawingCounter;
  private static int lastDialogs;

  public static void load(){
    Events.run(EventType.Trigger.uiDrawBegin, () -> drawingCounter = 0);
    Events.run(EventType.Trigger.uiDrawEnd, () -> lastDialogs = drawingCounter);

    BLUR_BACK = new TextureRegionDrawable(Core.atlas.white()) {
      @Override
      public void draw(float x, float y, float width, float height) {
        drawingCounter++;
        if (drawingCounter == lastDialogs) uiBlur.directDraw(() -> super.draw(x, y, width, height));

        Styles.black5.draw(x, y, width, height);
      }

      @Override
      public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        drawingCounter++;
        if (drawingCounter == lastDialogs) uiBlur.directDraw(() -> super.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation));

        Styles.black5.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation);
      }
    };
  }
}
