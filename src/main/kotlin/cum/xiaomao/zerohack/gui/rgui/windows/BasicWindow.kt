package cum.xiaomao.zerohack.gui.rgui.windows

import cum.xiaomao.zerohack.module.modules.client.GuiSetting
import cum.xiaomao.zerohack.setting.GuiConfig
import cum.xiaomao.zerohack.setting.configs.AbstractConfig
import cum.xiaomao.zerohack.util.graphics.RenderUtils2D
import cum.xiaomao.zerohack.util.graphics.shaders.WindowBlurShader
import cum.xiaomao.zerohack.util.interfaces.Nameable
import cum.xiaomao.zerohack.util.math.vector.Vec2f

/**
 * Window with rectangle rendering
 */
open class BasicWindow(
    name: CharSequence,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : CleanWindow(name, posX, posY, width, height, settingGroup, config) {
    override fun onRender(absolutePos: Vec2f) {
        super.onRender(absolutePos)
        if (GuiSetting.windowBlur) {
            WindowBlurShader.render(renderWidth, renderHeight)
        }
        if (GuiSetting.titleBar) {
            RenderUtils2D.drawRectFilled(0.0f, draggableHeight, renderWidth, renderHeight, GuiSetting.backGround)
        } else {
            RenderUtils2D.drawRectFilled(0.0f, 0.0f, renderWidth, renderHeight, GuiSetting.backGround)
        }
        if (GuiSetting.windowOutline) {
            RenderUtils2D.drawRectOutline(0.0f, 0.0f, renderWidth, renderHeight, 1.0f, GuiSetting.primary.alpha(255))
        }
        if (GuiSetting.titleBar) {
            RenderUtils2D.drawRectFilled(0.0f, 0.0f, renderWidth, draggableHeight, GuiSetting.primary)
        }
    }

}