package cum.xiaomao.zerohack.gui.hudgui.elements.player

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.gui.hudgui.LabelHud
import cum.xiaomao.zerohack.manager.managers.TimerManager

internal object TimerSpeed : LabelHud(
    name = "TimerSpeed",
    category = Category.PLAYER,
    description = "Client side timer speed"
) {
    override fun SafeClientEvent.updateText() {
        displayText.add("%.2f".format(50.0f / TimerManager.tickLength), primaryColor)
        displayText.add("x", secondaryColor)
    }
}