package cum.xiaomao.zerohack.module.modules.player

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.player.OnUpdateWalkingPlayerEvent
import cum.xiaomao.zerohack.event.events.render.Render3DEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.HotbarManager.resetHotbar
import cum.xiaomao.zerohack.manager.managers.HotbarManager.spoofHotbar
import cum.xiaomao.zerohack.manager.managers.PlayerPacketManager
import cum.xiaomao.zerohack.manager.managers.PlayerPacketManager.sendPlayerPacket
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.graphics.ESPRenderer
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB
import cum.xiaomao.zerohack.util.inventory.slot.HotbarSlot
import cum.xiaomao.zerohack.util.inventory.slot.firstItem
import cum.xiaomao.zerohack.util.inventory.slot.hotbarSlots
import cum.xiaomao.zerohack.util.items.blockBlacklist
import cum.xiaomao.zerohack.util.math.RotationUtils.legitYaw
import cum.xiaomao.zerohack.util.math.RotationUtils.yaw
import cum.xiaomao.zerohack.util.math.vector.Vec2f
import cum.xiaomao.zerohack.util.threads.runSafe
import cum.xiaomao.zerohack.util.world.*
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos

@Suppress("NOTHING_TO_INLINE")
internal object HypixelScaffold : Module(
    name = "HypixelScaffold",
    category = Category.PLAYER,
    description = "Scaffold bypass for hypixel",
    modulePriority = 5000
) {
    private val desyncPlacement by setting("Desync Placement", true)
    private val delay by setting("Delay", 2, 1..20, 1)
    private val placeTimeout by setting("Place Timeout", 4, 1..20, 1)
    private val rotationTimeout by setting("Rotation Timeout", 10, 1..20, 1)

    private val renderer = ESPRenderer()

    private var lastPos: BlockPos? = null
    private var placeInfo: PlaceInfo? = null
    private var ticks = 0
    private var lastRotation: Vec2f? = null
    private var switchBack = false

    override fun isActive(): Boolean {
        return isEnabled && placeInfo != null
    }

    init {
        onDisable {
            if (switchBack) {
                runSafe {
                    resetHotbar()
                }
            }

            lastPos = null
            placeInfo = null
            ticks = 0
            lastRotation = null
            switchBack = false
        }

        listener<PacketEvent.PostReceive> {
            if (it.packet is SPacketBlockChange && it.packet.blockPosition == lastPos && !it.packet.getBlockState().isReplaceable) {
                lastPos = null
            }
        }

        listener<Render3DEvent> {
            renderer.render(false)
        }

        safeListener<OnUpdateWalkingPlayerEvent.Pre> {
            ticks++
            renderer.clear()

            val slot = player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>()
            val newPlaceInfo = slot?.let {
                getPlaceInfo()
            }

            if (newPlaceInfo != null) {
                spoofHotbar(slot)
                switchBack = true
                renderer.add(newPlaceInfo.placedPos, ColorRGB(32, 255, 32))
                lastRotation = Vec2f(player.legitYaw(newPlaceInfo.side.opposite.yaw), 82.5f)
            } else if (ticks > 3 && switchBack) {
                resetHotbar()
                switchBack = false
            }

            lastRotation?.let {
                if (newPlaceInfo != null || ticks <= rotationTimeout) {
                    sendPlayerPacket {
                        rotate(it)
                    }
                }
            }

            placeInfo = newPlaceInfo
        }

        safeListener<OnUpdateWalkingPlayerEvent.Post> {
            if (PlayerPacketManager.prevRotation.y == 82.5f && PlayerPacketManager.rotation.y == 82.5f) {
                placeInfo?.let { placeInfo ->
                    player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>()?.let {
                        doPlace(placeInfo, it)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.getPlaceInfo(): PlaceInfo? {
        val posDown = BlockPos(player.posX, player.posY - 1.0, player.posZ)
        return if (world.getBlockState(posDown).isReplaceable) {
            getNeighbor(posDown, 1)
                ?: getNeighbor(posDown, 2, sides = EnumFacing.HORIZONTALS)
        } else {
            null
        }
    }

    private fun SafeClientEvent.doPlace(placeInfo: PlaceInfo, slot: HotbarSlot) {
        if (ticks < delay) return
        if (!desyncPlacement && lastPos == placeInfo.placedPos && ticks < placeTimeout) return

        if (!world.isPlaceable(placeInfo.placedPos)) return


        val itemStack = slot.stack
        val block = (itemStack.item as? ItemBlock?)?.block ?: return
        val metaData = itemStack.metadata
        val blockState = block.getStateForPlacement(world, placeInfo.pos, placeInfo.side, placeInfo.hitVecOffset.x, placeInfo.hitVecOffset.y, placeInfo.hitVecOffset.z, metaData, player, EnumHand.MAIN_HAND)
        val soundType = blockState.block.getSoundType(blockState, world, placeInfo.pos, player)

        val sneak = !player.isSneaking && blockBlacklist.contains(world.getBlock(placeInfo.pos))
        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

        spoofHotbar(slot)
        switchBack = true
        connection.sendPacket(placeInfo.toPlacePacket(EnumHand.MAIN_HAND))
        player.swingArm(EnumHand.MAIN_HAND)

        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))

        world.playSound(player, placeInfo.pos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
        if (desyncPlacement) world.setBlockState(placeInfo.placedPos, blockState)

        ticks = 0
        lastPos = placeInfo.placedPos
    }
}