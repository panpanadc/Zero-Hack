package cum.xiaomao.zerohack.setting

import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.gui.rgui.Component
import cum.xiaomao.zerohack.module.modules.client.Configurations
import cum.xiaomao.zerohack.setting.configs.AbstractConfig
import cum.xiaomao.zerohack.setting.settings.AbstractSetting
import cum.xiaomao.zerohack.util.extension.rootName
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${ZeroHackMod.DIRECTORY}/config/gui"
) {
    override val file get() = File("$filePath/${Configurations.guiPreset}.json")
    override val backup get() = File("$filePath/${Configurations.guiPreset}.bak")

    override fun addSettingToConfig(owner: Component, setting: AbstractSetting<*>) {
        val groupName = owner.settingGroup.groupName
        if (groupName.isNotEmpty()) {
            getGroupOrPut(groupName).getGroupOrPut(owner.rootName).addSetting(setting)
        }
    }
}