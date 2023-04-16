package cum.xiaomao.zerohack.gui.rgui.windows

import cum.xiaomao.zerohack.gui.rgui.WindowComponent
import cum.xiaomao.zerohack.setting.GuiConfig
import cum.xiaomao.zerohack.setting.configs.AbstractConfig
import cum.xiaomao.zerohack.util.interfaces.Nameable

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: CharSequence,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : WindowComponent(name, posX, posY, width, height, settingGroup, config)