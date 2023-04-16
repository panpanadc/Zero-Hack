package cum.xiaomao.zerohack.module.modules.player

import me.zero.mixins.core.network.MixinNetworkManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendNoSpamWarningMessage

/**
 * @see MixinNetworkManager
 */
internal object NoPacketKick : Module(
    name = "NoPacketKick",
    category = Category.PLAYER,
    description = "Suppress network exceptions and prevent getting kicked",
    visible = false
) {
    @JvmStatic
    fun sendWarning(throwable: Throwable) {
        sendNoSpamWarningMessage("$chatName Caught exception - \"$throwable\" check log for more info.")
        throwable.printStackTrace()
    }
}
