package cum.xiaomao.zerohack.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.MovementUtils.isMoving
import cum.xiaomao.zerohack.util.text.MessageDetection
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage
import cum.xiaomao.zerohack.util.threads.defaultScope
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

internal object LoginMessage : Module(
    name = "LoginMessage",
    description = "Sends a given message(s) to public chat on login.",
    category = Category.CHAT,
    visible = false,
    modulePriority = 150
) {
    private val sendAfterMoving by setting("Send After Moving", false, description = "Wait until you have moved after logging in")

    private val file = File("${ZeroHackMod.DIRECTORY}/loginmsg.txt")
    private val loginMessages = CopyOnWriteArrayList<String>()
    private var sent = false
    private var moved = false

    init {
        onEnable {
            if (file.exists()) {
                defaultScope.launch(Dispatchers.IO) {
                    try {
                        file.forEachLine {
                            if (it.isNotBlank()) loginMessages.add(it.trim())
                        }
                        MessageSendUtils.sendNoSpamChatMessage("$chatName Loaded ${loginMessages.size} login messages!")
                    } catch (e: Exception) {
                        MessageSendUtils.sendNoSpamErrorMessage("$chatName Failed loading login messages, $e")
                        disable()
                    }
                }
            } else {
                file.createNewFile()
                MessageSendUtils.sendNoSpamErrorMessage("$chatName Login Messages file not found!" +
                    ", please add them in the §7loginmsg.txt§f under the §7.minecraft/ZeroHack§f directory.")
                disable()
            }
        }

        onDisable {
            loginMessages.clear()
        }

        listener<ConnectionEvent.Disconnect> {
            sent = false
            moved = false
        }

        safeListener<TickEvent.Post> {
            if (!sent && (!sendAfterMoving || moved)) {
                for (message in loginMessages) {
                    if (MessageDetection.Command.TROLL_HACK detect message) {
                        MessageSendUtils.sendTrollCommand(message)
                    } else {
                        LoginMessage.sendServerMessage(message)
                    }
                }

                sent = true
            }

            if (!moved) moved = player.isMoving
        }
    }
}
