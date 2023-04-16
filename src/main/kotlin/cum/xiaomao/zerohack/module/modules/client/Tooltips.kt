package cum.xiaomao.zerohack.module.modules.client

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module

internal object Tooltips : Module(
    name = "Tooltips",
    description = "Displays handy module descriptions in the GUI",
    category = Category.CLIENT,
    visible = false,
    enabledByDefault = true
)
