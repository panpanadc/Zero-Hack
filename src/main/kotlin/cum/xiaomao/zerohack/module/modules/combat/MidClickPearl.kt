package cum.xiaomao.zerohack.module.modules.combat

import cum.xiaomao.zerohack.event.events.InputEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.HotbarManager.spoofHotbar
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.inventory.slot.firstItem
import cum.xiaomao.zerohack.util.inventory.slot.hotbarSlots
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendNoSpamChatMessage
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult.Type

internal object MidClickPearl : Module(
    name = "MidClickPearl",
    category = Category.COMBAT,
    description = "Throws a pearl automatically when you middle click in air"
) {
    init {
        safeListener<InputEvent.Mouse> {
            if (it.state || it.button != 2) return@safeListener

            val objectMouseOver = mc.objectMouseOver
            if (objectMouseOver == null || objectMouseOver.typeOfHit != Type.BLOCK) {
                val pearlSlot = player.hotbarSlots.firstItem(Items.ENDER_PEARL)

                if (pearlSlot != null) {
                    spoofHotbar(pearlSlot) {
                        connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    }
                } else {
                    sendNoSpamChatMessage("No Ender Pearl was found in hotbar!")
                }
            }
        }
    }
}