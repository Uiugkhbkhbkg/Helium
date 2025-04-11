package helium.graphics

import arc.graphics.Color
import arc.graphics.VertexAttribute.color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.NinePatch
import arc.math.Mathf
import arc.scene.style.NinePatchDrawable

@Suppress("LeakingThis")
open class NinePatchClipDrawable: BaseClipDrawable {
  companion object {
    private val tmpColor = Color()

    private val clazz = NinePatch::class.java
    private val idxField = clazz.getDeclaredField("idx").also { it.isAccessible = true }
    private val botLeftField = clazz.getDeclaredField("bottomLeft").also { it.isAccessible = true }
    private val botCenterField = clazz.getDeclaredField("bottomCenter").also { it.isAccessible = true }
    private val botRightField = clazz.getDeclaredField("bottomRight").also { it.isAccessible = true }
    private val midLeftField = clazz.getDeclaredField("middleLeft").also { it.isAccessible = true }
    private val midCenterField = clazz.getDeclaredField("middleCenter").also { it.isAccessible = true }
    private val midRightField = clazz.getDeclaredField("middleRight").also { it.isAccessible = true }
    private val topLeftField = clazz.getDeclaredField("topLeft").also { it.isAccessible = true }
    private val topCenterField = clazz.getDeclaredField("topCenter").also { it.isAccessible = true }
    private val topRightField = clazz.getDeclaredField("topRight").also { it.isAccessible = true }
    private val verticesField = clazz.getDeclaredField("vertices").also { it.isAccessible = true }
  }

  protected lateinit var ninePatch: NinePatch
  protected lateinit var originVert: FloatArray
  protected var idx = 0
  protected var bottomLeft = -1
  protected var bottomCenter = -1
  protected var bottomRight = -1
  protected var middleLeft = -1
  protected var middleCenter = -1
  protected var middleRight = -1
  protected var topLeft = -1
  protected var topCenter = -1
  protected var topRight = -1

  private val vertices = FloatArray(9*4*6)

  /** Creates an uninitialized NinePatchDrawable. The ninepatch must be [setPatch] before use.  */
  constructor() {
  }

  constructor(patch: NinePatch) {
    setPatch(patch)
  }

  constructor(drawable: NinePatchClipDrawable): super(drawable) {
    setPatch(drawable.ninePatch)
  }

  constructor(drawable: NinePatchDrawable): this(drawable.patch!!) {
    minWidth = drawable.minWidth
    minHeight = drawable.minHeight
    leftWidth = drawable.leftWidth
    rightWidth = drawable.rightWidth
    topHeight = drawable.topHeight
    bottomHeight = drawable.bottomHeight
  }

  override fun draw(
    x: Float, y: Float, width: Float, height: Float,
    clipLeft: Float, clipRight: Float, clipTop: Float, clipBottom: Float,
  ) {
    val vertices = this.vertices
    val patch = this.ninePatch

    if (clipLeft > 0f || clipRight > 0f || clipTop > 0f || clipBottom > 0f) {
      prepareVertices(
        vertices, originVert, patch,
        x, y, width, height, 1f, 1f,
        clipLeft, clipRight, clipTop, clipBottom
      )
    }
    else {
      prepareVertices(
        vertices, originVert, patch,
        x, y, width, height, 1f, 1f
      )
    }

    Draw.vert(patch.texture, vertices, 0, idx)
  }

