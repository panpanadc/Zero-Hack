package cum.xiaomao.zerohack.gui.hudgui.elements.misc

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.gui.hudgui.LabelHud
import cum.xiaomao.zerohack.util.InfoCalculator

internal object Ping : LabelHud(
    name = "Ping",
    category = Category.MISC,
    description = "Delay between client and server"
) {

    override fun SafeClientEvent.updateText() {
        displayText.add(InfoCalculator.ping().toString(), primaryColor)
        displayText.add("ms", secondaryColor)
    }

}