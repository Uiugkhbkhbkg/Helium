package helium.ui.fragments.placement

import arc.Core
import arc.Events
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.input.KeyCode
import arc.math.Interp
import arc.scene.Element
import arc.scene.Group
import arc.scene.actions.Actions
import arc.scene.event.ClickListener
import arc.scene.event.HandCursorListener
import arc.scene.event.InputEvent
import arc.scene.event.Touchable
import arc.scene.style.Drawable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.ButtonGroup
import arc.scene.ui.Image
import arc.scene.ui.ImageButton
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.HorCollapser
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Table
import arc.struct.ObjectFloatMap
import arc.struct.ObjectMap
import arc.struct.Seq
import arc.util.Align
import helium.He.config
import helium.ui.HeAssets
import helium.util.accessField
import mindustry.Vars
import mindustry.core.UI
import mindustry.game.EventType.BlockInfoEvent
import mindustry.game.EventType.WorldLoadEvent
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.type.Category
import mindustry.ui.Fonts
import mindustry.ui.Styles
import mindustry.ui.fragments.PlacementFragment
import mindustry.world.Block
import kotlin.math.roundToInt

class HePlacementFrag {
  companion object {
    private val returnArray = Seq<Block>()
    private val PlacementFragment.togglerRef: Table by accessField("toggler")
    private var currBlock: Block?
      get() = Vars.control.input.block
      set(value) { Vars.control.input.block = value }
  }

  private var selectionShown = false

  var menuHoverBlock: Block? = null
  var currentCategory = Category.distribution
  val returnCatArray = Seq<Category>()
  val scrollPositions = ObjectFloatMap<Category>()
  var categoryEmpty = BooleanArray(Category.all.size)
  var selectedBlocks = ObjectMap<Category, Block>()

  private val invSlots = Seq<InvSlot>()
  private var currentSlot: InvSlot? = null

  private lateinit var topLevel: Table
  private lateinit var container: Table
  private lateinit var topInfo: Table

  private var wasHovered = false
  private var lastDisplayBlock: Block? = null

  private lateinit var fastInventory: Table
  private lateinit var blockSelection: Table
  private lateinit var toolButtons: Table

  private lateinit var blockTable: Table
  private lateinit var blockPane: ScrollPane

  private lateinit var foldBlocks: HorCollapser
  private lateinit var foldTools: HorCollapser
  private lateinit var foldIcon: Group

  init {
    Events.on(WorldLoadEvent::class.java) { event ->
      Core.app.post {
        currentCategory = Category.distribution
        currBlock = null

        rebuildCategory()
      }
    }
  }

  fun build(parent: Group) {
    parent.fill{ toggler ->
      topLevel = toggler

      toggler.update {
        val old = Vars.ui.hudfrag.blockfrag.togglerRef
        old.visible = !config.enableBetterPlacement

        update()
      }
      toggler.bottom().right().visible { Vars.ui.hudfrag.shown }

      toggler.table{ cont ->
        container = cont

        cont.table(Tex.buttonEdge2){ top ->
          top.left()
          topInfo = top
          buildTopInfo(top)
        }.growX().fillY().touchable(Touchable.enabled)
          .visible{ Vars.control.input.block != null || menuHoverBlock != null }
        cont.row()
        cont.table(Tex.pane){ bottom ->
          bottom.margin(4f).top().defaults().fillX().growY()

          bottom.table { fastInv ->
            fastInv.top()
            fastInventory = fastInv
            buildFastInventory(fastInventory)
          }.padLeft(4f)
          bottom.image().color(Color.darkGray).width(4f).growY()
            .padLeft(4f).padRight(-2f)
          bottom.add(HorCollapser(true) { blocks ->
            blocks.top()
            blockSelection = blocks
            buildBlockSelection(blockSelection)
          }.setDuration(0.3f, Interp.pow3Out).also { foldBlocks = it })
          bottom.image().color(Color.darkGray).width(4f).growY()
            .padLeft(-2f).padRight(0f)
          bottom.table{ tools ->
            tools.top()
            toolButtons = tools
            buildToolbar(toolButtons)
          }
        }.fill()
      }.fill()
    }
  }

