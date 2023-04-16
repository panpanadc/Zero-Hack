package cum.xiaomao.zerohack.gui.hudgui.component

import cum.xiaomao.zerohack.gui.hudgui.AbstractHudElement
import cum.xiaomao.zerohack.gui.hudgui.ZeroHudGui
import cum.xiaomao.zerohack.gui.rgui.component.BooleanSlider
import cum.xiaomao.zerohack.util.math.vector.Vec2f

class HudButton(val hudElement: AbstractHudElement) : BooleanSlider(hudElement.name, hudElement.description) {
    override val progress: Float
        get() = if (hudElement.visible) 1.0f else 0.0f

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        if (buttonId == 0) hudElement.visible = !hudElement.visible
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) ZeroHudGui.displaySettingWindow(hudElement)
    }
}