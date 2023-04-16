package cum.xiaomao.zerohack.module.modules.client

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module

internal object CommandSetting : Module(
    name = "CommandSetting",
    category = Category.CLIENT,
    description = "Settings for commands",
    visible = false,
    alwaysEnabled = true
) {
    var prefix by setting("Prefix", ";")
}