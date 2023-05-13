package cum.xiaomao.zerohack.module.modules.chat

import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.manager.managers.MessageManager.newMessageModifier
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.text.MessageDetection
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import kotlin.math.min

internal object ChatSuffix : Module(
    name = "ChatSuffix",
    category = Category.CHAT,
    description = "Add a custom ending to your message!",
    visible = false,
    modulePriority = 200
) {
    private val textMode by setting("Message", TextMode.NAME)
    private val decoMode by setting("Separator", DecoMode.NONE)
    private val commands by setting("Commands", false)
    private val spammer by setting("Spammer", false)
    private val customText by setting("Custom Text", "Default")

    private enum class DecoMode {
        SEPARATOR, CLASSIC, NONE
    }

    private enum class TextMode {
        NAME, CUSTOM
    }

    private val timer = TickTimer(TimeUnit.SECONDS)
    private val modifier = newMessageModifier(
        filter = {
            (commands || MessageDetection.Command.ANY detectNot it.packet.message)
                && (spammer || it.source !is Spammer)
        },
        modifier = {
            val message = it.packet.message + getFull()
            message.substring(0, min(256, message.length))
        }
    )

    init {
        onEnable {
            modifier.enable()
        }

        onDisable {
            modifier.disable()
        }
    }

    private fun getText() = when (textMode) {
        TextMode.NAME -> "Z E R O ＨＡＣＫ"
        TextMode.CUSTOM -> customText
    }

    private fun getFull() = when (decoMode) {
        DecoMode.NONE -> " " + getText()
        DecoMode.CLASSIC -> " \u00ab " + getText() + " \u00bb"
        DecoMode.SEPARATOR -> " | " + getText()
    }

    init {
        safeParallelListener<TickEvent.Post> {
            if (timer.tickAndReset(5L) && textMode == TextMode.CUSTOM && customText.equals("Default", ignoreCase = true)) {
                MessageSendUtils.sendNoSpamWarningMessage("$chatName Warning: In order to use the custom $name, please change the CustomText setting in ClickGUI")
            }
        }
    }


}
