package cum.xiaomao.zerohack.module.modules.misc

import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.manager.managers.WaypointManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.isFakeOrSelf
import cum.xiaomao.zerohack.util.atTrue
import cum.xiaomao.zerohack.util.math.MathUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import net.minecraft.util.math.BlockPos

internal object TeleportLogger : Module(
    name = "TeleportLogger",
    category = Category.MISC,
    description = "Logs when a player teleports somewhere"
) {
    private val saveToWaypoints = setting("Save To Waypoints", true)
    private val removeInRange = setting("Remove In Range", true)
    private val printAdd = setting("Print Add", true)
    private val printRemove = setting("Print Remove", true, removeInRange.atTrue())
    private val minimumDistance = setting("Minimum Distance", 512, 128..2048, 128)

    private val teleportedPlayers = HashMap<String, BlockPos>()

    init {
        safeParallelListener<TickEvent.Post> {
            for (otherPlayer in world.playerEntities) {
                if (otherPlayer.isFakeOrSelf) continue

                /* 8 chunk render distance * 16 */
                if (removeInRange.value && otherPlayer.getDistance(player) < 128) {
                    teleportedPlayers.remove(otherPlayer.name)?.let {
                        val removed = WaypointManager.remove(it)

                        if (printRemove.value) {
                            if (removed) {
                                MessageSendUtils.sendNoSpamChatMessage("$chatName Removed ${otherPlayer.name}, they are now ${MathUtils.round(otherPlayer.getDistance(player), 1)} blocks away")
                            } else {
                                MessageSendUtils.sendNoSpamErrorMessage("$chatName Error removing ${otherPlayer.name} from coords, their position wasn't saved anymore")
                            }
                        }
                    }

                    continue
                }

                if (otherPlayer.getDistance(player) < minimumDistance.value || teleportedPlayers.containsKey(otherPlayer.name)) {
                    continue
                }

                val coords = logCoordinates(otherPlayer.position, "${otherPlayer.name} Teleport Spot")
                teleportedPlayers[otherPlayer.name] = coords
                if (printAdd.value) MessageSendUtils.sendNoSpamChatMessage("$chatName ${otherPlayer.name} teleported, ${getSaveText()} ${coords.x}, ${coords.y}, ${coords.z}")
            }
        }
    }

    private fun logCoordinates(coordinate: BlockPos, name: String): BlockPos {
        return if (saveToWaypoints.value) WaypointManager.add(coordinate, name).pos
        else coordinate
    }

    private fun getSaveText(): String {
        return if (saveToWaypoints.value) "saved their coordinates at"
        else "their coordinates are"
    }
}
