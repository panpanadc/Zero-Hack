package cum.xiaomao.zerohack.util.graphics

import cum.xiaomao.zerohack.event.AlwaysListening
import cum.xiaomao.zerohack.module.modules.client.GuiSetting
import cum.xiaomao.zerohack.util.extension.fastCeil
import cum.xiaomao.zerohack.util.interfaces.Helper

object Resolution : AlwaysListening, Helper {
    val widthI
        get() = mc.displayWidth

    val heightI
        get() = mc.displayHeight

    val heightF
        get() = mc.displayHeight.toFloat()

    val widthF
        get() = mc.displayWidth.toFloat()

    val trollWidthF
        get() = widthF / GuiSetting.scaleFactorFloat

    val trollHeightF
        get() = heightF / GuiSetting.scaleFactorFloat

    val trollWidthI
        get() = trollWidthF.fastCeil()

    val trollHeightI
        get() = trollHeightF.fastCeil()
}