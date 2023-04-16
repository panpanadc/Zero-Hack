package cum.xiaomao.zerohack.module.modules.misc

import cum.xiaomao.zerohack.command.CommandManager
import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.events.baritone.BaritoneCommandEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.BaritoneUtils
import cum.xiaomao.zerohack.util.accessor.sendClickBlockToController
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.formatValue
import cum.xiaomao.zerohack.util.threads.runSafe

internal object AutoMine : Module(
    name = "AutoMine",
    description = "Automatically mines chosen ores",
    category = Category.MISC
) {

    private val manual by setting("Manual", false)
    private val iron = setting("Iron", false)
    private val diamond = setting("Diamond", true)
    private val gold = setting("Gold", false)
    private val coal = setting("Coal", false)
    private val log = setting("Logs", false)

    init {
        onEnable {
            runSafe {
                run()
            } ?: disable()
        }

        onDisable {
            BaritoneUtils.cancelEverything()
        }
    }

    private fun SafeClientEvent.run() {
        if (isDisabled || manual) return

        val blocks = ArrayList<String>()

        if (iron.value) blocks.add("iron_ore")
        if (diamond.value) blocks.add("diamond_ore")
        if (gold.value) blocks.add("gold_ore")
        if (coal.value) blocks.add("coal_ore")
        if (log.value) {
            blocks.add("log")
            blocks.add("log2")
        }

        if (blocks.isEmpty()) {
            MessageSendUtils.sendBaritoneMessage("Error: you have to choose at least one thing to mine. " +
                "To mine custom blocks run the ${formatValue("${CommandManager.prefix}b mine block")} command")
            BaritoneUtils.cancelEverything()
            return
        }

        MessageSendUtils.sendBaritoneCommand("mine", *blocks.toTypedArray())
    }

    init {
        safeListener<TickEvent.Pre> {
            if (manual) {
                mc.sendClickBlockToController(true)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        iron.listeners.add { runSafe { run() } }
        diamond.listeners.add { runSafe { run() } }
        gold.listeners.add { runSafe { run() } }
        coal.listeners.add { runSafe { run() } }
        log.listeners.add { runSafe { run() } }
    }
}
