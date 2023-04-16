package cum.xiaomao.zerohack.module.modules.misc

import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import net.minecraft.entity.passive.AbstractChestHorse
import net.minecraft.network.play.client.CPacketUseEntity

internal object MountBypass : Module(
    name = "MountBypass",
    category = Category.MISC,
    description = "Might allow you to mount chested animals on servers that block it"
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity || it.packet.action != CPacketUseEntity.Action.INTERACT_AT) return@listener
            if (it.packet.getEntityFromWorld(mc.world) is AbstractChestHorse) it.cancel()
        }
    }
}