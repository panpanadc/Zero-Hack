package cum.xiaomao.zerohack.module.modules.combat

import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.EntityEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage

internal object AutoKit : Module(
    name = "AutoKit",
    description = "Do /kit automatically",
    category = Category.COMBAT
) {
    private val kitName by setting("Kit Name", "")

    private var shouldSend = false
    private val timer = TickTimer()

    init {
        listener<ConnectionEvent.Connect> {
            shouldSend = true
            timer.reset(3000L)
        }

        safeListener<EntityEvent.Death> {
            if (it.entity == player) {
                shouldSend = true
                timer.reset(1500L)
            }
        }

        safeListener<TickEvent.Post> {
            if (player.isDead) {
                shouldSend = true
            } else if (shouldSend && timer.tick(0)) {
                val name = kitName
                if (name.isNotBlank()) {
                    MessageSendUtils.sendServerMessage("/kit $name")
                }
                shouldSend = false
            }
        }
    }
}
