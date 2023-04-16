package cum.xiaomao.zerohack.module.modules.misc

import com.mojang.authlib.GameProfile
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.manager.managers.WaypointManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.flooredPosition
import cum.xiaomao.zerohack.util.EntityUtils.isFakeOrSelf
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.math.CoordinateConverter.asString
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.threads.onMainThread
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.BlockPos

internal object LogoutLogger : Module(
    name = "LogoutLogger",
    category = Category.MISC,
    description = "Logs when a player leaves the game"
) {
    private val saveWaypoint by setting("Save Waypoint", true)
    private val print by setting("Print To Chat", true)

    private val loggedPlayers = LinkedHashMap<GameProfile, BlockPos>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        listener<ConnectionEvent.Disconnect> {
            onMainThread {
                loggedPlayers.clear()
            }
        }

        safeParallelListener<TickEvent.Post> {
            for (loadedPlayer in world.playerEntities) {
                if (loadedPlayer !is EntityOtherPlayerMP) continue
                if (loadedPlayer.isFakeOrSelf) continue

                val info = connection.getPlayerInfo(loadedPlayer.gameProfile.id) ?: continue
                loggedPlayers[info.gameProfile] = loadedPlayer.flooredPosition
            }

            if (timer.tickAndReset(1L)) {
                loggedPlayers.entries.removeIf { (profile, pos) ->
                    @Suppress("SENSELESS_COMPARISON")
                    if (connection.getPlayerInfo(profile.id) == null) {
                        if (saveWaypoint) WaypointManager.add(pos, "${profile.name} Logout Spot")
                        if (print) MessageSendUtils.sendNoSpamChatMessage("${profile.name} logged out at ${pos.asString()}")
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
}