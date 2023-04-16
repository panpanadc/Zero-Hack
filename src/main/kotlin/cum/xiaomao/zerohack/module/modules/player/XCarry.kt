package cum.xiaomao.zerohack.module.modules.player

import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.accessor.windowID
import cum.xiaomao.zerohack.util.inventory.slot.craftingSlots
import cum.xiaomao.zerohack.util.inventory.slot.hasAnyItem
import net.minecraft.network.play.client.CPacketCloseWindow

internal object XCarry : Module(
    name = "XCarry",
    category = Category.PLAYER,
    description = "Store items in crafting slots"
) {
    init {
        safeListener<PacketEvent.Send> {
            if (it.packet is CPacketCloseWindow && it.packet.windowID == 0 && (!player.inventory.itemStack.isEmpty || player.craftingSlots.hasAnyItem())) {
                it.cancel()
            }
        }
    }
}