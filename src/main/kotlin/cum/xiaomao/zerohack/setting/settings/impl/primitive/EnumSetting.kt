package cum.xiaomao.zerohack.setting.settings.impl.primitive

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import cum.xiaomao.zerohack.setting.settings.MutableNonPrimitive
import cum.xiaomao.zerohack.setting.settings.MutableSetting
import cum.xiaomao.zerohack.util.asStringOrNull
import cum.xiaomao.zerohack.util.extension.next
import java.util.*

class EnumSetting<T : Enum<T>>(
    name: CharSequence,
    value: T,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: T, input: T) -> T = { _, input -> input },
    description: CharSequence = ""
) : MutableSetting<T>(name, value, visibility, consumer, description), MutableNonPrimitive<T> {

    val enumClass: Class<T> = value.declaringJavaClass
    val enumValues: Array<out T> = enumClass.enumConstants

    fun nextValue() {
        value = value.next()
    }

    override fun setValue(valueIn: String) {
        super<MutableSetting>.setValue(valueIn.uppercase(Locale.ROOT).replace(' ', '_'))
    }

    override fun write(): JsonElement = JsonPrimitive(value.name)

    override fun read(jsonElement: JsonElement) {
        jsonElement.asStringOrNull?.let { element ->
            enumValues.firstOrNull { it.name.equals(element, true) }?.let {
                value = it
            }
        }
    }

}