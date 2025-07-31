package helium.graphics

import arc.Core
import arc.Events
import arc.graphics.Color
import arc.graphics.GL30
import arc.graphics.GLTexture
import arc.graphics.Gl
import arc.graphics.g2d.Draw
import arc.graphics.gl.FrameBuffer
import arc.graphics.gl.GLFrameBuffer
import arc.graphics.gl.Shader
import arc.util.serialization.Jval
import helium.util.accessField
import mindustry.Vars
import mindustry.game.EventType
import mindustry.graphics.Layer
import mindustry.graphics.Pixelator

object ScreenSampler {
    private val <T : GLTexture> GLFrameBuffer<T>.lastBoundFramebuffer: GLFrameBuffer<T>? by accessField("lastBoundFramebuffer")
    private val Pixelator.buffer: FrameBuffer by accessField("buffer")

    private val pixelatorBuffer by lazy { Vars.renderer.pixelator.buffer }

    private var worldBuffer: FrameBuffer? = null
    private var uiBuffer: FrameBuffer? = null

    private var currBuffer: FrameBuffer? = null
    private var activity = false

    fun resetMark() {
        Core.settings.remove("sampler.setup")
    }

    fun setup() {
        if (activity) throw RuntimeException("forbid setup sampler twice")

        var e = Jval.read(Core.settings.getString("sampler.setup", "{enabled: false}"))

        if (!e.getBool("enabled", false)) {
            e = Jval.newObject()
            e.put("enabled", true)
            e.put("className", ScreenSampler::class.java.name)
            e.put("worldBuffer", "worldBuffer")
            e.put("uiBuffer", "uiBuffer")

            worldBuffer = FrameBuffer()
            uiBuffer = FrameBuffer()

            Core.settings.put("sampler.setup", e.toString())

            Events.run(EventType.Trigger.preDraw) {
                if (Vars.renderer.pixelate) {
                    pixelatorBuffer.end()
                    beginWorld()
                    pixelatorBuffer.begin()
                } else beginWorld()
            }
            Events.run(EventType.Trigger.draw) {
                Draw.draw(Layer.end + 0.001f) { endWorld() }
            }

            Events.run(EventType.Trigger.uiDrawBegin) { beginUI() }
            Events.run(EventType.Trigger.uiDrawEnd) { endUI() }
        } else {
            val className = e.getString("className")
            val worldBufferName = e.getString("worldBuffer")
            val uiBufferName = e.getString("uiBuffer")
            val clazz = Class.forName(className)
            val worldBufferField = clazz.getDeclaredField(worldBufferName)
            val uiBufferField = clazz.getDeclaredField(uiBufferName)

            worldBufferField.isAccessible = true
            uiBufferField.isAccessible = true
            worldBuffer = worldBufferField[null] as FrameBuffer
            uiBuffer = uiBufferField[null] as FrameBuffer

            Events.run(EventType.Trigger.preDraw) { currBuffer = worldBuffer }
            Events.run(EventType.Trigger.postDraw) { currBuffer = null }
            Events.run(EventType.Trigger.uiDrawBegin) { currBuffer = uiBuffer }
            Events.run(EventType.Trigger.uiDrawEnd) { currBuffer = null }
        }

        activity = true
    }

    private fun beginWorld() {
        if (worldBuffer == null || worldBuffer!!.isBound()) return

        currBuffer = worldBuffer
        worldBuffer!!.resize(Core.graphics.width, Core.graphics.height)
        worldBuffer!!.begin(Color.clear)
    }

    private fun endWorld() {
        currBuffer = null
        worldBuffer!!.end()
        blitBuffer(worldBuffer!!, null)
    }

    private fun beginUI() {
        if (uiBuffer == null || uiBuffer!!.isBound()) return

        currBuffer = uiBuffer
        uiBuffer!!.resize(Core.graphics.width, Core.graphics.height)
        uiBuffer!!.begin(Color.clear)
        blitBuffer(worldBuffer!!, uiBuffer)
    }

    private fun endUI() {
        currBuffer = null
        uiBuffer!!.end()
        blitBuffer(uiBuffer!!, null)
    }

    fun blit(shader: Shader, unit: Int = 0) {
        checkNotNull(currBuffer) { "currently no buffer bound" }

        currBuffer!!.texture.bind(unit)
        Draw.blit(shader)
    }

    private fun blitBuffer(from: FrameBuffer, to: FrameBuffer?) {
        if (Core.gl30 == null) {
            from.blit(HeShaders.baseScreen)
        } else {
            val target = to ?: from.lastBoundFramebuffer
            Gl.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, from.framebufferHandle)
            Gl.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target?.framebufferHandle ?: 0)
            Core.gl30.glBlitFramebuffer(
                0, 0, from.width, from.height,
                0, 0,
                target?.width ?: Core.graphics.width,
                target?.height ?: Core.graphics.height,
                Gl.colorBufferBit, Gl.nearest
            )
        }
    }

    fun getToBuffer(target: FrameBuffer, clear: Boolean) {
        checkNotNull(currBuffer) { "currently no buffer bound" }

        if (clear) target.begin(Color.clear)
        else target.begin()

        Gl.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, currBuffer!!.framebufferHandle)
        Gl.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.framebufferHandle)
        Core.gl30.glBlitFramebuffer(
            0, 0, currBuffer!!.width, currBuffer!!.height,
            0, 0, target.width, target.height,
            Gl.colorBufferBit, Gl.nearest
        )

        target.end()
    }

    // 补一个东西
    private fun FrameBuffer.isBound(): Boolean {
        return (this as? GLFrameBuffer<*>)?.let {
            val last = it.lastBoundFramebuffer
            last === it
        } ?: false
    }
}