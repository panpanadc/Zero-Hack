package cum.xiaomao.zerohack.module.modules.client

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB

internal object Hud : Module(
    name = "Hud",
    description = "Toggles Hud displaying and settings",
    category = Category.CLIENT,
    visible = false,
    enabledByDefault = true
) {
    val primaryColor by setting("Primary Color", ColorRGB(255, 250, 253), false)
    val secondaryColor by setting("Secondary Color", ColorRGB(255, 135, 230), false)
}