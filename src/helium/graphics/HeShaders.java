package helium.graphics;

import arc.Core;
import arc.files.Fi;
import arc.graphics.gl.Shader;
import helium.Helium;

public class HeShaders {
  public static Shader baseScreen;


  public static final Fi internalShaderDir = Helium.getInternalFile("shaders");

  public static void load(){
    baseScreen = new Shader(
        Core.files.internal("shaders/screenspace.vert"),
        internalShaderDir.child("dist_base.frag"));
  }
}
