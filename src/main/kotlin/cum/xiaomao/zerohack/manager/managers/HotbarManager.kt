package cum.xiaomao.zerohack.manager.managers

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.Manager
import cum.xiaomao.zerohack.util.accessor.currentPlayerItem
import cum.xiaomao.zerohack.util.inventory.slot.HotbarSlot
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange

@Suppress("NOTHING_TO_INLINE")
object HotbarManager : Manager() {
    var serverSideHotbar = 0; private set
    var swapTime = 0L; private set

    val EntityPlayerSP.serverSideItem: ItemStack
        get() = inventory.mainInventory[serverSideHotbar]

    init {
        safeListener<PacketEvent.Send>(Int.MIN_VALUE) {
            if (it.cancelled || it.packet !is CPacketHeldItemChange) return@safeListener

            synchronized(playerController) {
                serverSideHotbar = it.packet.slotId
                swapTime = System.currentTimeMillis()
            }
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(playerController) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int, crossinline block: () -> Unit) {
        synchronized(playerController) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.spoofHotbarBypass(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(playerController) {
            val swap = slot.hotbarSlot != serverSideHotbar
            if (swap) playerController.pickItem(slot.hotbarSlot)
            block.invoke()
            if (swap) playerController.pickItem(slot.hotbarSlot)
        }
    }

    inline fun SafeClientEvent.spoofHotbarBypass(slot: Int, crossinline block: () -> Unit) {
        synchronized(playerController) {
            val swap = slot != serverSideHotbar
            if (swap) playerController.pickItem(slot)
            block.invoke()
            if (swap) playerController.pickItem(slot)
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot) {
        return spoofHotbar(slot.hotbarSlot)
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int) {
        if (serverSideHotbar != slot) {
            connection.sendPacket(CPacketHeldItemChange(slot))
        }
    }

    inline fun SafeClientEvent.resetHotbar() {
        val slot = playerController.currentPlayerItem
        if (serverSideHotbar != slot) {
            spoofHotbar(slot)
        }
    }
}