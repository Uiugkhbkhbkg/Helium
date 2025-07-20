package helium.graphics

import arc.func.Cons
import arc.graphics.gl.Shader
import mindustry.content.Fx
import mindustry.entities.Effect
import mindustry.graphics.Layer
import mindustry.graphics.Shaders

class ShieldRenderer {
  private lateinit var oldShader: Shader
  private lateinit var oldAbsorb: Cons<Effect.EffectContainer>

  fun setup(){
    oldShader = Shaders.shield

    Fx.absorb.run {
      oldAbsorb = renderer
      lifetime = 150f
      layer = Layer.shields + 2

      renderer = Cons {

      }
    }
  }

  fun update(){

  }
}