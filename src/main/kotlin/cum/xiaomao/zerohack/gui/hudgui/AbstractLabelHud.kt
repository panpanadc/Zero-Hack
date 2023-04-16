package cum.xiaomao.zerohack.gui.hudgui

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.setting.configs.AbstractConfig
import cum.xiaomao.zerohack.util.graphics.font.TextComponent
import cum.xiaomao.zerohack.util.interfaces.Nameable
import cum.xiaomao.zerohack.util.math.vector.Vec2d

abstract class AbstractLabelHud(
    name: String,
    alias: Array<String>,
    category: Category,
    description: String,
    alwaysListening: Boolean,
    enabledByDefault: Boolean,
    config: AbstractConfig<out Nameable>,
) : AbstractHudElement(name, alias, category, description, alwaysListening, enabledByDefault, config) {
    override val hudWidth: Float get() = displayText.getWidth() + 2.0f
    override val hudHeight: Float get() = displayText.getHeight(2)

    protected val displayText = TextComponent()

    init {
        safeParallelListener<TickEvent.Post> {
            displayText.clear()
            updateText()
        }
    }

    protected abstract fun SafeClientEvent.updateText()

    override fun renderHud() {
        super.renderHud()

        val textPosX = width * dockingH.multiplier / scale - dockingH.offset
        val textPosY = height * dockingV.multiplier / scale

        displayText.draw(
            Vec2d(textPosX.toDouble(), textPosY.toDouble()),
            horizontalAlign = dockingH,
            verticalAlign = dockingV
        )
    }

}