  override fun draw(
    x: Float, y: Float, originX: Float, originY: Float,
    width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float,
    clipLeft: Float, clipRight: Float, clipTop: Float, clipBottom: Float,
  ) {
    val vertices = this.vertices
    val patch = this.ninePatch
    val n = this.idx

    if (clipLeft > 0f || clipRight > 0f || clipTop > 0f || clipBottom > 0) {
      prepareVertices(
        vertices, originVert, patch,
        x, y, width, height, scaleX, scaleY,
        clipLeft, clipRight, clipTop, clipBottom
      )
    }
    else {
      prepareVertices(
        vertices, originVert, patch,
        x, y, width, height, scaleX, scaleY
      )
    }

    val worldOriginX = x + originX
    val worldOriginY = y + originY
    if (rotation != 0f) {
      var i = 0
      while (i < n) {
        val vx = (vertices[i] - worldOriginX)*scaleX
        val vy = (vertices[i + 1] - worldOriginY)*scaleY
        val cos = Mathf.cosDeg(rotation)
        val sin = Mathf.sinDeg(rotation)
        vertices[i] = cos*vx - sin*vy + worldOriginX
        vertices[i + 1] = sin*vx + cos*vy + worldOriginY
        i += 6
      }
    }
    else if (scaleX != 1f || scaleY != 1f) {
      var i = 0
      while (i < n) {
        vertices[i] = (vertices[i] - worldOriginX)*scaleX + worldOriginX
        vertices[i + 1] = (vertices[i + 1] - worldOriginY)*scaleY + worldOriginY
        i += 6
      }
    }
    Draw.vert(patch.texture, vertices, 0, n)
  }

  open fun setPatch(patch: NinePatch) {
    this.ninePatch = patch
    minWidth = patch.totalWidth
    minHeight = patch.totalHeight
    leftWidth = patch.padLeft
    rightWidth = patch.padRight
    topHeight = patch.padTop
    bottomHeight = patch.padBottom

    idx = idxField.getInt(patch)

    bottomLeft = botLeftField.getInt(patch)
    bottomCenter = botCenterField.getInt(patch)
    bottomRight = botRightField.getInt(patch)
    middleLeft = midLeftField.getInt(patch)
    middleCenter = midCenterField.getInt(patch)
    middleRight = midRightField.getInt(patch)
    topLeft = topLeftField.getInt(patch)
    topCenter = topCenterField.getInt(patch)
    topRight = topRightField.getInt(patch)

    originVert = verticesField.get(patch) as FloatArray
  }

  /** Creates a new drawable that renders the same as this drawable tinted the specified color.  */
  fun tint(tint: Color?): NinePatchClipDrawable {
    val drawable = NinePatchClipDrawable(this)
    drawable.ninePatch = NinePatch(drawable.ninePatch, tint)
    return drawable
  }

