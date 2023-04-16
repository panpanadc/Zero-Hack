package cum.xiaomao.zerohack.gui.rgui.component

open class BooleanSlider(
    name: CharSequence,
    description: CharSequence,
    visibility: (() -> Boolean)? = null
) : Slider(name, description, visibility)