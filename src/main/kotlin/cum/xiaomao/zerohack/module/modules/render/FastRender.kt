package cum.xiaomao.zerohack.module.modules.render

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.accessor.renderPosX
import cum.xiaomao.zerohack.util.accessor.renderPosY
import cum.xiaomao.zerohack.util.accessor.renderPosZ
import cum.xiaomao.zerohack.util.graphics.fastrender.tileentity.*
import cum.xiaomao.zerohack.util.threads.runSafe
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL30.glBindVertexArray

internal object FastRender : Module(
    name = "FastRender",
    description = "Fps boost",
    category = Category.RENDER,
    enabledByDefault = true
) {
    private val renderEntryMap = HashMap<Class<out TileEntity>, RenderEntry<out TileEntity>>()

    init {
        register {
            BedRenderBuilder(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        }
        register {
            ChestRenderBuilder(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        }
        register {
            EnderChestRenderBuilder(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        }
        register {
            ShulkerBoxRenderBuilder(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        }
    }

    private inline fun <reified T : TileEntity> register(noinline newBuilder: () -> ITileEntityRenderBuilder<T>) {
        renderEntryMap[T::class.java] = RenderEntry(newBuilder)
    }

    init {
        listener<TickEvent.Post> {
            renderEntryMap.values.forEach {
                it.clear()
            }

            runSafe {
                mc.world.loadedTileEntityList.forEach { tileEntity ->
                    renderEntryMap[tileEntity.javaClass]?.let {
                        @Suppress("UNCHECKED_CAST")
                        (it as RenderEntry<TileEntity>?)?.add(tileEntity)
                    }
                }
            }

            updateRenderers()
        }
    }

    @JvmStatic
    fun renderTileEntities() {
        AbstractTileEntityRenderBuilder.Shader.updateShaders()
        GlStateManager.disableCull()

        renderEntryMap.values.forEach {
            it.render()
        }

        glBindVertexArray(0)
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    fun updateRenderers() {
        runBlocking {
            val actor = actor<Pair<RenderEntry<*>, ITileEntityRenderBuilder<*>>> {
                for ((entry, builder) in channel) {
                    entry.updateRenderer(builder.upload())
                }
            }

            coroutineScope {
                for (entry in renderEntryMap.values) {
                    entry.update(this, actor)
                }
            }

            actor.close()
        }
    }

    private class RenderEntry<T : TileEntity>(
        private val newBuilder: () -> ITileEntityRenderBuilder<T>
    ) {
        init {
            newBuilder.invoke()
        }

        private var renderer: ITileEntityRenderBuilder.Renderer? = null
        private val tileEntities = ArrayList<T>()
        private var dirty = false

        fun clear() {
            if (tileEntities.isNotEmpty()) {
                tileEntities.clear()
                dirty = true
            }
        }

        fun add(tileEntity: T) {
            tileEntities.add(tileEntity)
            dirty = true
        }

        fun remove(tileEntity: T) {
            dirty = tileEntities.remove(tileEntity) || dirty
        }

        fun update(scope: CoroutineScope, actor: SendChannel<Pair<RenderEntry<*>, ITileEntityRenderBuilder<*>>>) {
            if (!dirty) return

            if (tileEntities.isEmpty()) {
                updateRenderer(null)
            } else {
                scope.launch(Dispatchers.Default) {
                    val builder = newBuilder.invoke()

                    tileEntities.forEach {
                        builder.add(it)
                    }

                    builder.build()

                    actor.send(Pair(this@RenderEntry, builder))
                }
            }
        }

        fun updateRenderer(newRenderer: ITileEntityRenderBuilder.Renderer?) {
            renderer?.destroy()
            renderer = newRenderer
            dirty = false
        }

        fun render() {
            renderer?.render(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
        }
    }

/*    class TileEntityRenderFrameBuffer : FrameBuffer() {
        private val depthTextureID = glGenTextures()

        override fun allocateFrameBuffer(width: Int, height: Int, sampleLevel: Int) {
            super.allocateFrameBuffer(width, height, sampleLevel)

            if (sampleLevel == 0) {
                GlStateManager.bindTexture(depthTextureID)
                glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null as ByteBuffer?)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE)
                GlStateManager.bindTexture(0)

                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTextureID, 0)
            } else {
                glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, depthTextureID)
                glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, AntiAlias.sampleLevel, GL_DEPTH_COMPONENT24, width, height, true)
//                glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
//                glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
//                glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
//                glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
//                glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)
//                glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_COMPARE_MODE, GL_NONE)
                glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0)

                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D_MULTISAMPLE, depthTextureID, 0)
            }
        }

        fun bindDepthTexture() {
            if (multiTexturing) glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, depthTextureID)
            else GlStateManager.bindTexture(depthTextureID)
        }

        fun unbindDepthTexture() {
            if (multiTexturing) glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0)
            else GlStateManager.bindTexture(0)
        }

        override fun destroy() {
            super.destroy()
            glDeleteTextures(depthTextureID)
        }
    }*/
}
