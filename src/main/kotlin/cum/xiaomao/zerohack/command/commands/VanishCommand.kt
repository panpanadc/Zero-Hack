package cum.xiaomao.zerohack.command.commands

import cum.xiaomao.zerohack.command.ClientCommand
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendNoSpamChatMessage
import cum.xiaomao.zerohack.util.text.formatValue
import net.minecraft.entity.Entity

object VanishCommand : ClientCommand(
    name = "vanish",
    description = "Allows you to vanish using an entity."
) {
    private var vehicle: Entity? = null

    init {
        executeSafe {
            if (player.ridingEntity != null && vehicle == null) {
                vehicle = player.ridingEntity?.also {
                    player.dismountRidingEntity()
                    world.removeEntityFromWorld(it.entityId)
                    sendNoSpamChatMessage("Vehicle " + formatValue(it.name) + " removed")
                }
            } else {
                vehicle?.let {
                    it.isDead = false
                    world.addEntityToWorld(it.entityId, it)
                    player.startRiding(it, true)
                    sendNoSpamChatMessage("Vehicle " + formatValue(it.name) + " created")
                    vehicle = null
                } ?: sendNoSpamChatMessage("Not riding any vehicles")
            }
        }
    }
}