package cum.xiaomao.zerohack.module.modules.render

import cum.xiaomao.zerohack.event.events.render.ResolutionUpdateEvent
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.threads.onMainThread

internal object AntiAlias : Module(
    name = "AntiAlias",
    description = "Enables Antialias",
    category = Category.RENDER
) {
    private val sampleLevel0 = setting("SSAA Level", 1.0f, 1.0f..2.0f, 0.05f)

    val sampleLevel get() = if (isEnabled) sampleLevel0.value else 1.0f

    init {
        onToggle {
            onMainThread {
                mc.resize(mc.displayWidth, mc.displayHeight)
                ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight).post()
            }
        }

        sampleLevel0.listeners.add {
            onMainThread {
                mc.resize(mc.displayWidth, mc.displayHeight)
                ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight).post()
            }
        }
    }
}
