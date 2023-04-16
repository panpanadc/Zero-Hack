package cum.xiaomao.zerohack.util.accessor

import me.zero.mixins.accessor.entity.AccessorEntityLivingBase
import net.minecraft.entity.EntityLivingBase

fun EntityLivingBase.onItemUseFinish() {
    (this as AccessorEntityLivingBase).trollInvokeOnItemUseFinish()
}