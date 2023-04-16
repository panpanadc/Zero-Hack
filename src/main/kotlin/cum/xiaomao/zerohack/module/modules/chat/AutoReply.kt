package cum.xiaomao.zerohack.module.modules.chat

import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.atTrue
import cum.xiaomao.zerohack.util.text.MessageDetection
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage
import net.minecraft.network.play.server.SPacketChat

internal object AutoReply : Module(
    name = "AutoReply",
    description = "Automatically reply to direct messages",
    category = Category.CHAT
) {
    private val customMessage = setting("Custom Message", false)
    private val customText = setting("Custom Text", "unchanged", customMessage.atTrue())

    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat || MessageDetection.Direct.RECEIVE detect it.packet.chatComponent.unformattedText) return@listener
            if (customMessage.value) {
                sendServerMessage("/r " + customText.value)
            } else {
                sendServerMessage("/r I just automatically replied, thanks to Zero Hack's AutoReply module!")
            }
        }

        safeParallelListener<TickEvent.Post> {
            if (timer.tickAndReset(5L) && customMessage.value && customText.value.equals("unchanged", true)) {
                MessageSendUtils.sendNoSpamWarningMessage("$chatName Warning: In order to use the custom $name, please change the CustomText setting in ClickGUI")
            }
        }
    }
}