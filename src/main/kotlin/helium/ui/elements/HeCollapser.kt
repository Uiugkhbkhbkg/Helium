package helium.ui.elements

import arc.func.Boolp
import arc.func.Cons
import arc.graphics.g2d.Draw
import arc.math.Interp
import arc.scene.actions.TemporalAction
import arc.scene.event.Touchable
import arc.scene.style.Drawable
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import arc.util.ArcRuntimeException

class HeCollapser(
  private var collTable: Table,
  var collapse: Boolean,
  private val collX: Boolean,
  private val collY: Boolean
) : WidgetGroup() {
  var collapsedFunc: Boolp? = null
  private val collapseAction = CollapseAction()
  var actionRunning = false
  var currentWidth = 0f
  var currentHeight = 0f

  constructor(
    collX: Boolean,
    collY: Boolean,
    collapsed: Boolean = false,
    background: Drawable? = null,
    cons: Cons<Table>
  ) : this(Table(background), collapsed, collX, collY) {
    cons.get(collTable)
  }

  init {
    isTransform = true

    updateTouchable()
    addChild(collTable)
  }

  fun setDuration(seconds: Float, interp: Interp = Interp.linear): HeCollapser {
    this.collapseAction.duration = seconds
    this.collapseAction.interpolation = interp
    return this
  }

  fun setCollapsed(collapsed: Boolp): HeCollapser {
    this.collapsedFunc = collapsed
    return this
  }

  fun toggle() {
    setCollapsed(!collapse)
  }

  fun setCollapsed(collapse: Boolean) {
    this.collapse = collapse
    updateTouchable()

    actionRunning = true

    addAction(collapseAction)
    collapseAction.restart()
  }

  private fun updateTouchable() {
    val touchable1 = if (collapse) Touchable.disabled else Touchable.enabled
    this.touchable = touchable1
  }

  override fun draw() {
    if (currentWidth > 1 && currentHeight > 1) {
      Draw.flush()
      if (clipBegin(x, y, currentWidth, currentHeight)) {
        super.draw()
        Draw.flush()
        clipEnd()
      }
    }
  }

  override fun drawChildren() {
    if (collapse && !actionRunning) return
    super.drawChildren()
  }

  override fun act(delta: Float) {
    super.act(delta)

    if (collapsedFunc != null) {
      val col = collapsedFunc!!.get()
      if (col != collapse) {
        setCollapsed(col)
      }
    }
  }

  override fun layout() {
    collTable.setBounds(0f, 0f, getWidth(), getHeight())

    if (!actionRunning) {
      currentWidth = if (collX) if (collapse) 0f else collTable.prefWidth else getWidth()
      currentHeight = if (collY) if (collapse) 0f else collTable.prefHeight else getHeight()
    }
  }

  override fun getPrefWidth(): Float {
    if (!collX) return collTable.prefWidth

    if (!actionRunning) {
      return if (collapse) 0f
      else collTable.prefWidth
    }

    return currentWidth
  }

  override fun getPrefHeight(): Float {
    if (!collY) return collTable.prefHeight

    if (!actionRunning) {
      return if (collapse) 0f
      else collTable.prefHeight
    }

    return currentHeight
  }

  fun setTable(table: Table) {
    this.collTable = table
    clearChildren()
    addChild(table)
  }

  override fun getMinWidth(): Float {
    return 0f
  }

  override fun getMinHeight(): Float {
    return 0f
  }

  override fun childrenChanged() {
    super.childrenChanged()
    if (getChildren().size > 1) throw ArcRuntimeException("Only one actor can be added to CollapsibleWidget")
  }

  private inner class CollapseAction : TemporalAction() {
    override fun act(delta: Float) = super.act(delta) || !actionRunning

    override fun begin() {
      actionRunning = true
    }
    override fun update(percent: Float) {
      if (collX) {
        currentWidth =
          if (collapse) (1 - percent)*collTable.prefWidth
          else percent*collTable.prefWidth
      }
      if (collY) {
        currentHeight =
          if (collapse) (1 - percent)*collTable.prefHeight
          else percent*collTable.prefHeight
      }

      invalidateHierarchy()
    }

    override fun end() {
      actionRunning = false
    }
  }
}