  @Suppress("DuplicatedCode")
  protected open fun prepareVertices(
    vertices: FloatArray, originVert: FloatArray, patch: NinePatch,
    x: Float, y: Float, width: Float, height: Float, scaleX: Float, scaleY: Float,
  ) {
    vertices.fill(0f)

    val centerColumnX = x + leftWidth
    val rightColumnX = x + width - rightWidth
    val middleRowY = y + bottomHeight
    val topRowY = y + height - topHeight
    val c: Float = tmpColor.set(patch.color).mul(Draw.getColor()).toFloatBits()

    if (bottomLeft != -1) set(
      vertices, bottomLeft,
      x, y, centerColumnX - x, middleRowY - y,
      originVert[bottomLeft + 3], originVert[bottomLeft + 4],
      originVert[bottomLeft + 15], originVert[bottomLeft + 16],
      c
    )
    if (bottomCenter != -1) set(
      vertices, bottomCenter,
      centerColumnX, y, rightColumnX - centerColumnX, middleRowY - y,
      originVert[bottomCenter + 3], originVert[bottomCenter + 4],
      originVert[bottomCenter + 15], originVert[bottomCenter + 16],
      c
    )
    if (bottomRight != -1) set(
      vertices, bottomRight,
      rightColumnX, y, x + width - rightColumnX, middleRowY - y,
      originVert[bottomRight + 3], originVert[bottomRight + 4],
      originVert[bottomRight + 15], originVert[bottomRight + 16],
      c
    )
    if (middleLeft != -1) set(
      vertices, middleLeft,
      x, middleRowY, centerColumnX - x, topRowY - middleRowY,
      originVert[middleLeft + 3], originVert[middleLeft + 4],
      originVert[middleLeft + 15], originVert[middleLeft + 16],
      c
    )
    if (middleCenter != -1) set(
      vertices, middleCenter,
      centerColumnX, middleRowY, rightColumnX - centerColumnX, topRowY - middleRowY,
      originVert[middleCenter + 3], originVert[middleCenter + 4],
      originVert[middleCenter + 15], originVert[middleCenter + 16],
      c
    )
    if (middleRight != -1) set(
      vertices, middleRight,
      rightColumnX, middleRowY, x + width - rightColumnX, topRowY - middleRowY,
      originVert[middleRight + 3], originVert[middleRight + 4],
      originVert[middleRight + 15], originVert[middleRight + 16],
      c
    )
    if (topLeft != -1) set(
      vertices, topLeft,
      x, topRowY, centerColumnX - x, y + height - topRowY,
      originVert[topLeft + 3], originVert[topLeft + 4],
      originVert[topLeft + 15], originVert[topLeft + 16],
      c
    )
    if (topCenter != -1) set(
      vertices, topCenter,
      centerColumnX, topRowY, rightColumnX - centerColumnX, y + height - topRowY,
      originVert[topCenter + 3], originVert[topCenter + 4],
      originVert[topCenter + 15], originVert[topCenter + 16],
      c
    )
    if (topRight != -1) set(
      vertices, topRight,
      rightColumnX, topRowY, x + width - rightColumnX, y + height - topRowY,
      originVert[topRight + 3], originVert[topRight + 4],
      originVert[topRight + 15], originVert[topRight + 16],
      c
    )
  }

