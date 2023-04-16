package cum.xiaomao.zerohack.module.modules.combat

import cum.xiaomao.zerohack.event.events.render.Render3DEvent
import cum.xiaomao.zerohack.event.events.render.RenderEntityEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.viewEntity
import cum.xiaomao.zerohack.util.accessor.renderPosX
import cum.xiaomao.zerohack.util.accessor.renderPosY
import cum.xiaomao.zerohack.util.accessor.renderPosZ
import cum.xiaomao.zerohack.util.graphics.GlStateUtils
import cum.xiaomao.zerohack.util.graphics.RenderUtils3D
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB
import cum.xiaomao.zerohack.util.graphics.color.setGLColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityEnderCrystal

internal object CrystalChams : Module(
    name = "CrystalChams",
    description = "Renders chams for End Crystals",
    category = Category.COMBAT
) {
    val scale by setting("Scale", 1.0f, 0.1f..4.0f, 0.1f)
    val spinSpeed by setting("Spin Speed", 1.0f, 0.0f..4.0f, 0.1f)
    val floatSpeed by setting("Float Speed", 1.0f, 0.0f..4.0f, 0.1f)
    val filled by setting("Filled", true)
    val filledDepth by setting("Filled Depth", true, ::filled)
    private val filledColor by setting("Filled Color", ColorRGB(133, 255, 200, 63), true, ::filled)
    val outline by setting("Outline", true)
    val outlineDepth by setting("Outline Depth", false, ::outline)
    private val outlineColor by setting("Outline Color", ColorRGB(133, 255, 200, 200), true, ::outline)
    private val width by setting("Width", 2.0f, 0.25f..4.0f, 0.25f, ::outline)
    private val range by setting("Range", 16.0f, 0.0f..16.0f, 0.5f)

    init {
        safeListener<RenderEntityEvent.All.Pre> {
            if (it.entity is EntityEnderCrystal && viewEntity.getDistanceSq(it.entity) <= range * range) {
                it.cancel()
            }
        }

        safeListener<Render3DEvent> {
            val partialTicks = RenderUtils3D.partialTicks
            val rangeSq = range * range
            val renderer = mc.renderManager.getEntityClassRenderObject<EntityEnderCrystal>(EntityEnderCrystal::class.java)
                ?: return@safeListener

            GlStateUtils.alpha(true)
            GlStateManager.glLineWidth(width)
            GlStateUtils.useProgram(0)

            for (crystal in world.loadedEntityList) {
                if (crystal !is EntityEnderCrystal) continue
                if (viewEntity.getDistanceSq(crystal) > rangeSq) continue

                renderer.doRender(
                    crystal,
                    crystal.posX - mc.renderManager.renderPosX,
                    crystal.posY - mc.renderManager.renderPosY,
                    crystal.posZ - mc.renderManager.renderPosZ,
                    0.0f,
                    partialTicks
                )
            }

            GlStateUtils.depth(false)
            GlStateUtils.alpha(false)
        }
    }

    @JvmStatic
    fun setFilledColor() {
        filledColor.setGLColor()
    }

    @JvmStatic
    fun setOutlineColor() {
        outlineColor.setGLColor()
    }
}