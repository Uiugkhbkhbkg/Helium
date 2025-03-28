package helium;

import arc.Events;
import arc.files.Fi;
import helium.graphics.ScreenSampler;
import mindustry.game.EventType;
import mindustry.mod.Mod;

public class Helium extends Mod {
  public Helium() {
    ScreenSampler.resetMark();
  }

  @Override
  public void init() {
    He.init();
    Events.run(EventType.Trigger.update, He::update);
  }

  public static Fi getInternalFile(String path) {
    return He.modFile.child(path);
  }
}
