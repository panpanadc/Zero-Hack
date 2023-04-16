package cum.xiaomao.zerohack.module.modules.combat

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.events.player.OnUpdateWalkingPlayerEvent
import cum.xiaomao.zerohack.event.events.render.Render3DEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.gui.hudgui.elements.client.Notification
import cum.xiaomao.zerohack.manager.managers.CombatManager
import cum.xiaomao.zerohack.manager.managers.PlayerPacketManager.sendPlayerPacket
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.Bind
import cum.xiaomao.zerohack.util.EntityUtils.eyePosition
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.combat.CalcContext
import cum.xiaomao.zerohack.util.combat.CrystalDamage
import cum.xiaomao.zerohack.util.combat.CrystalUtils
import cum.xiaomao.zerohack.util.combat.CrystalUtils.hasValidSpaceForCrystal
import cum.xiaomao.zerohack.util.graphics.ESPRenderer
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB
import cum.xiaomao.zerohack.util.inventory.slot.allSlots
import cum.xiaomao.zerohack.util.inventory.slot.firstBlock
import cum.xiaomao.zerohack.util.inventory.slot.hasItem
import cum.xiaomao.zerohack.util.inventory.slot.hotbarSlots
import cum.xiaomao.zerohack.util.math.RotationUtils.getRotationTo
import cum.xiaomao.zerohack.util.math.VectorUtils
import cum.xiaomao.zerohack.util.math.vector.distanceTo
import cum.xiaomao.zerohack.util.math.vector.toVec3d
import cum.xiaomao.zerohack.util.threads.defaultScope
import cum.xiaomao.zerohack.util.threads.onMainThreadSafeSuspend
import cum.xiaomao.zerohack.util.threads.runSafe
import cum.xiaomao.zerohack.util.world.*
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.PriorityQueue
import kotlin.math.max

