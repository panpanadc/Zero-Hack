package cum.xiaomao.zerohack.module.modules.movement

import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import net.minecraft.init.MobEffects

internal object AntiLevitation : Module(
    name = "AntiLevitation",
    description = "Removes levitation potion effect",
    category = Category.MOVEMENT
) {
    init {
        safeListener<TickEvent.Pre> {
            player.removeActivePotionEffect(MobEffects.LEVITATION)
        }
    }
}