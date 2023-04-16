package cum.xiaomao.zerohack.util.graphics

import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.event.AlwaysListening
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.events.render.ResolutionUpdateEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.util.Wrapper
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.client.shader.ShaderLinkHelper
import net.minecraft.util.ResourceLocation

class ShaderHelper(shaderIn: ResourceLocation) : AlwaysListening {
    private val mc = Wrapper.minecraft

    val shader: ShaderGroup? =
        if (!OpenGlHelper.shadersSupported) {
            ZeroHackMod.logger.warn("Shaders are unsupported by OpenGL!")
            null
        } else {
            try {
                ShaderLinkHelper.setNewStaticShaderLinkHelper()

                ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, shaderIn).also {
                    it.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                }
            } catch (e: Exception) {
                ZeroHackMod.logger.warn("Failed to load shaders")
                e.printStackTrace()

                null
            }
        }

    private var frameBuffersInitialized = false

    init {
        listener<TickEvent.Post> {
            if (!frameBuffersInitialized) {
                shader?.createBindFramebuffers(mc.displayWidth, mc.displayHeight)

                frameBuffersInitialized = true
            }
        }

        listener<ResolutionUpdateEvent> {
            shader?.createBindFramebuffers(it.width, it.height) // this will not run if on Intel GPU or unsupported Shaders
        }
    }

    fun getFrameBuffer(name: String) = shader?.getFramebufferRaw(name)
}