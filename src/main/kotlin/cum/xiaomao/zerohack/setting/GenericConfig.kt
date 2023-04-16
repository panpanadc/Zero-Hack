package cum.xiaomao.zerohack.setting

import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.setting.configs.NameableConfig

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${ZeroHackMod.DIRECTORY}/config/"
)