package cum.xiaomao.zerohack.util.text

import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.util.interfaces.Helper
import cum.xiaomao.zerohack.util.threads.onMainThread
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting

object NoSpamMessage : Helper {
    fun sendMessage(message: String) {
        send(coloredName(TextFormatting.LIGHT_PURPLE, message), message.hashCode())
    }

    fun sendMessage(identifier: Any, message: String) {
        send(coloredName(TextFormatting.LIGHT_PURPLE, message), identifier.hashCode())
    }

    fun sendWarning(message: String) {
        send(coloredName(TextFormatting.GOLD, message), message.hashCode())
    }

    fun sendWarning(identifier: Any, message: String) {
        send(coloredName(TextFormatting.GOLD, message), identifier.hashCode())
    }

    fun sendError(message: String) {
        send(coloredName(TextFormatting.DARK_RED, message), message.hashCode())
    }

    fun sendError(identifier: Any, message: String) {
        send(coloredName(TextFormatting.DARK_RED, message), identifier.hashCode())
    }

    private fun send(message: String, id: Int) {
        onMainThread {
            mc.ingameGUI?.chatGUI?.printChatMessageWithOptionalDeletion(TextComponentString(message), id)
        }
    }

    private fun coloredName(textFormatting: TextFormatting, message: String): String {
        return "${TextFormatting.GRAY}[$textFormatting${ZeroHackMod.NAME}${TextFormatting.GRAY}]${TextFormatting.RESET} $message"
    }
}