  private fun buildTopInfo(table: Table) {
    table.update {
      val topTable = table

      val displayBlock = if (menuHoverBlock != null) menuHoverBlock else Vars.control.input.block
      val isHovered = displayBlock == null

      //don't refresh unnecessarily
      //refresh only when the hover state changes, or the displayed block changes
      if (wasHovered == isHovered && lastDisplayBlock === displayBlock) return@update

      topTable.clear()
      topTable.top().left().margin(14f)

      lastDisplayBlock = displayBlock
      wasHovered = isHovered

      //show details of selected block, with costs
      if (displayBlock != null) {
        topTable.table{ name ->
          name.table { header ->
            header.left()
            header.add(Image(displayBlock.uiIcon)).size((8*4).toFloat())
            header.labelWrap { if (!unlocked(displayBlock)) Core.bundle.get("block.unknown") else displayBlock.localizedName }
              .left().growX().padLeft(5f)
          }.growX().left()
          if (unlocked(displayBlock)) {
            name.button("?", Styles.flatBordert) {
              Vars.ui.content.show(displayBlock)
              Events.fire(BlockInfoEvent())
            }.size((8*5).toFloat()).padTop(-5f).padRight(-5f).right().fill().name("blockinfo")
          }
        }.growX().fillY()
        topTable.row()
        //add requirement table
        topTable.table { req ->
          req.top().left()
          displayBlock.requirements.forEach { stack ->
            req.table { line ->
              line.left()
              line.image(stack.item.uiIcon).size((8*2).toFloat())

              line.add(HorCollapser(!selectionShown){ col ->
                col.add(stack.item.localizedName).maxWidth(140f).fillX().color(Color.lightGray).padLeft(2f).left()
                  .get().setEllipsis(true)
              }.setCollapsed { !selectionShown }.setDuration(0.3f, Interp.pow3Out)).fill().pad(0f)

              line.labelWrap {
                val core = Vars.player.core()
                val stackamount = (stack.amount*Vars.state.rules.buildCostMultiplier).roundToInt()
                if (core == null || Vars.state.rules.infiniteResources) return@labelWrap "*/$stackamount"

                val amount = core.items.get(stack.item)
                val color =
                  (if (amount < stackamount/2f) "[scarlet]" else if (amount < stackamount) "[accent]" else "[white]")
                color + UI.formatAmount(amount.toLong()) + "[white]/" + stackamount
              }.padLeft(5f)
            }.left()
            req.row()
          }
        }.growX().left().margin(3f)

        if ((!displayBlock.isPlaceable || !Vars.player.isBuilder) && !Vars.state.rules.editor) {
          topTable.row()
          topTable.table { b ->
            b.image(Icon.cancel).padRight(2f).color(Color.scarlet)
            b.add(if (!Vars.player.isBuilder) "@unit.nobuild" else if (!displayBlock.supportsEnv(Vars.state.rules.env)) "@unsupported.environment" else "@banned")
              .width(190f).wrap()
            b.left()
          }.padTop(2f).left()
        }
      }
    }
  }

  private fun update() {
    if (selectionShown) {
      currentSlot?.also { slot ->
        slot.block = currBlock
      }
    }
  }

