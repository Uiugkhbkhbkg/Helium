package helium.graphics

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.graphics.gl.FrameBuffer
import arc.graphics.gl.Shader
import arc.math.Mathf
import arc.math.geom.Vec2

object DrawUtils {
  private var drawTasks: Array<DrawTask?> = arrayOfNulls(16)
  private var taskBuffer: Array<FrameBuffer?> = arrayOfNulls(16)

  private val v1 = Vec2()
  private val v2 = Vec2()
  private val v3 = Vec2()

  fun dashCircle(
    x: Float, y: Float, radius: Float,
    dashes: Int = 8,
    totalDashDeg: Float = 180f,
    rotate: Float = 0f
  ) {
    if (Mathf.equal(totalDashDeg, 0f)) return

    val totalTransDeg = 360f - totalDashDeg
    val dashDeg = totalDashDeg / dashes
    val transDeg = totalTransDeg / dashes
    val step = dashDeg + transDeg

    for (i in 0 until dashes) {
      Lines.arc(
        x, y, radius,
        dashDeg/360f, rotate + i*step,
        (360/dashDeg).toInt()*2
      )
    }
  }

  /**发布缓存的任务并在首次发布时的z轴时进行绘制，传递的一些参数只在初始化时起了效果，之后都被选择性的无视了
   *
   * @param taskId 任务的标识ID，用于区分任务缓存
   * @param target 传递给绘制任务的数据目标，这是为了优化lambda的内存而添加的，避免产生大量闭包的lambda实例造成不必要的内存占用
   * @param drawFirst **选择性的参数，若任务已初始化，这个参数无效**，用于声明这个任务组在执行前要进行的操作
   * @param drawLast **选择性的参数，若任务已初始化，这个参数无效**，用于声明这个任务组在完成主绘制后要执行的操作
   * @param draw 添加到任务缓存的绘制任务，即此次绘制的操作
   */
  @Suppress("UNCHECKED_CAST")
  fun <T: Any, D: Any> drawTask(
    taskId: Int,
    target: T? = null,
    defTarget: D? = null,
    drawFirst: DrawAcceptor<D>? = null,
    drawLast: DrawAcceptor<D>? = null,
    draw: DrawAcceptor<T>,
  ) {
    while (taskId >= drawTasks.size) {
      drawTasks = drawTasks.copyOf(drawTasks.size*2)
    }

    var task = drawTasks[taskId]
    if (task == null) {
      task = DrawTask()
      drawTasks[taskId] = task
    }
    if (!task.init) {
      task.defaultFirstTask = drawFirst as DrawAcceptor<Any>
      task.defaultLastTask = drawLast as DrawAcceptor<Any>
      task.defaultTarget = defTarget
      task.init = true
      Draw.draw(Draw.z()) { task.flush() }
    }
    task.addTask(target, draw)
  }

  /**发布缓存的任务并在首次发布时的z轴时进行绘制，传递的一些参数只在初始化时起了效果，之后都被选择性的无视了
   *
   * @param taskID 任务的标识id，用于区分任务缓存
   * @param target 递给绘制任务的数据目标，这是为了优化lambda的内存而添加的，避免产生大量闭包的lambda实例造成不必要的内存占用
   * @param shader **选择性的参数，若任务已初始化，这个参数无效**，在这组任务绘制时使用的着色器
   * @param draw 添加到任务缓存的绘制任务，即此次绘制的操作
   */
  fun <T: Any, S: Shader> drawTask(taskID: Int, target: T, shader: S, draw: DrawAcceptor<T>) {
    while (taskID >= taskBuffer.size) {
      taskBuffer = taskBuffer.copyOf(taskBuffer.size*2)
    }

    var buffer = taskBuffer[taskID]
    if (buffer == null) {
      buffer = FrameBuffer()
      taskBuffer[taskID] = buffer
    }
    drawTask(taskID, target, shader, { _ ->
      buffer.resize(Core.graphics.width, Core.graphics.height)
      buffer.begin(Color.clear)
    }, { e ->
      buffer.end()
      buffer.blit(e)
    }, draw)
  }
}

private val none = Any()
class DrawTask {
  var defaultFirstTask: DrawAcceptor<Any>? = null
  var defaultLastTask: DrawAcceptor<Any>? = null
  var defaultTarget: Any? = null

  var tasks: Array<DrawAcceptor<Any>?> = arrayOfNulls(16)
  var dataTarget: Array<Any?> = arrayOfNulls(16)
  var taskCounter: Int = 0
  var init: Boolean = false

  @Suppress("UNCHECKED_CAST")
  fun <T: Any> addTask(dataAcceptor: T?, task: DrawAcceptor<T>) {
    if (tasks.size <= taskCounter) {
      tasks = tasks.copyOf(tasks.size + 1)
      dataTarget = dataTarget.copyOf(tasks.size)
    }

    tasks[taskCounter] = task as DrawAcceptor<Any>
    dataTarget[taskCounter++] = dataAcceptor
  }

  fun flush() {
    defaultFirstTask?.draw(defaultTarget?:none)
    for (i in 0..<taskCounter) {
      tasks[i]?.draw(dataTarget[i]?:none)
    }
    defaultLastTask?.draw(defaultTarget?:none)

    taskCounter = 0
    init = false
  }
}

fun interface DrawAcceptor<T> {
  fun draw(accept: T)
}