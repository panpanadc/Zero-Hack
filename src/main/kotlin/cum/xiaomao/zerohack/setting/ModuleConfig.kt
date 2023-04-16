package cum.xiaomao.zerohack.setting

import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.module.modules.client.Configurations
import cum.xiaomao.zerohack.setting.configs.NameableConfig
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
    "modules",
    "${ZeroHackMod.DIRECTORY}/config/modules",
) {
    override val file: File get() = File("$filePath/${Configurations.modulePreset}.json")
    override val backup get() = File("$filePath/${Configurations.modulePreset}.bak")
}