  @Suppress("DuplicatedCode")
  protected open fun prepareVertices(
    vertices: FloatArray, originVert: FloatArray, patch: NinePatch,
    x: Float, y: Float, width: Float, height: Float, scaleX: Float, scaleY: Float,
    cLeft: Float, cRight: Float, cTop: Float, cBottom: Float,
  ) {
    vertices.fill(0f)

    if (cLeft > width - cRight || cTop > height - cBottom) return

    val centerColumnX = x + this.leftWidth
    val rightColumnX = x + width - this.rightWidth
    val middleRowY = y + this.bottomHeight
    val topRowY = y + height - this.topHeight

    val bottomHeight = this.bottomHeight
    val topHeight = this.topHeight
    val leftWidth = this.leftWidth
    val rightWidth = this.rightWidth
    val centerWidth = width - leftWidth - rightWidth
    val centerHeight = height - bottomHeight - topHeight

    val clipToX = width - cRight
    val clipYoY = height - cTop

    val fromX = x + cLeft
    val fromY = y + cBottom
    val toX = x + clipToX
    val toY = y + clipYoY

    val c = tmpColor.set(patch.color).mul(Draw.getColor()).toFloatBits()

    if (bottomLeft != -1 && fromX < centerColumnX && fromY < middleRowY) {
      val u1 = originVert[bottomLeft + 3]
      val v1 = originVert[bottomLeft + 4]
      val u2 = originVert[bottomLeft + 15]
      val v2 = originVert[bottomLeft + 16]

      val rateXL = cLeft/leftWidth
      val rateYB = cBottom/bottomHeight
      val rateXR = Mathf.clamp(clipToX/leftWidth)
      val rateYT = Mathf.clamp(clipYoY/bottomHeight)

      set(
        vertices, bottomLeft,
        fromX, fromY,
        leftWidth*(rateXR - rateXL), bottomHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
    if (bottomCenter != -1 && fromX < rightColumnX && toX > centerColumnX && fromY < middleRowY) {
      val u1 = originVert[bottomCenter + 3]
      val v1 = originVert[bottomCenter + 4]
      val u2 = originVert[bottomCenter + 15]
      val v2 = originVert[bottomCenter + 16]

      val rateXL = Mathf.clamp((cLeft - leftWidth)/centerWidth)
      val rateYB = cBottom/bottomHeight
      val rateXR = Mathf.clamp((clipToX - leftWidth)/centerWidth)
      val rateYT = Mathf.clamp(clipYoY/bottomHeight)

      set(
        vertices, bottomCenter,
        centerColumnX + centerWidth*rateXL, fromY,
        centerWidth*(rateXR - rateXL), bottomHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
    if (bottomRight != -1 && toX > rightColumnX && fromY < middleRowY) {
      val u1 = originVert[bottomRight + 3]
      val v1 = originVert[bottomRight + 4]
      val u2 = originVert[bottomRight + 15]
      val v2 = originVert[bottomRight + 16]

      val rateXL = Mathf.clamp((cLeft - leftWidth - centerWidth)/rightWidth)
      val rateYB = cBottom/bottomHeight
      val rateXR = Mathf.clamp((clipToX - leftWidth - centerWidth)/rightWidth)
      val rateYT = Mathf.clamp(clipYoY/bottomHeight)

      set(
        vertices, bottomRight,
        rightColumnX + rightWidth*rateXL, fromY,
        rightWidth*(rateXR - rateXL), bottomHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }

    if (middleLeft != -1 && fromX < centerColumnX && fromY < topRowY && toY > middleRowY) {
      val u1 = originVert[middleLeft + 3]
      val v1 = originVert[middleLeft + 4]
      val u2 = originVert[middleLeft + 15]
      val v2 = originVert[middleLeft + 16]

      val rateXL = cLeft/leftWidth
      val rateYB = Mathf.clamp((cBottom - bottomHeight)/centerHeight)
      val rateXR = Mathf.clamp(clipToX/leftWidth)
      val rateYT = Mathf.clamp((clipYoY - bottomHeight)/centerHeight)

      set(
        vertices, middleLeft,
        fromX, middleRowY + centerHeight*rateYB,
        leftWidth*(rateXR - rateXL), centerHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
    if (middleCenter != -1 && fromX < rightColumnX && toX > centerColumnX && fromY < topRowY && toY > middleRowY) {
      val u1 = originVert[middleCenter + 3]
      val v1 = originVert[middleCenter + 4]
      val u2 = originVert[middleCenter + 15]
      val v2 = originVert[middleCenter + 16]

      val rateXL = Mathf.clamp((cLeft - leftWidth)/centerWidth)
      val rateYB = Mathf.clamp((cBottom - bottomHeight)/centerHeight)
      val rateXR = Mathf.clamp((clipToX - leftWidth)/centerWidth)
      val rateYT = Mathf.clamp((clipYoY - bottomHeight)/centerHeight)

      set(
        vertices, middleCenter,
        centerColumnX + centerWidth*rateXL, middleRowY + centerHeight*rateYB,
        centerWidth*(rateXR - rateXL), centerHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
    if (middleRight != -1 && toX > rightColumnX && fromY < topRowY && toY > middleRowY) {
      val u1 = originVert[middleRight + 3]
      val v1 = originVert[middleRight + 4]
      val u2 = originVert[middleRight + 15]
      val v2 = originVert[middleRight + 16]

      val rateXL = Mathf.clamp((cLeft - leftWidth - centerWidth)/rightWidth)
      val rateYB = Mathf.clamp((cBottom - bottomHeight)/centerHeight)
      val rateXR = Mathf.clamp((clipToX - leftWidth - centerWidth)/rightWidth)
      val rateYT = Mathf.clamp((clipYoY - bottomHeight)/centerHeight)

      set(
        vertices, middleRight,
        rightColumnX + rightWidth*rateXL, middleRowY + centerHeight*rateYB,
        rightWidth*(rateXR - rateXL), centerHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }

    if (topLeft != -1 && fromX < centerColumnX && toY > topRowY) {
      val u1 = originVert[topLeft + 3]
      val v1 = originVert[topLeft + 4]
      val u2 = originVert[topLeft + 15]
      val v2 = originVert[topLeft + 16]

      val rateXL = cLeft/leftWidth
      val rateYB = Mathf.clamp((cBottom - bottomHeight - centerHeight)/topHeight)
      val rateXR = Mathf.clamp(clipToX/leftWidth)
      val rateYT = Mathf.clamp((clipYoY - bottomHeight - centerHeight)/topHeight)

      set(
        vertices, topLeft,
        fromX, middleRowY + centerHeight + topHeight*rateYB,
        leftWidth*(rateXR - rateXL), topHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
    if (topCenter != -1 && fromX < rightColumnX && toX > centerColumnX && toY > topRowY) {
      val u1 = originVert[topCenter + 3]
      val v1 = originVert[topCenter + 4]
      val u2 = originVert[topCenter + 15]
      val v2 = originVert[topCenter + 16]

      val rateXL = Mathf.clamp((cLeft - leftWidth)/centerWidth)
      val rateYB = Mathf.clamp((cBottom - bottomHeight - centerHeight)/topHeight)
      val rateXR = Mathf.clamp((clipToX - leftWidth)/centerWidth)
      val rateYT = Mathf.clamp((clipYoY - bottomHeight - centerHeight)/topHeight)

      set(
        vertices, topCenter,
        centerColumnX + centerWidth*rateXL, middleRowY + centerHeight + topHeight*rateYB,
        centerWidth*(rateXR - rateXL), topHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
    if (topRight != -1 && toX > rightColumnX && toY > topRowY) {
      val u1 = originVert[topRight + 3]
      val v1 = originVert[topRight + 4]
      val u2 = originVert[topRight + 15]
      val v2 = originVert[topRight + 16]

      val rateXL = Mathf.clamp((cLeft - leftWidth - centerWidth)/rightWidth)
      val rateYB = Mathf.clamp((cBottom - bottomHeight - centerHeight)/topHeight)
      val rateXR = Mathf.clamp((clipToX - leftWidth - centerWidth)/rightWidth)
      val rateYT = Mathf.clamp((clipYoY - bottomHeight - centerHeight)/topHeight)

      set(
        vertices, topRight,
        rightColumnX + rightWidth*rateXL, middleRowY + centerHeight + topHeight*rateYB,
        rightWidth*(rateXR - rateXL), topHeight*(rateYT - rateYB),
        u1 + (u2 - u1)*rateXL, v1 + (v2 - v1)*rateYB, u1 + (u2 - u1)*rateXR, v1 + (v2 - v1)*rateYT,
        c
      )
    }
  }

  protected fun set(
    vertices: FloatArray, idx: Int,
    x: Float, y: Float, width: Float, height: Float,
    u1: Float, v1: Float, u2: Float, v2: Float,
    color: Float,
  ){
    val fx2 = x + width
    val fy2 = y + height
    val mixColor = Color.clearFloatBits
    vertices[idx] = x
    vertices[idx + 1] = y
    vertices[idx + 2] = color
    vertices[idx + 3] = u1
    vertices[idx + 4] = v1
    vertices[idx + 5] = mixColor

    vertices[idx + 6] = x
    vertices[idx + 7] = fy2
    vertices[idx + 8] = color
    vertices[idx + 9] = u1
    vertices[idx + 10] = v2
    vertices[idx + 11] = mixColor

    vertices[idx + 12] = fx2
    vertices[idx + 13] = fy2
    vertices[idx + 14] = color
    vertices[idx + 15] = u2
    vertices[idx + 16] = v2
    vertices[idx + 17] = mixColor

    vertices[idx + 18] = fx2
    vertices[idx + 19] = y
    vertices[idx + 20] = color
    vertices[idx + 21] = u2
    vertices[idx + 22] = v1
    vertices[idx + 23] = mixColor
  }
}