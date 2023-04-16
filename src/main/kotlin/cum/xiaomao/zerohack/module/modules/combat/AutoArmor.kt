package cum.xiaomao.zerohack.module.modules.combat

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.manager.managers.HotbarManager.spoofHotbar
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.inventory.InventoryTask
import cum.xiaomao.zerohack.util.inventory.executedOrTrue
import cum.xiaomao.zerohack.util.inventory.inventoryTask
import cum.xiaomao.zerohack.util.inventory.operation.action
import cum.xiaomao.zerohack.util.inventory.operation.pickUp
import cum.xiaomao.zerohack.util.inventory.operation.quickMove
import cum.xiaomao.zerohack.util.inventory.operation.swapWith
import cum.xiaomao.zerohack.util.inventory.slot.*
import cum.xiaomao.zerohack.util.items.durability
import cum.xiaomao.zerohack.util.items.getEnchantmentLevel
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import kotlin.math.max
import kotlin.math.roundToInt

internal object AutoArmor : Module(
    name = "AutoArmor",
    category = Category.COMBAT,
    description = "Automatically equips armour",
    modulePriority = 500
) {
    private val antiGlitchArmor by setting("Anti Glitch Armor", true)
    private val stackedArmor by setting("Stacked Armor", false)
    private val swapSlot by setting("Swap Slot", 9, 1..9, 1, { stackedArmor })
    private val blastProtectionLeggings by setting("Blast Protection Leggings", true, description = "Prefer leggings with blast protection enchantment")
    private val armorSaver by setting("Armor Saver", false, { !stackedArmor }, description = "Swaps out armor at low durability")
    private val duraThreshold by setting("Durability Threshold", 10, 1..50, 1, { !stackedArmor && armorSaver })
    private val delay by setting("Delay", 2, 1..10, 1)

    private val moveTimer = TickTimer(TimeUnit.TICKS)
    private var lastTask: InventoryTask? = null

    init {
        safeParallelListener<TickEvent.Post> {
            if (AutoMend.isActive() || player.openContainer != player.inventoryContainer || !lastTask.executedOrTrue) return@safeParallelListener

            val armorSlots = player.armorSlots
            val isElytraOn = player.chestSlot.stack.item == Items.ELYTRA

            // store slots and values of best armor pieces, initialize with currently equipped armor
            // Pair<Slot, Value>
            val bestArmors = Array(4) {
                armorSlots[it] to getArmorValue(armorSlots[it].stack)
            }

            // search inventory for better armor
            findBestArmor(player.hotbarSlots, bestArmors, isElytraOn)
            findBestArmor(player.craftingSlots, bestArmors, isElytraOn)
            findBestArmor(player.inventorySlots, bestArmors, isElytraOn)

            // equip better armor
            if (equipArmor(armorSlots, bestArmors)) {
                moveTimer.reset()
            } else if (antiGlitchArmor && moveTimer.tick(10) && player.totalArmorValue != player.armorSlots.sumOf { getRawArmorValue(it.stack) }) {
                inventoryTask {
                    for (slot in player.armorSlots) {
                        pickUp(slot)
                        pickUp(slot)
                    }
                    delay(1, TimeUnit.TICKS)
                    postDelay(delay, TimeUnit.TICKS)
                }
                moveTimer.reset()
            }
        }
    }

    private fun getRawArmorValue(itemStack: ItemStack): Int {
        val item = itemStack.item
        return if (item !is ItemArmor) 0 else item.damageReduceAmount
    }

    private fun findBestArmor(slots: List<Slot>, bestArmors: Array<Pair<Slot, Float>>, isElytraOn: Boolean) {
        for (slot in slots) {
            val itemStack = slot.stack
            val item = itemStack.item
            if (item !is ItemArmor) continue

            val armorType = item.armorType
            if (armorType == EntityEquipmentSlot.CHEST && isElytraOn) continue // Skip if item is chestplate and we have elytra equipped

            val armorValue = getArmorValue(itemStack)
            val armorIndex = 3 - armorType.index

            if (armorValue > bestArmors[armorIndex].second) bestArmors[armorIndex] = slot to armorValue
        }
    }

    private fun getArmorValue(itemStack: ItemStack): Float {
        val item = itemStack.item
        return if (item !is ItemArmor) {
            -1.0f
        } else {
            val value = item.damageReduceAmount * getProtectionModifier(itemStack)

            // Less weight for armor with low dura so it gets swaps out
            if (!stackedArmor && armorSaver && itemStack.isItemStackDamageable && itemStack.duraPercentage < duraThreshold) value * 0.1f
            else value
        }
    }

    private val ItemStack.duraPercentage: Int
        get() = (this.durability / this.maxDamage.toFloat() * 100.0f).roundToInt()

    private fun getProtectionModifier(itemStack: ItemStack): Float {
        val item = itemStack.item
        val protectionLevel = itemStack.getEnchantmentLevel(Enchantments.PROTECTION)

        val level = if (blastProtectionLeggings && item is ItemArmor && item.armorType == EntityEquipmentSlot.LEGS) {
            // Blast protection reduces 2x damage
            max(itemStack.getEnchantmentLevel(Enchantments.BLAST_PROTECTION) * 2, protectionLevel)
        } else {
            protectionLevel
        }

        return 1.0f + 0.04f * level
    }

    private fun SafeClientEvent.equipArmor(armorSlots: List<Slot>, bestArmors: Array<Pair<Slot, Float>>): Boolean {
        for ((index, pair) in bestArmors.withIndex()) {
            val slotFrom = pair.first
            if (slotFrom.slotNumber in 5..8) continue // Skip if we didn't find a better armor in inventory

            val slotTo = armorSlots[index]

            lastTask = if (stackedArmor && slotFrom.stack.count > 1) {
                moveStackedArmor(slotFrom, slotTo)
            } else {
                if (pair.first.slotNumber in 1..4) {
                    moveFromCraftingSlot(pair.first, slotTo)
                } else {
                    moveFromInventory(pair.first, slotTo)
                }
            }

            return true // Don't move more than one at once
        }

        return false
    }

    private fun SafeClientEvent.moveStackedArmor(slotFrom: Slot, slotTo: Slot): InventoryTask {
        return slotFrom.toHotbarSlotOrNull()?.let {
            if (slotTo.hasStack) {
                inventoryTask {
                    pickUp(slotTo)
                    action {
                        spoofHotbar(it) {
                            connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                    }
                    pickUp(slotFrom)
                }
            } else {
                inventoryTask {
                    action {
                        spoofHotbar(it) {
                            connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                    }
                }
            }
        } ?: run {
            inventoryTask {
                swapWith(slotFrom, player.getHotbarSlot(swapSlot - 1))
                postDelay(delay, TimeUnit.TICKS)
            }
        }
    }

    private fun moveFromCraftingSlot(slotFrom: Slot, slotTo: Slot): InventoryTask {
        return if (!slotTo.hasStack) {
            inventoryTask {
                pickUp(slotFrom) // Pick up the new one
                pickUp(slotTo) // Put the new one into armor slot
                postDelay(delay, TimeUnit.TICKS)
            }
        } else {
            inventoryTask {
                pickUp(slotFrom) // Pick up the new one
                pickUp(slotTo) // Put the new one into armor slot
                pickUp(slotFrom) // Put the old one into the empty slot
                postDelay(delay, TimeUnit.TICKS)
            }
        }
    }

    private fun SafeClientEvent.moveFromInventory(slotFrom: Slot, slotTo: Slot): InventoryTask {
        return when {
            !slotTo.hasStack -> {
                inventoryTask {
                    quickMove(slotFrom) // Move the new one into armor slot)
                    postDelay(delay, TimeUnit.TICKS)
                }
            }

            player.inventorySlots.hasEmpty() -> {
                inventoryTask {
                    quickMove(slotTo) // Move out the old one
                    quickMove(slotFrom) // Put the old one into the empty slot
                    postDelay(delay, TimeUnit.TICKS)
                }
            }

            else -> {
                inventoryTask {
                    pickUp(slotFrom) // Pick up the new one
                    pickUp(slotTo) // Put the new one into armor slot
                    postDelay(delay, TimeUnit.TICKS)
                }
            }
        }
    }
}