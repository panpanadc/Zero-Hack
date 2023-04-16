package cum.xiaomao.zerohack.module.modules.misc

import cum.xiaomao.zerohack.event.events.GuiEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.WaypointManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.InfoCalculator
import cum.xiaomao.zerohack.util.math.CoordinateConverter.asString
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import net.minecraft.client.gui.GuiGameOver

internal object AutoRespawn : Module(
    name = "AutoRespawn",
    description = "Automatically respawn after dying",
    category = Category.MISC
) {
    private val respawn by setting("Respawn", true)
    private val deathCoords by setting("Save Death Coords", true)
    private val antiGlitchScreen by setting("Anti Glitch Screen", true)

    init {
        safeListener<GuiEvent.Displayed> {
            if (it.screen !is GuiGameOver) return@safeListener

            if (deathCoords && player.health <= 0.0f) {
                WaypointManager.add("Death - " + InfoCalculator.getServerType())
                MessageSendUtils.sendChatMessage("You died at ${player.position.asString()}")
            }

            if (respawn || antiGlitchScreen && player.health > 0.0f) {
                player.respawnPlayer()
                it.screen = null
            }
        }
    }
}