package cum.xiaomao.zerohack.module.modules.misc

import cum.xiaomao.zerohack.event.events.player.InteractEvent
import cum.xiaomao.zerohack.event.events.player.PlayerAttackEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.combat.CombatUtils
import cum.xiaomao.zerohack.util.combat.CombatUtils.equipBestWeapon
import cum.xiaomao.zerohack.util.inventory.equipBestTool
import net.minecraft.entity.EntityLivingBase

internal object AutoTool : Module(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Category.MISC
) {
    private val swapWeapon by setting("Switch Weapon", false)
    private val preferWeapon by setting("Prefer", CombatUtils.PreferWeapon.SWORD)

    init {
        safeListener<InteractEvent.Block.LeftClick> {
            if (!player.isCreative && world.getBlockState(it.pos).getBlockHardness(world, it.pos) != -1.0f) {
                equipBestTool(world.getBlockState(it.pos))
            }
        }

        safeListener<PlayerAttackEvent> {
            if (swapWeapon && it.entity is EntityLivingBase) {
                equipBestWeapon(preferWeapon)
            }
        }
    }
}