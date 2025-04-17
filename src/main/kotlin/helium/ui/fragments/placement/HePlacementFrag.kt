package helium.ui.fragments.placement

import arc.scene.Group
import arc.scene.ui.layout.Table
import helium.util.accessField
import mindustry.Vars
import mindustry.ui.fragments.PlacementFragment

class HePlacementFrag {
  companion object {
    private val PlacementFragment.togglerRef by accessField<PlacementFragment, Table>("toggler")
  }

  var defaultHidden = true

  fun build(parent: Group) {
    parent.fill{ toggler ->
      toggler.update {
        val old = Vars.ui.hudfrag.blockfrag.togglerRef
        old.visible = !defaultHidden
      }
      toggler.bottom().right().visible { Vars.ui.hudfrag.shown }


    }
  }
}