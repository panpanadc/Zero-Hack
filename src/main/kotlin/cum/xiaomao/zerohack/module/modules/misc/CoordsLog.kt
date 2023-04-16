package cum.xiaomao.zerohack.module.modules.misc

import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.WaypointManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.InfoCalculator
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.math.CoordinateConverter.asString
import cum.xiaomao.zerohack.util.math.vector.toBlockPos
import cum.xiaomao.zerohack.util.text.MessageSendUtils

internal object CoordsLog : Module(
    name = "CoordsLog",
    description = "Automatically logs your coords, based on actions",
    category = Category.MISC
) {
    private val saveOnDeath = setting("Save On Death", true)
    private val autoLog = setting("Automatically Log", false)
    private val delay = setting("Delay", 15, 1..60, 1)

    private var previousCoord: String? = null
    private var savedDeath = false
    private var timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.Post> {
            if (autoLog.value && timer.tickAndReset(delay.value.toLong())) {
                val currentCoord = player.positionVector.toBlockPos().asString()

                if (currentCoord != previousCoord) {
                    WaypointManager.add("autoLogger")
                    previousCoord = currentCoord
                }
            }

            if (saveOnDeath.value) {
                savedDeath = if (player.isDead || player.health <= 0.0f) {
                    if (!savedDeath) {
                        val deathPoint = WaypointManager.add("Death - " + InfoCalculator.getServerType()).pos
                        MessageSendUtils.sendNoSpamChatMessage("You died at ${deathPoint.x}, ${deathPoint.y}, ${deathPoint.z}")
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

}
