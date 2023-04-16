package cum.xiaomao.zerohack.gui.hudgui

import cum.xiaomao.zerohack.gui.rgui.Component
import cum.xiaomao.zerohack.setting.GuiConfig
import cum.xiaomao.zerohack.setting.settings.SettingRegister

internal abstract class HudElement(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false,
) : AbstractHudElement(name, alias, category, description, alwaysListening, enabledByDefault, GuiConfig),
    SettingRegister<Component> by GuiConfig