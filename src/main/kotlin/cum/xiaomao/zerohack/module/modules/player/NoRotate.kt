package cum.xiaomao.zerohack.module.modules.player

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module

internal object NoRotate : Module(
    name = "NoRotate",
    alias = arrayOf("AntiForceLook"),
    category = Category.PLAYER,
    description = "Stops server packets from turning your head"
)