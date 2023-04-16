package cum.xiaomao.zerohack.module.modules.render

import cum.xiaomao.zerohack.event.events.InputEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.mixins.core.render.MixinEntityRenderer
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.module.modules.player.Freecam
import cum.xiaomao.zerohack.util.accessor.unpressKey
import cum.xiaomao.zerohack.util.atTrue

/**
 * @see MixinEntityRenderer
 */
internal object ThirdPersonCamera : Module(
    name = "ThirdPersonCamera",
    category = Category.RENDER,
    description = "Modify 3rd person camera behavior",
    visible = false
) {
    val cameraClip by setting("Camera Clip", true)
    private val whileHolding0 = setting("While Holding", false)
    private val whileHolding by whileHolding0
    private val perspectiveMode by setting("PerspectiveMode Mode", PerspectiveMode.BACK, whileHolding0.atTrue())
    val distance by setting("Camera Distance", 4.0f, 1.0f..10.0f, 0.1f, description = "Camera distance to player")

    @Suppress("unused")
    private enum class PerspectiveMode(val state: Int) {
        BACK(1),
        FRONT(2)
    }

    init {
        listener<InputEvent.Keyboard> {
            if (whileHolding && Freecam.isDisabled && it.key == mc.gameSettings.keyBindTogglePerspective.keyCode) {
                if (it.state) {
                    mc.gameSettings.thirdPersonView = perspectiveMode.state
                } else {
                    mc.gameSettings.thirdPersonView = 0
                }

                mc.gameSettings.keyBindTogglePerspective.unpressKey()
            }
        }
    }
}