@CombatManager.CombatModule
internal object CrystalBasePlace : Module(
    name = "CrystalBasePlace",
    description = "Places obby for placing crystal on",
    category = Category.COMBAT,
    modulePriority = 90
) {
    private val manualPlaceBind by setting("Bind Manual Place", Bind(), {
        runSafe {
            if (isEnabled && CombatManager.isOnTopPriority(CrystalBasePlace) && !CombatSetting.pause) {
                prePlace(minDamageIncManual)
            }
        }
    })
    private val minDamage by setting("Min Damage", 8.0f, 0.0f..20.0f, 0.5f)
    private val maxSelfDamage by setting("Max Self Damage", 8.0f, 0.0f..20.0f, 0.5f)
    private val minDamageIncManual by setting("Min Damage Inc Inactive", 2.0f, 0.0f..20.0f, 0.25f)
    private val minDamageIncInactive by setting("Min Damage Inc Inactive", 4.0f, 0.0f..20.0f, 0.25f)
    private val minDamageIncActive by setting("Min Damage Inc Active", 8.0f, 0.0f..20.0f, 0.25f)
    private val range by setting("Range", 4.0f, 0.0f..8.0f, 0.5f)
    private val delay by setting("Delay", 20, 0..50, 5)

    private val timer = TickTimer(TimeUnit.TICKS)
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }
    private var inactiveTicks = 0
    private var rotationTo: Vec3d? = null

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks < 4
    }

    override fun getHudInfo(): String {
        return if (inactiveTicks <= 10) {
            "Active"
        } else {
            ""
        }
    }

    init {
        onDisable {
            inactiveTicks = 0
        }

        listener<Render3DEvent> {
            val clear = inactiveTicks >= 30
            renderer.render(clear)
        }

        safeListener<OnUpdateWalkingPlayerEvent.Pre> {
            if (isActive()) {
                rotationTo?.let { hitVec ->
                    sendPlayerPacket {
                        rotate(getRotationTo(hitVec))
                    }
                }
            } else {
                rotationTo = null
            }
        }

        safeParallelListener<TickEvent.Post> {
            inactiveTicks++

            if (CombatManager.isOnTopPriority(CrystalBasePlace)
                && !CombatSetting.pause
                && (TrollAura.isEnabled || AutoCrystalPlus.isEnabled)
                && player.allSlots.hasItem(Items.END_CRYSTAL)) {
                prePlace(if (checkInactivity()) minDamageIncInactive else minDamageIncActive)
            }
        }
    }

    private fun checkInactivity(): Boolean {
        return if (AutoCrystalPlus.isEnabled) {
            System.currentTimeMillis() - AutoCrystalPlus.lastActiveTime > 500L
        } else {
            TrollAura.isEnabled && TrollAura.inactiveTicks > 10
        }
    }

    private fun SafeClientEvent.prePlace(minDamageInc: Float) {
        if (rotationTo != null || !timer.tick(delay)) return

        val slot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) ?: return
        val eyePos = player.eyePosition
        val posList = VectorUtils.getBlockPosInSphere(eyePos, range)
            .filter { hasValidSpaceForCrystal(it) }
            .filter { world.isPlaceable(it) }
            .toList()

        defaultScope.launch {
            val placeInfo = getPlaceInfo(eyePos, posList, minDamageInc)

            if (placeInfo != null) {
                rotationTo = placeInfo.hitVec
                renderer.replaceAll(mutableListOf(ESPRenderer.Info(AxisAlignedBB(placeInfo.placedPos), ColorRGB(255, 255, 255))))
                inactiveTicks = 0
                timer.reset()

                delay(50)
                onMainThreadSafeSuspend {
                    placeBlock(placeInfo, slot)
                    Notification.send(CrystalBasePlace, "$chatName Obsidian placed")
                }
            } else {
                timer.reset(-max(delay - 1, 0) * 50L)
            }
        }
    }

    private fun SafeClientEvent.getPlaceInfo(eyePos: Vec3d, posList: List<BlockPos>, minDamageInc: Float): PlaceInfo? {
        val contextSelf = CombatManager.contextSelf ?: return null
        val contextTarget = CombatManager.contextTarget ?: return null

        val mutableBlockPos = BlockPos.MutableBlockPos()
        val cacheList = PriorityQueue<CrystalDamage>(compareByDescending { it.targetDamage })
        val maxCurrentDamage = CombatManager.placeMap.entries
            .filter { eyePos.distanceTo(it.key) < range }
            .maxOfOrNull { it.value.targetDamage } ?: 0.0f

        for (pos in posList) {
            // Neighbor blocks check
            if (!hasNeighbor(pos)) continue

            // Collide check
            val crystalPos = pos.toVec3d(0.5, 1.0, 0.5)
            if (!contextSelf.checkColliding(crystalPos)) continue
            if (!contextTarget.checkColliding(crystalPos)) continue

            // Damage check
            val crystalDamage = calculateDamage(contextSelf, contextTarget, eyePos, crystalPos, pos, mutableBlockPos)
            if (!checkDamage(crystalDamage, maxCurrentDamage, minDamageInc)) continue

            cacheList.add(crystalDamage)
        }

        var current = cacheList.poll()
        while (current != null) {
            val neighbor = getNeighbor(current.blockPos, 1)
            if (neighbor != null) {
                return neighbor
            }
            current = cacheList.poll()
        }

        return null
    }

    private fun calculateDamage(
        contextSelf: CalcContext,
        contextTarget: CalcContext?,
        eyePos: Vec3d,
        crystalPos: Vec3d,
        pos: BlockPos,
        mutableBlockPos: BlockPos.MutableBlockPos
    ): CrystalDamage {
        val function: World.(BlockPos, IBlockState) -> FastRayTraceAction = { rayTracePos, blockState ->
            when {
                rayTracePos == pos -> {
                    FastRayTraceAction.HIT
                }
                blockState.block != Blocks.AIR && CrystalUtils.isResistant(blockState) -> {
                    FastRayTraceAction.CALC
                }
                else -> {
                    FastRayTraceAction.SKIP
                }
            }
        }

        val selfDamage = max(contextSelf.calcDamage(crystalPos, true, mutableBlockPos, function), contextSelf.calcDamage(crystalPos, false, mutableBlockPos, function))
        val targetDamage = contextTarget?.calcDamage(crystalPos, true, mutableBlockPos, function) ?: 0.0f

        return CrystalDamage(crystalPos, pos, selfDamage, targetDamage, eyePos.distanceTo(crystalPos), contextSelf.currentPos.distanceTo(crystalPos))
    }

    private fun checkDamage(crystalDamage: CrystalDamage, maxCurrentDamage: Float, minDamageInc: Float) =
        crystalDamage.selfDamage <= maxSelfDamage
            && (crystalDamage.targetDamage >= minDamage
            && (crystalDamage.targetDamage - maxCurrentDamage >= minDamageInc))
}