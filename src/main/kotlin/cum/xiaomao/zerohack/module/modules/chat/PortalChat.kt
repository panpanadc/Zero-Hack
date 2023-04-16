package cum.xiaomao.zerohack.module.modules.chat

import me.zero.mixins.core.player.MixinEntityPlayerSP
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module

/**
 * @see MixinEntityPlayerSP
 */
internal object PortalChat : Module(
    name = "PortalChat",
    category = Category.CHAT,
    description = "Allows you to open GUIs in portals",
    visible = false
)
