package cum.xiaomao.zerohack.module.modules.chat

import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage
import cum.xiaomao.zerohack.util.text.NoSpamMessage
import cum.xiaomao.zerohack.util.text.format
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.EnumDifficulty

internal object AutoQMain : Module(
    name = "AutoQMain",
    description = "Automatically does '/queue main'",
    category = Category.CHAT,
    visible = false
) {
    private val delay by setting("Delay", 5000, 0..15000, 100)
    private val twoBeeCheck by setting("2B Check", true)
    private val command by setting("Command", "/queue main")

    private val timer = TickTimer()

    init {
        @Suppress("UNNECESSARY_SAFE_CALL")
        safeListener<TickEvent.Pre> {
            if (world.difficulty == EnumDifficulty.PEACEFUL
                && player.dimension == 1
                && (!twoBeeCheck || player.serverBrand?.contains("2b2t") == true)
                && timer.tickAndReset(delay)) {
                sendQueueMain()
            }
        }
    }

    private fun sendQueueMain() {
        NoSpamMessage.sendMessage(this, "$chatName Run ${TextFormatting.GRAY format command} at ${TextFormatting.GRAY format TimeUtils.getTime(TimeUtils.TimeFormat.HHMMSS, TimeUtils.TimeUnit.H24)}")
        sendServerMessage(command)
    }
}
