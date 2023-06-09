package cum.xiaomao.zerohack.module.modules.render

import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.render.Render3DEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.flooredPosition
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.extension.sq
import cum.xiaomao.zerohack.util.graphics.GlStateUtils
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB
import cum.xiaomao.zerohack.util.graphics.esp.StaticBoxRenderer
import cum.xiaomao.zerohack.util.graphics.mask.SideMask
import cum.xiaomao.zerohack.util.math.vector.distanceSqTo
import cum.xiaomao.zerohack.util.threads.defaultScope
import cum.xiaomao.zerohack.util.world.isAir
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11.*

internal object VoidESP : Module(
    name = "VoidESP",
    description = "Highlights holes leading to the void",
    category = Category.RENDER
) {
    private val filled by setting("Filled", true)
    private val outline by setting("Outline", true)
    private val color by setting("Color", ColorRGB(148, 161, 255), false)
    private val filledAlpha by setting("Filled Alpha", 127, 0..255, 1)
    private val outlineAlpha by setting("Outline Alpha", 255, 0..255, 1)
    private val renderMode by setting("Mode", Mode.BLOCK_HOLE)
    private val range by setting("Range", 8, 4..32, 1)

    @Suppress("UNUSED")
    private enum class Mode {
        BLOCK_HOLE, BLOCK_VOID, FLAT
    }

    private val renderer = StaticBoxRenderer()
    private val timer = TickTimer()

    override fun getHudInfo(): String {
        return renderer.size.toString()
    }

    init {
        safeListener<Render3DEvent> {
            if (timer.tickAndReset(133L)) { // Avoid running this on a tick
                updateRenderer()
            }

            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
            GlStateManager.glLineWidth(2.0f)
            GlStateUtils.depth(false)
            if (renderMode == Mode.FLAT) GlStateUtils.cull(false)

            val filledAlpha = if (filled) filledAlpha else 0
            val outlineAlpha = if (outline) outlineAlpha else 0

            renderer.render(filledAlpha, outlineAlpha)

            GlStateUtils.cull(true)
            GlStateUtils.depth(true)
            GlStateManager.glLineWidth(1.0f)
        }
    }

    private fun SafeClientEvent.updateRenderer() {
        val side = if (renderMode != Mode.FLAT) SideMask.ALL else SideMask.DOWN
        val playerPos = player.flooredPosition
        val rangeSq = range.sq
        val pos = BlockPos.MutableBlockPos()

        defaultScope.launch {
            renderer.update {
                for (x in playerPos.x - range..playerPos.x + range) {
                    for (z in playerPos.z - range..playerPos.z + range) {
                        if (playerPos.distanceSqTo(x, 0, z) > rangeSq) continue
                        if (!isVoid(x, z)) continue

                        if (renderMode == Mode.BLOCK_VOID) pos.setPos(x, -1, z) else pos.setPos(x, 0, z)
                        putBox(AxisAlignedBB(pos), color, side)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.isVoid(x: Int, z: Int): Boolean {
        return world.isAir(x, 0, z)
            && world.isAir(x, 1, z)
            && world.isAir(x, 2, z)
    }
}