  private fun buildBlockSelection(table: Table) {
    val selection = table.table().growY().fillX().get()

    selection.table(HeAssets.darkGrayUIAlpha) { blocksSelect ->
      blocksSelect.margin(4f).marginTop(0f)
      blockPane = blocksSelect.top().pane(Styles.smallPane) { blocks -> blockTable = blocks }
        .update { pane ->
          if (pane!!.hasScroll()) {
            val result = Core.scene.hoverElement
            if (result == null || !result.isDescendantOf(pane)) {
              Core.scene.setScrollFocus(null)
            }
          }
        }.fill().maxHeight(5*48f).get()
    }.growY().bottom().touchable(Touchable.enabled)
    selection.table(Styles.black6) { categories ->
      //categories.add(object : Image(Styles.black6) {
      //  override fun draw() {
      //    if (height <= Scl.scl(3f)) return
      //    drawable.draw(x, y, width, height - Scl.scl(3f))
      //  }
      //}).colspan(2).growX().growY().padTop(-3f).row()
      categories.top().pane { catPane ->
        catPane.defaults().size(48f)
        val group = ButtonGroup<ImageButton>()

        for (cat in Category.all) {
          val blocks = getUnlockedByCategory(cat)
          categoryEmpty[cat.ordinal] = blocks.isEmpty
        }

        var needsAssign = categoryEmpty[currentCategory.ordinal]

        var f = 0
        for (cat in getCategories()) {
          if (f++%2 == 0) catPane.row()

          if (categoryEmpty[cat.ordinal]) {
            catPane.add()
            continue
          }

          if (needsAssign) {
            currentCategory = cat
            needsAssign = false
          }

          catPane.button(Vars.ui.getIcon(cat.name), Styles.clearNoneTogglei) {
            currentCategory = cat
            if (currBlock != null) {
              currBlock = getSelectedBlock(currentCategory)
            }
            rebuildCategory()
          }.group(group).update { i -> i.setChecked(currentCategory == cat) }.name("category-" + cat.name)
        }
      }.fill().maxHeight(5*48f)
    }.growY().top().touchable(Touchable.enabled)

    rebuildCategory()
  }

  private fun rebuildCategory() {
    blockTable.clear()
    blockTable.top().margin(5f)

    var index = 0
    val rowItems = if (config.doubleFastSlots) 4 else 5

    val group = ButtonGroup<ImageButton>()
    group.setMinCheckCount(0)

    for (block in getUnlockedByCategory(currentCategory)) {
      if (!unlocked(block)) continue
      if (index++%rowItems == 0) {
        blockTable.row()
      }

      val button = blockTable.button(TextureRegionDrawable(block.uiIcon), Styles.selecti) {
        if (unlocked(block)) {
          if ((Core.input.keyDown(KeyCode.shiftLeft) || Core.input.keyDown(KeyCode.controlLeft))
              && Fonts.getUnicode(block.name) != 0) {
            Core.app.clipboardText = Fonts.getUnicode(block.name).toChar().toString() + ""
            Vars.ui.showInfoFade("@copied")
          }
          else {
            currBlock = if (currBlock === block) null else block
            selectedBlocks.put(currentCategory, currBlock)
          }
        }
      }.size(46f).group(group).name("block-" + block.name).get()
      button.resizeImage(Vars.iconMed)

      button.update {
        val core = Vars.player.core()
        val color =
          if ((Vars.state.rules.infiniteResources
               || (core != null && (core.items.has(block.requirements, Vars.state.rules.buildCostMultiplier)
                                    || Vars.state.rules.infiniteResources))
              ) && Vars.player.isBuilder
          ) Color.white
          else Color.gray
        button.forEach { elem -> elem!!.setColor(color) }
        button.setChecked(currBlock === block)
        if (!block.isPlaceable) {
          button.forEach { elem -> elem.setColor(Color.darkGray) }
        }
      }

      button.hovered { menuHoverBlock = block }
      button.exited {
        if (menuHoverBlock === block) {
          menuHoverBlock = null
        }
      }
    }

    if (index < 4) {
      for (i in 0..<4 - index) {
        blockTable.add().size(46f)
      }
    }
    blockTable.act(0f)
    blockPane.setScrollYForce(scrollPositions.get(currentCategory, 0f))
    Core.app.post {
      blockPane.setScrollYForce(scrollPositions.get(currentCategory, 0f))
      blockPane.act(0f)
      blockPane.layout()
    }
  }

  private fun getCategories() =
    returnCatArray.clear().addAll(*Category.all)
      .sort{ c1, c2 -> categoryEmpty[c1.ordinal].compareTo(categoryEmpty[c2.ordinal]) }

  private fun getSelectedBlock(cat: Category) =
    selectedBlocks.get(cat) { getByCategory(cat).find { block -> this.unlocked(block) } }

