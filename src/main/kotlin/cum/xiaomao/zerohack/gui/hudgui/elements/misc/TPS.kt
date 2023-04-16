package cum.xiaomao.zerohack.gui.hudgui.elements.misc

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.gui.hudgui.LabelHud
import cum.xiaomao.zerohack.util.TpsCalculator
import cum.xiaomao.zerohack.util.collections.CircularArray
import cum.xiaomao.zerohack.util.collections.CircularArray.Companion.average

internal object TPS : LabelHud(
    name = "TPS",
    category = Category.MISC,
    description = "Server TPS"
) {

    // Buffered TPS readings to add some fluidity to the TPS HUD element
    private val tpsBuffer = CircularArray(120, 20.0f)

    override fun SafeClientEvent.updateText() {
        tpsBuffer.add(TpsCalculator.tickRate)

        displayText.add("%.2f".format(tpsBuffer.average()), primaryColor)
        displayText.add("tps", secondaryColor)
    }

}