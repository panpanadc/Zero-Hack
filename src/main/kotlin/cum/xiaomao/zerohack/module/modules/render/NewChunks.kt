package cum.xiaomao.zerohack.module.modules.render

import cum.xiaomao.zerohack.event.events.render.Render3DEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.ChunkManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.getInterpolatedPos
import cum.xiaomao.zerohack.util.graphics.GlStateUtils
import cum.xiaomao.zerohack.util.graphics.RenderUtils3D
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB
import cum.xiaomao.zerohack.util.math.vector.distanceSqToBlock
import cum.xiaomao.zerohack.util.threads.onMainThread
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.ChunkPos
import org.lwjgl.opengl.GL11.GL_LINES

internal object NewChunks : Module(
    name = "NewChunks",
    description = "Highlights newly generated chunks",
    category = Category.RENDER
) {
    private val relative by setting("Relative", false, description = "Renders the chunks at relative Y level to player")
    private val yOffset by setting("Y Offset", 0, -256..256, 4, fineStep = 1, description = "Render offset in Y axis")
    private val color by setting("Color", ColorRGB(255, 64, 64, 200), description = "Highlighting color")
    private val thickness by setting("Thickness", 1.5f, 0.1f..4.0f, 0.1f, description = "Thickness of the highlighting square")
    private val renderRange by setting("Render Range", 512, 64..2048, 32, description = "Maximum range for chunks to be highlighted")

    @Suppress("unused")
    private enum class RemoveMode {
        NEVER, UNLOAD, MAX_NUMBER
    }

    private val chunks = LinkedHashSet<ChunkPos>()

    override fun getHudInfo(): String {
        return chunks.size.toString()
    }

    init {
        onDisable {
            onMainThread {
                chunks.clear()
            }
        }

        safeListener<Render3DEvent> {
            val y = yOffset.toDouble() + if (relative) getInterpolatedPos(player, RenderUtils3D.partialTicks).y else 0.0

            GlStateManager.glLineWidth(thickness)
            GlStateUtils.depth(false)

            val rangeSq = renderRange * renderRange

            for (chunkPos in ChunkManager.newChunks) {
                if (player.distanceSqToBlock(chunkPos) > rangeSq) continue

                val xStart = chunkPos.xStart.toDouble()
                val xEnd = chunkPos.xEnd + 1.0
                val zStart = chunkPos.zStart.toDouble()
                val zEnd = chunkPos.zEnd + 1.0

                RenderUtils3D.putVertex(xStart, y, zStart, color)
                RenderUtils3D.putVertex(xEnd, y, zStart, color)
                RenderUtils3D.putVertex(xEnd, y, zStart, color)
                RenderUtils3D.putVertex(xEnd, y, zEnd, color)
                RenderUtils3D.putVertex(xEnd, y, zEnd, color)
                RenderUtils3D.putVertex(xStart, y, zEnd, color)
                RenderUtils3D.putVertex(xStart, y, zEnd, color)
                RenderUtils3D.putVertex(xStart, y, zStart, color)
            }

            RenderUtils3D.draw(GL_LINES)

            GlStateManager.glLineWidth(1.0f)
            GlStateUtils.depth(true)
        }
    }
}