package cum.xiaomao.zerohack.module.modules.render

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.setting.settings.impl.collection.CollectionSetting
import cum.xiaomao.zerohack.util.BOOLEAN_SUPPLIER_FALSE
import cum.xiaomao.zerohack.util.threads.onMainThread
import net.minecraft.block.state.IBlockState

internal object Xray : Module(
    name = "Xray",
    description = "Lets you see through blocks",
    category = Category.RENDER
) {
    private val defaultVisibleList = linkedSetOf("minecraft:diamond_ore", "minecraft:iron_ore", "minecraft:gold_ore", "minecraft:portal", "minecraft:cobblestone")

    val blockList = setting(CollectionSetting("Visible List", defaultVisibleList, BOOLEAN_SUPPLIER_FALSE))

    @JvmStatic
    fun shouldReplace(state: IBlockState): Boolean {
        return isEnabled && !blockList.contains(state.block.registryName.toString())
    }

    init {
        onToggle {
            onMainThread {
                mc.renderGlobal?.loadRenderers()
            }
        }

        blockList.editListeners.add {
            onMainThread {
                mc.renderGlobal?.loadRenderers()
            }
        }
    }
}
