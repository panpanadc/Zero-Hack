package cum.xiaomao.zerohack.module.modules.misc

import me.zero.mixins.core.world.MixinWorld
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module

/**
 * @see MixinWorld.getThunderStrengthHead
 * @see MixinWorld.getRainStrengthHead
 */
internal object AntiWeather : Module(
    name = "AntiWeather",
    description = "Removes rain and thunder from your world",
    category = Category.MISC
)
