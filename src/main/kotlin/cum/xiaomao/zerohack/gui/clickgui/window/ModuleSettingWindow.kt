package cum.xiaomao.zerohack.gui.clickgui.window

import cum.xiaomao.zerohack.gui.rgui.windows.SettingWindow
import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.setting.settings.AbstractSetting

class ModuleSettingWindow(
    module: AbstractModule,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractModule>(module.name, module, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.fullSettingList.filter { it.name != "Enabled" && it.name != "Clicks" }
    }

}