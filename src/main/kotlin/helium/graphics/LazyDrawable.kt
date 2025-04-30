package helium.graphics

import arc.func.Prov
import arc.scene.style.Drawable

class LazyDrawable(
  private val prov: Prov<Drawable>
): Drawable {
  private var drawable: Drawable? = null

  private fun check(){
    if (drawable == null) drawable = prov.get()
  }

  override fun draw(x: Float, y: Float, width: Float, height: Float) {
    check()
    drawable!!.draw(x, y, width, height)
  }

  override fun draw(
    x: Float, y: Float, originX: Float, originY: Float,
    width: Float, height: Float, scaleX: Float, scaleY: Float,
    rotation: Float,
  ) {
    check()
    drawable!!.draw(x, y, originX, originY, width, height, scaleX, scaleY, rotation)
  }

  override fun getLeftWidth() = drawable?.leftWidth?:0f
  override fun setLeftWidth(leftWidth: Float) { drawable?.leftWidth = leftWidth }
  override fun getRightWidth() = drawable?.rightWidth?:0f
  override fun setRightWidth(rightWidth: Float) { drawable?.rightWidth = rightWidth }
  override fun getTopHeight() = drawable?.topHeight?:0f
  override fun setTopHeight(topHeight: Float) { drawable?.topHeight = topHeight }
  override fun getBottomHeight() = drawable?.bottomHeight?:0f
  override fun setBottomHeight(bottomHeight: Float) { drawable?.bottomHeight = bottomHeight }

  override fun getMinWidth() = drawable?.minWidth?:0f
  override fun setMinWidth(minWidth: Float) { drawable?.minWidth = minWidth }
  override fun getMinHeight() = drawable?.minHeight?:0f
  override fun setMinHeight(minHeight: Float) { drawable?.minHeight = minHeight }
}