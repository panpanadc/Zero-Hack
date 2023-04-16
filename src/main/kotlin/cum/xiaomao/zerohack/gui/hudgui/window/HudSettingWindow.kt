package cum.xiaomao.zerohack.gui.hudgui.window

import cum.xiaomao.zerohack.gui.hudgui.AbstractHudElement
import cum.xiaomao.zerohack.gui.rgui.windows.SettingWindow
import cum.xiaomao.zerohack.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: AbstractHudElement,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractHudElement>(hudElement.name, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}