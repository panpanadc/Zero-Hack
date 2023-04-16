package cum.xiaomao.zerohack.setting.settings.impl.other

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import it.unimi.dsi.fastutil.ints.IntArrayList
import cum.xiaomao.zerohack.event.AlwaysListening
import cum.xiaomao.zerohack.event.events.InputEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.setting.settings.ImmutableSetting
import cum.xiaomao.zerohack.setting.settings.NonPrimitive
import cum.xiaomao.zerohack.util.Bind
import cum.xiaomao.zerohack.util.KeyboardUtils
import cum.xiaomao.zerohack.util.interfaces.Helper
import cum.xiaomao.zerohack.util.interfaces.Nameable
import org.lwjgl.input.Keyboard
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class BindSetting(
    name: CharSequence,
    value: Bind,
    visibility: ((() -> Boolean))? = null,
    private val action: ((Boolean) -> Unit)? = null,
    description: CharSequence = ""
) : ImmutableSetting<Bind>(name, value, visibility, { _, input -> input }, description), NonPrimitive<Bind> {

    init {
        binds.add(this)
    }

    override val defaultValue: Bind = Bind(IntAVLTreeSet(value.modifierKeys), value.key)

    override fun resetValue() {
        value.setBind(defaultValue.modifierKeys, defaultValue.key)
    }

    override fun setValue(valueIn: String) {
        if (valueIn.equals("None", ignoreCase = true)) {
            value.clear()
            return
        }

        val splitNames = valueIn.split('+')
        val lastName = splitNames.last()
        val lastKey = if (!lastName.startsWith("Mouse ")) {
            KeyboardUtils.getKey(splitNames.last())
        } else {
            lastName.last().digitToIntOrNull()?.let {
                -it - 1
            } ?: 0
        }

        // Don't clear if the string is fucked
        if (lastKey !in 1..255 && lastKey !in -16..-1) {
            return
        }

        val modifierKeys = IntArrayList(0)
        for (index in 0 until splitNames.size - 1) {
            val name = splitNames[index]
            val key = KeyboardUtils.getKey(name)

            if (key !in 1..255) continue
            modifierKeys.add(key)
        }

        value.setBind(modifierKeys, lastKey)
    }

    override fun write() = JsonPrimitive(value.toString())

    override fun read(jsonElement: JsonElement) {
        setValue(jsonElement.asString ?: "None")
    }

    companion object : AlwaysListening, Helper, Nameable {
        override val name: CharSequence = "BindSetting"
        private val binds = CopyOnWriteArrayList<BindSetting>()

        init {
            listener<InputEvent.Keyboard>(true) {
                if (mc.currentScreen == null && it.key != Keyboard.KEY_NONE && !Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                    for (bind in binds) {
                        if (bind.value.isDown(it.key)) {
                            bind.action?.invoke(it.state)
                        }
                    }
                }
            }

            listener<InputEvent.Mouse>(true) {
                if (mc.currentScreen == null) {
                    for (bind in binds) {
                        if (bind.value.isDown(-it.button - 1)) bind.action?.invoke(it.state)
                    }
                }
            }
        }
    }
}
