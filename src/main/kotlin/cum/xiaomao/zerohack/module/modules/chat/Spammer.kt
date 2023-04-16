package cum.xiaomao.zerohack.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.atTrue
import cum.xiaomao.zerohack.util.extension.synchronized
import cum.xiaomao.zerohack.util.text.MessageDetection
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage
import cum.xiaomao.zerohack.util.threads.defaultScope
import java.io.File
import java.net.URL
import kotlin.random.Random

internal object Spammer : Module(
    name = "Spammer",
    description = "Spams text from a file on a set delay into the chat",
    category = Category.CHAT,
    modulePriority = 100
) {
    private val modeSetting = setting("Order", Mode.RANDOM_ORDER)
    private val delay = setting("Delay", 10, 1..180, 1, description = "Delay between messages, in seconds")
    private val loadRemote = setting("Load From URL", false)
    private val remoteURL = setting("Remote URL", "Unchanged", loadRemote.atTrue())

    private val file = File("${ZeroHackMod.DIRECTORY}/spammer.txt")
    private val spammer = ArrayList<String>().synchronized()
    private val timer = TickTimer(TimeUnit.SECONDS)
    private var currentLine = 0

    private enum class Mode {
        IN_ORDER, RANDOM_ORDER
    }

    private val urlValue
        get() = if (remoteURL.value != "Unchanged") {
            remoteURL.value
        } else {
            MessageSendUtils.sendNoSpamErrorMessage("Change the RemoteURL setting in the ClickGUI!")
            disable()
            null
        }

    init {
        onEnable {
            spammer.clear()

            if (loadRemote.value) {
                val url = urlValue ?: return@onEnable

                defaultScope.launch(Dispatchers.IO) {
                    try {
                        val text = URL(url).readText()
                        spammer.addAll(text.split("\n"))

                        MessageSendUtils.sendNoSpamChatMessage("$chatName Loaded remote spammer messages!")
                    } catch (e: Exception) {
                        MessageSendUtils.sendNoSpamErrorMessage("$chatName Failed loading remote spammer, $e")
                        disable()
                    }
                }

            } else {
                defaultScope.launch(Dispatchers.IO) {
                    if (file.exists()) {
                        try {
                            file.forEachLine { if (it.isNotBlank()) spammer.add(it.trim()) }
                            MessageSendUtils.sendNoSpamChatMessage("$chatName Loaded spammer messages!")
                        } catch (e: Exception) {
                            MessageSendUtils.sendNoSpamErrorMessage("$chatName Failed loading spammer, $e")
                            disable()
                        }
                    } else {
                        file.createNewFile()
                        MessageSendUtils.sendNoSpamErrorMessage("$chatName Spammer file is empty!" +
                            ", please add them in the §7spammer.txt§f under the §7.minecraft/ZeroHack§f directory.")
                        disable()
                    }
                }
            }
        }

        safeListener<TickEvent.Post> {
            if (spammer.isEmpty() || !timer.tickAndReset(delay.value)) return@safeListener

            val message = if (modeSetting.value == Mode.IN_ORDER) getOrdered() else getRandom()
            if (MessageDetection.Command.TROLL_HACK detect message) {
                MessageSendUtils.sendTrollCommand(message)
            } else {
                Spammer.sendServerMessage(message)
            }
        }
    }

    private fun getOrdered(): String {
        currentLine %= spammer.size
        return spammer[currentLine++]
    }

    private fun getRandom(): String {
        val prevLine = currentLine
        // Avoids sending the same message
        while (spammer.size != 1 && currentLine == prevLine) {
            currentLine = Random.nextInt(spammer.size)
        }
        return spammer[currentLine]
    }
}