  private fun getByCategory(cat: Category) = returnArray.selectFrom(Vars.content.blocks()) { block ->
    block!!.category == cat && block.isVisible && block.environmentBuildable() }

  private fun getUnlockedByCategory(cat: Category) =
    returnArray.selectFrom(Vars.content.blocks()) { block ->
      block.category == cat && block.isVisible && unlocked(block)
    }.sort{ b1, b2 -> b2.isPlaceable.compareTo(b1.isPlaceable) }

  private fun unlocked(block: Block) =
    block.unlockedNowHost() && block.placeablePlayer && block.environmentBuildable()
    && block.supportsEnv(Vars.state.rules.env)

  private fun buildFastInventory(table: Table) {
    if (config.doubleFastSlots) {
      table.table { left ->
        left.defaults().size(48f)
        for (i in 0 until 5) {
          val slot = InvSlot()
          left.add(slot)
          left.row()

          invSlots.add(slot)
        }
      }.growY()
    }

    table.table{ right ->
      for (i in 0 until 4) {
        right.defaults().size(48f)
        val slot = InvSlot()
        right.add(slot)
        right.row()

        invSlots.add(slot)
      }

      foldIcon = right.button(Icon.leftOpen, Styles.clearNonei, 28f){
        toggleSelectionShown()
      }.get()
    }.growY()
  }

  private fun toggleSelectionShown() {
    selectionShown = !selectionShown
    if (selectionShown) {
      currentSlot = null

      foldBlocks.setCollapsed(false)
      foldTools.setCollapsed(true)

      foldIcon.clearActions()
      foldIcon.setOrigin(Align.center)
      foldIcon.isTransform = true
      foldIcon.actions(Actions.scaleTo(-1f, 1f, 0.3f))
    }
    else {
      foldBlocks.setCollapsed(true)
      foldTools.setCollapsed(false)

      foldIcon.clearActions()
      foldIcon.setOrigin(Align.center)
      foldIcon.isTransform = true
      foldIcon.actions(Actions.scaleTo(1f, 1f, 0.3f))
    }
  }

  private fun buildToolbar(table: Table) {
    table.table { edits ->
      edits.top().defaults().size(48f)

      val tmp = Table().also { Vars.control.input.buildPlacementUI(it) }
      tmp.children.begin().forEach { but ->
        edits.add(but)
        edits.row()
      }
      tmp.children.end()
    }.fillX().growY()
    table.add(HorCollapser(false, HeAssets.grayUI){ tools ->
      tools.top().defaults().size(48f)

      tools.button(Icon.effect, Styles.clearNonei, 28f) {

      }
    }.setDuration(0.3f, Interp.pow3Out).also { foldTools = it }).fillX().growY()
  }

  private inner class InvSlot(val background: Drawable? = HeAssets.slotsBack): Element() {
    var block: Block? = null

    private val selected: Boolean get() = currentSlot == this

    init {
      clicked {
        if (currentSlot == this) {
          currentSlot = null
          currBlock = null
        }
        else {
          currentSlot = this
          currBlock = block
        }
      }

      addListener(HandCursorListener())
      addListener(object : ClickListener(){
        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Element?) {
          super.enter(event, x, y, pointer, fromActor)

          if (pointer != -1) return

          menuHoverBlock = block
        }

        override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Element?) {
          super.exit(event, x, y, pointer, toActor)

          if (pointer == -1) {
            menuHoverBlock = null
          }
        }
      })
    }

    override fun act(delta: Float) {
      super.act(delta)
      if (block != null && selected && currBlock != block) currentSlot = null
    }

    override fun draw() {
      super.draw()

      background?.draw(x, y, width, height)
      block?.also { Draw.rect(it.uiIcon, x + width / 2, y + height / 2, Scl.scl(32f), Scl.scl(32f)) }

      if (selected) {
        Lines.stroke(Scl.scl(4f), Pal.accent)
        Lines.rect(x, y, width, height)
      }

      Draw.reset()
    }
  }
}