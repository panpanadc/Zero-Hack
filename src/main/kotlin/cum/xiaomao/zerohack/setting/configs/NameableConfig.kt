package cum.xiaomao.zerohack.setting.configs

import cum.xiaomao.zerohack.setting.settings.AbstractSetting
import cum.xiaomao.zerohack.util.extension.rootName
import cum.xiaomao.zerohack.util.interfaces.Nameable

open class NameableConfig<T : Nameable>(
    name: String,
    filePath: String
) : AbstractConfig<T>(name, filePath) {

    override fun addSettingToConfig(owner: T, setting: AbstractSetting<*>) {
        getGroupOrPut(owner.rootName).addSetting(setting)
    }

    open fun getSettings(nameable: Nameable) = getGroup(nameable.rootName)?.getSettings() ?: emptyList()

}
