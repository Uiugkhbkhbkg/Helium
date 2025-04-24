package helium.ui.fragments.entityinfo

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Rect
import arc.math.geom.Vec2
import arc.scene.Group
import arc.scene.event.Touchable
import arc.scene.ui.Label
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import arc.struct.*
import arc.util.Scaling
import arc.util.Time
import arc.util.Tmp
import arc.util.pooling.Pool.Poolable
import arc.util.pooling.Pools
import helium.He
import helium.He.config
import helium.addEventBlocker
import helium.graphics.EdgeLineStripDrawable
import helium.graphics.FillStripDrawable
import helium.ui.HeAssets
import helium.ui.HeStyles
import helium.ui.elements.roulette.StripButton
import helium.ui.elements.roulette.StripButtonStyle
import helium.ui.elements.roulette.StripWrap
import helium.ui.fragments.entityinfo.Side.*
import helium.util.MutablePair
import helium.util.accessField
import helium.util.mto
import mindustry.Vars
import mindustry.game.Team
import mindustry.gen.*
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.input.Binding
import mindustry.ui.Styles
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EntityInfoFrag {
  companion object {
    private val tempEntry = EntityEntry()

    private val outerStyle = HeStyles.clearS.copyWith(
      over = FillStripDrawable (
        Pal.accent.cpy().a(0.7f),
        Pal.accent.cpy().a(0f)
      ),
      down = EdgeLineStripDrawable (
        Scl.scl(2f),
        Pal.accent,
        Pal.accent.cpy().a(0.7f),
        Pal.accent.cpy().a(0f)
      ),
      checked = FillStripDrawable(
        Pal.accent.cpy(),
        Pal.accent.cpy().a(0f)
      )
    )

    private val Bits.bits: LongArray by accessField("bits")
  }

  var controlling = false

  private val disabledTeams = ObjectMap<EntityInfoDisplay<*>, Bits>()
  private val displays = Seq<EntityInfoDisplay<Model<*>>>()

  private val all = ObjectSet<EntityEntry>()
  private val displayQueue = Seq<EntityEntry>()

  private val hovering = OrderedSet<EntityEntry>()
  private val currHov = OrderedSet<EntityEntry>()

  private var selecting = false
  private val selectStart = Vec2()
  private val selectionRect = Rect()

  private val worldViewport = Rect()
  private val screenViewport = Rect()

  private lateinit var infoFill: Group
  private lateinit var configPane: Table

  /**for hot-update*/
  fun displaySetupUpdated(){
    all.forEach { Pools.free(it) }
    displayQueue.clear()
    all.clear()
  }

  @Suppress("UNCHECKED_CAST")
  fun addDisplay(display: EntityInfoDisplay<*>) {
    displays.add(display as EntityInfoDisplay<Model<*>>)
    disabledTeams.put(display, Bits(256))
    displaySetupUpdated()
  }

  @Suppress("UNCHECKED_CAST")
  fun addDisplayBefore(display: EntityInfoDisplay<*>, target: EntityInfoDisplay<*>){
    val index = displays.indexOf(target)
    display as EntityInfoDisplay<Model<*>>
    if (index == -1) displays.add(display)
    else displays.insert(index, display)
    disabledTeams.put(display, Bits(256))
    displaySetupUpdated()
  }

  @Suppress("UNCHECKED_CAST")
  fun addDisplayAfter(display: EntityInfoDisplay<*>, target: EntityInfoDisplay<*>){
    val index = displays.indexOf(target)
    display as EntityInfoDisplay<Model<*>>
    if (index == -1 || index == displays.size - 1) displays.add(display)
    else displays.insert(index + 1, display)
    disabledTeams.put(display, Bits(256))
    displaySetupUpdated()
  }

  @Suppress("UNCHECKED_CAST")
  fun replaceDisplay(display: EntityInfoDisplay<*>, target: EntityInfoDisplay<*>) {
    val index = displays.indexOf(target)
    display as EntityInfoDisplay<Model<*>>
    if (index != -1) displays[index] = display
    displaySetupUpdated()
  }

  @Suppress("UNCHECKED_CAST")
  fun removeDisplay(display: EntityInfoDisplay<*>) {
    displays.remove(display as EntityInfoDisplay<Model<*>>)
    disabledTeams.remove(display)
    displaySetupUpdated()
  }

  fun toggleSwitchConfig() {
    if (configPane.visible) {
      config.saveAsync()
      configPane.visible = false
    }
    else {
      buildConfig(configPane)
      configPane.visible = true
    }
  }

  private fun buildTeamStyle(team: Team): StripButtonStyle {
    val overColor = team.color.cpy().lerp(Color.white, 0.4f)
    return StripButtonStyle(
      up = FillStripDrawable(team.color.cpy().a(0.7f)),
      over = EdgeLineStripDrawable(
        Scl.scl(3f),
        Color.lightGray,
        overColor
      ),
      down = EdgeLineStripDrawable(
        Scl.scl(3f),
        Color.white,
        overColor
      ),
      checked = EdgeLineStripDrawable(
        Scl.scl(3f),
        Pal.accent.cpy().lerp(Color.white, 0.4f),
        team.color
      ),
      checkedOver = EdgeLineStripDrawable(
        Scl.scl(3f),
        Pal.accent.cpy().lerp(Color.white, 0.4f),
        overColor
      ),
      disabled = FillStripDrawable(
        team.color.cpy().a(0.7f)
      )
    )
  }

  private fun buildConfig(table: Table) {
    table.clearChildren()

    var current: EntityInfoDisplay<*>? = null
    val teams = Vars.state.teams.active
    val stack = Stack()

    val radius = min(min(Core.graphics.width, Core.graphics.height)/2f - Scl.scl(40f), Scl.scl(340f))
    val r1 = radius/2.8f
    val r2 = radius/1.75f
    val r3 = radius
    val r4 = radius*1.32f
    val off = Scl.scl(3f)

    stack.add(StripWrap(background = HeStyles.black5).also {
      it.setCBounds(
        0f, 0f,
        360f, radius
      )
      it.addEventBlocker()
    })

    stack.add(StripButton(HeStyles.clearS){
      it.image(Icon.none).size(42f).scaling(Scaling.fit)
      it.row()
      it.add(Core.bundle["infos.disableAll"], Styles.outlineLabel, 0.75f)
    }.also {
      it.setCBounds(
        90f, 0f,
        120f, r1 - off
      )
      it.clicked { disabledTeams.values().forEach { bits -> Arrays.fill(bits.bits, -0x1L) } }
      it.addEventBlocker()
    })
    stack.add(StripButton(HeStyles.clearS){
      it.image(Icon.filters).size(42f).scaling(Scaling.fit)
      it.row()
      it.add(Core.bundle["infos.allSetting"], Styles.outlineLabel, 0.75f)
    }.also {
      it.setCBounds(
        210f, 0f,
        120f, r1 - off
      )
      it.clicked { He.configDialog.show("entityInfo") }
    })
    stack.add(StripButton(HeStyles.clearS){
      it.image(Icon.effect).size(42f).scaling(Scaling.fit)
      it.row()
      it.add(Core.bundle["infos.enableAll"], Styles.outlineLabel, 0.75f)
    }.also {
      it.setCBounds(
        330f, 0f,
        120f, r1 - off
      )
      it.clicked {
        disabledTeams.values().forEach { bits -> Arrays.fill(bits.bits, 0) }
      }
      it.addEventBlocker()
    })
    stack.add(StripWrap(background = HeAssets.whiteEdge).also {
      it.setColor(Pal.darkestGray)
      it.setCBounds(
        90f, 0f,
        0f, r1 - off
      )
    })
    stack.add(StripWrap(background = HeAssets.whiteEdge).also {
      it.setColor(Pal.darkestGray)
      it.setCBounds(
        210f, 0f,
        0f, r1 - off
      )
    })
    stack.add(StripWrap(background = HeAssets.whiteEdge).also {
      it.setColor(Pal.darkestGray)
      it.setCBounds(
        330f, 0f,
        0f, r1 - off
      )
    })

    stack.add(StripWrap(background = HeStyles.boundBlack).also {
      it.setColor(Pal.darkestGray)
      it.setCBounds(
        0f, r1 - off/2f,
        360f, 0f
      )
    })

    stack.add(StripWrap(
      Label(Core.bundle["infos.enabledTeams"], Styles.outlineLabel).also { it.setFontScale(0.85f) },
      HeStyles.boundBlack5
    ).also {
      it.setCBounds(
        60f, r1,
        60f, r2 - r1
      )
    })
    val teamButtonDelta = 300f/teams.size
    teams.forEachIndexed { i, team ->
      stack.add(StripButton(
        buildTeamStyle(team.team),
        Label(team.team.emoji, Styles.outlineLabel).also { it.setFontScale(2f) }
      ).also {
        it.setCBounds(
          120f + teamButtonDelta*i, r1,
          teamButtonDelta, r2 - r1
        )
        it.setDisabled { current == null }
        it.update { it.isChecked = current?.let { dis -> !disabledTeams[dis].get(team.team.id) }?: false }
        it.clicked { disabledTeams[current!!].flip(team.team.id) }
        it.addEventBlocker()
      })
    }

    stack.add(StripWrap(background = HeStyles.boundBlack).also {
      it.setCBounds(
        0f, r2 + off/2f,
        360f, 0f
      )
    })

    val displayButtonDelta = 360f/displays.size
    displays.forEachIndexed{ i, dis ->
      stack.add(StripButton(outerStyle){ dis.buildConfig(it) }.also {
        it.setCBounds(
          90f + displayButtonDelta*i, r2 + off,
          displayButtonDelta, r3 - r2 - off
        )
        it.update { it.isChecked = current == dis }
        it.clicked { current = dis }
        it.addEventBlocker()
      })
      if (dis is ConfigurableDisplay){
        stack.add(StripWrap(background = HeStyles.black5).also {
          it.setCBounds(
            90f + displayButtonDelta*i, r3 + off,
            displayButtonDelta, r4 - r3 - off
          )
        })
        dis.getConfigures().also { list ->
          val configButtonDelta = displayButtonDelta/list.size
          stack.add(StripWrap(background = HeAssets.whiteEdge).also {
            it.setColor(Pal.darkestGray)
            it.setCBounds(
              90f + displayButtonDelta*(i + 1), r3 + off,
              0f, r4 - r3 - off
            )
          })
          list.forEachIndexed { ci, conf ->
            stack.add(StripButton(HeStyles.toggleClearS){ conf.build(it) }.also {
              it.setCBounds(
                90f + displayButtonDelta*i + configButtonDelta*ci, r3 + off,
                configButtonDelta, r4 - r3 - off
              )
              conf.checked?.also { ch -> it.update { it.isChecked = ch.get() } }
              it.clicked { conf.callback.run() }
              it.addEventBlocker()
            })
            stack.add(StripWrap(background = HeAssets.whiteEdge).also {
              it.setColor(Pal.darkestGray)
              it.setCBounds(
                90f + displayButtonDelta*i + configButtonDelta*ci, r3 + off,
                0f, r4 - r3 - off
              )
            })
          }
          stack.add(StripWrap(background = HeAssets.whiteEdge).also {
            it.setColor(Pal.darkestGray)
            it.setCBounds(
              90f + displayButtonDelta*i, r3 + off/2f,
              displayButtonDelta, 0f
            )
          })
        }
      }
      stack.add(StripWrap(background = HeAssets.whiteEdge).also {
        it.setColor(Pal.darkestGray)
        it.setCBounds(
          90f + displayButtonDelta*i, r2 + off,
          0f, r3 - r2 - off
        )
      })
    }

    table.add(stack).size(0f)
  }

  fun build(parent: Group){
    infoFill = object: Group(){
      override fun draw() {
        if (!config.enableEntityInfoDisplay) return
        drawHUDLay()
        super.draw()
      }

      override fun act(delta: Float) {
        if (!config.enableEntityInfoDisplay){
          if (all.any()) reset()
          return
        }

        super.act(delta)

        updateShowing()

        this@EntityInfoFrag.also {
          update(delta*60)
        }
      }
    }

    parent.addChildAt(0, infoFill)
    infoFill.fillParent = true

    parent.fill { config ->
      configPane = config
      config.touchable = Touchable.enabled
      config.clicked {
        toggleSwitchConfig()
      }
      config.visible = false
    }
  }

  fun reset(){
    all.forEach { Pools.free(it) }
    all.clear()
    displayQueue.clear()
  }

  fun drawWorld(){
    if (!config.enableEntityInfoDisplay) return

    val alpha = config.entityInfoAlpha

    Core.camera.bounds(worldViewport)

    worldViewport.also {
      val bl = it.getPosition(Tmp.v1)
      val tr = Tmp.v2.set(it.x + it.width, it.y + it.height)
      Core.camera.project(bl)
      Core.camera.project(tr)
      tr.sub(bl)
      screenViewport.set(bl.x, bl.y, tr.x, tr.y)
    }

    val rect = selectionRect
    if (selecting && rect.width > 0 && rect.height > 0) {
      Draw.z(Layer.overlayUI)
      Draw.color(Pal.accent, 0.3f)
      Fill.rect(rect)
    }

    if (controlling || Core.input.keyDown(config.entityInfoHotKey)) {
      currHov.forEach { e ->
        val rad = e.size*1.44f + Mathf.absin(4f, 2f)
        val origX = e.entity.x
        val origY = e.entity.y

        Draw.z(Layer.overlayUI)
        Drawf.poly(
          origX, origY, 4,
          rad, 0f,
          if (hovering.contains(e)) Color.crimson else Pal.accent
        )
      }
    }

    displayQueue.forEach { e ->
      if (hovering.contains(e)) {
        val rad = e.size*1.44f
        val ent = e.entity
        val origX = e.entity.x
        val origY = e.entity.y

        if (ent is Unit) {
          Draw.z(if (ent.isFlying) Layer.plans else Layer.groundUnit - 1f)
          Fill.lightInner(
            origX, origY, 4,
            rad*0.7f,
            rad,
            0f,
            Tmp.c3.set(Pal.accent).a(0f),
            Tmp.c2.set(Pal.accent).a(0.7f)
          )
        }

        Draw.z(Layer.overlayUI)
        Lines.stroke(1f, Pal.accent)
        Lines.poly(
          origX, origY, 4,
          rad, 0f
        )
      }

      e.display.forEach r@{ entry ->
        val display = entry.first
        if (!display.worldRender) return@r
        val model = entry.second?:return@r

        if (model.disabledTeam.get(e.entity.team().id)) return@r

        display.apply {
          if (
            (hoveringOnly && !model.checkHovering(checkIsHovering(e)))
            || !model.shouldDisplay()
            || !model.checkWorldClip(worldViewport)
          ) return@r
          model.drawWorld(alpha*e.alpha)
        }
      }
    }
  }

  private fun updateShowing() {
    val mouse = Core.input.mouseWorld()
    val hotkeyDown = controlling || Core.input.keyDown(config.entityInfoHotKey)
    if (hotkeyDown) {
      if (!selecting && Core.input.keyDown(Binding.select)) {
        selecting = true
        selectStart.set(mouse)
        selectionRect.set(mouse.x, mouse.y, 0f, 0f)
      }
      else if (selecting) {
        val beginX = min(selectStart.x, mouse.x)
        val beginY = min(selectStart.y, mouse.y)
        val width = abs(selectStart.x - mouse.x)
        val height = abs(selectStart.y - mouse.y)
        selectionRect.set(beginX, beginY, width, height)
      }
      else {
        selectionRect.set(mouse.x, mouse.y, 0f, 0f)
      }
    }

    currHov.clear()

    Groups.all.forEach { entity ->
      if (entity !is Teamc) return@forEach

      val e = tempEntry
      e.entity = entity

      val ent = all.get(e)
      if (ent == null) {
        val entry = Pools.obtain(EntityEntry::class.java){ EntityEntry() }
        entry.entity = entity

        assignDisplayModels(entry)

        entry.isValid = !entry.display.isEmpty
        entry.showing = true
        all.add(entry)
        if (entry.isValid) {
          displayQueue.add(entry)

          if (checkHovering(entity)) {
            currHov.add(entry)
          }
        }
      }
      else {
        ent.showing = true

        if (ent.isValid && checkHovering(entity)) {
          currHov.add(ent)
        }
      }
    }

    if (selecting && !(hotkeyDown && Core.input.keyDown(Binding.select))){
      if (currHov.isEmpty) hovering.clear()
      else {
        var anyAdded = currHov.size > hovering.size
        currHov.forEach { hov ->
          anyAdded = hovering.add(hov) or anyAdded
        }

        if (!anyAdded) currHov.forEach { hovering.remove(it) }
      }

      selecting = false
      selectStart.setZero()
      selectionRect.set(0f, 0f, 0f, 0f)
    }

    val itr = all.iterator()
    while (itr.hasNext()) {
      val entry = itr.next()

      if (!entry.entity.isAdded) hovering.remove(entry)

      if (!entry.showing && !hovering.contains(entry))
        entry.alpha = Mathf.approach(entry.alpha, 0f, 0.025f*Time.delta)
      else entry.alpha = 1f

      entry.showing = false

      if (entry.alpha <= 0.001f) {
        itr.remove()
        displayQueue.remove(entry)
        Pools.free(entry)
      }
    }

    if (!displayQueue.isEmpty) displayQueue
      .sort { e -> if (hovering.contains(e)) -1f else e.entity.dst2(Core.input.mouseWorld()) }
  }

  @Suppress("UNCHECKED_CAST")
  private fun assignDisplayModels(entry: EntityEntry) {
    val entity = entry.entity

    displays.forEach { display ->
      if (display.valid(entity) && display.enabled()) {
        entry.display.add(
          display mto
          if (display.hoveringOnly) null
          else {
            val model = (display as EntityInfoDisplay<*>).obtainModel(entity)
            model.disabledTeam = disabledTeams.get(display) { Bits() }
            if (display is InputEventChecker<*>) {
              display as InputEventChecker<InputCheckerModel<*>>
              model as InputCheckerModel<*>
              model.element = display.run { model.buildListener().also { infoFill.addChild(it) } }
            }
            model
          }
        )
      }
    }
  }

  private fun checkHovering(entity: Posc): Boolean {
    val rect = selectionRect

    return when(entity){
      is Hitboxc -> rect.overlaps(Tmp.r1.also { entity.hitbox(it) })
      is Building -> {
        val block = entity.block
        val size = (block.size*Vars.tilesize).toFloat()
        return rect.overlaps(
          entity.x - size/2, entity.y - size/2,
          size, size
        )
      }
      else -> {
        return rect.overlaps(
          entity.x - 5, entity.y - 5,
          10f, 10f
        )
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun update(delta: Float) {
    for (i in displayQueue.size - 1 downTo 0) {
      val e = displayQueue[i]

      e.display.forEach r@{
        it.first.apply {
          if (!it.second.checkHovering(checkIsHovering(e)) && hoveringOnly) return@r
          if (it.second == null){
            val model = obtainModel(e.entity)
            model.disabledTeam = disabledTeams.get(this) { Bits() }
            if (this is InputEventChecker<*>){
              this as InputEventChecker<InputCheckerModel<*>>
              model as InputCheckerModel
              model.element = model.buildListener().also { elem -> infoFill.addChild(elem) }
            }
            it.second = model
          }
          it.second!!.update(delta)
        }
      }
    }
  }

  private fun drawHUDLay() {
    val scale = config.entityInfoScale
    val alpha = config.entityInfoAlpha
    val maxSizeMul = 6
    val minSizeMul = 2

    Draw.sort(true)
    for (i in displayQueue.size - 1 downTo 0) {
      val e = displayQueue[i]

      var offsetLeft = 0f
      var offsetRight = 0f
      var offsetTop = 0f
      var offsetBottom = 0f

      var topWidth = 0f
      var bottomWidth = 0f
      var leftHeight = 0f
      var rightHeight = 0f
      var centerWith = 0f
      var centerHeight = 0f

      val origin = Core.camera.project(e.entity.x, e.entity.y)
      val origX = origin.x
      val origY = origin.y

      val a = e.alpha*alpha
      e.display.forEach f@{ entry ->
        val display = entry.first
        if (!display.screenRender) return@f
        val model = entry.second?: return@f

        if (model.disabledTeam.get(e.entity.team().id)) return@f

        display.apply {
          if (
            (hoveringOnly && !model.checkHovering(checkIsHovering(e)))
            || !model.shouldDisplay()
          ) return@f
          when (display.layoutSide) {
            CENTER -> {
              centerWith = max(model.prefWidth, centerWith)
              centerHeight = max(model.prefHeight, centerHeight)
            }
            RIGHT -> rightHeight = max(model.prefHeight, rightHeight)
            TOP -> topWidth = max(model.prefWidth, topWidth)
            LEFT -> leftHeight = max(model.prefHeight, leftHeight)
            BOTTOM -> bottomWidth = max(model.prefWidth, bottomWidth)
          }
        }
      }

      val sizeOrig = Core.camera.project(0f, 0f).x
      val sizeOff = Core.camera.project(e.size, 0f).x - sizeOrig

      e.display.forEach f@{ entry ->
        val display = entry.first
        val model = entry.second?: return@f
        if (model.disabledTeam.get(e.entity.team().id)) return@f

        val dir = if(display.layoutSide == CENTER) Tmp.p1.set(0, 0) else Geometry.d4(display.layoutSide.dir)
        val offset = Tmp.v1.set(sizeOff*dir.x, sizeOff*dir.y)
        val ox = origX + offset.x
        val oy = origY + offset.y

        display.apply {
          if (
            (hoveringOnly && !model.checkHovering(checkIsHovering(e)))
            || !model.shouldDisplay()
          ) return@f
          when (layoutSide) {
            CENTER -> {
              val disW = centerWith*scale
              val disH = centerHeight*scale
              if (model.checkScreenClip(screenViewport, ox - disW/2, oy - disH/2, disW, disH))
                model.draw(a, scale, ox - disW/2, oy - disH/2, disW, disH)
            }
            RIGHT -> {
              val w = model.realWidth(rightHeight)
              val h = model.realHeight(rightHeight)
              val scl = min(max(maxSizeMul*sizeOff/h, minSizeMul*sizeOff/h*scale), scale)
              val disW = w*scl
              val disH = h*scl
              if (model.checkScreenClip(screenViewport, ox + offsetRight, oy - disH/2, disW, disH))
                model.draw(a, scl, ox + offsetRight, oy - disH/2, disW, disH)

              offsetRight += disW
            }
            TOP -> {
              val w = model.realWidth(topWidth)
              val h = model.realHeight(topWidth)
              val scl = min(max(maxSizeMul*sizeOff/w, minSizeMul*sizeOff/w*scale), scale)
              val disW = w*scl
              val disH = h*scl
              if (model.checkScreenClip(screenViewport, ox - disW/2, oy + offsetTop, disW, disH))
                model.draw(a, scl, ox - disW/2, oy + offsetTop, disW, disH)

              offsetTop += disH
            }
            LEFT -> {
              val w = model.realWidth(leftHeight)
              val h = model.realHeight(leftHeight)
              val scl = min(max(maxSizeMul*sizeOff/h, minSizeMul*sizeOff/h*scale), scale)
              val disW = w*scl
              val disH = h*scl
              if (model.checkScreenClip(screenViewport, ox - disW - offsetLeft, oy - disH/2, disW, disH))
                model.draw(a, scl, ox - disW - offsetLeft, oy - disH/2, disW, disH)

              offsetLeft += disW
            }
            BOTTOM -> {
              val w = model.realWidth(bottomWidth)
              val h = model.realHeight(bottomWidth)
              val scl = min(max(maxSizeMul*sizeOff/w, minSizeMul*sizeOff/w*scale), scale)
              val disW = w*scl
              val disH = h*scl
              if (model.checkScreenClip(screenViewport, ox - disW/2, oy - disH - offsetBottom, disW, disH))
                model.draw(a, scl, ox - disW/2, oy - disH - offsetBottom, disW, disH)

              offsetBottom += disH
            }
          }
        }
      }
    }
    Draw.flush()
    Draw.sort(false)
  }

  private fun checkIsHovering(e: EntityEntry) =
    hovering.contains(e) || ((controlling || Core.input.keyDown(config.entityInfoHotKey)) && currHov.contains(e))
}

class EntityEntry : Poolable {
  lateinit var entity: Teamc
  var alpha = 0f
  var showing = false
  var isValid = false

  val display = Seq<MutablePair<EntityInfoDisplay<Model<*>>, Model<*>?>>()

  var size: Float = -1f
    get() = if (field < 0) entity.let {
      field = when (it) {
        is Hitboxc -> it.hitSize()/1.44f
        is Building -> it.block.size*Vars.tilesize/2f
        else -> 10f
      }
      field
    } else field

  override fun reset() {
    size = -1f
    display.forEach { e ->
      e.second?.also {
        if (it is InputCheckerModel) it.element.remove()
        Pools.free(it)
      }
    }
    display.clear()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is EntityEntry) return false
    return entity === other.entity
  }

  override fun hashCode(): Int {
    return entity.hashCode()
  }
}

