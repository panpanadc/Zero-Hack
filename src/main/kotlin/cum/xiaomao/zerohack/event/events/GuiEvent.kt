package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.client.gui.GuiScreen

sealed class GuiEvent : Event {
    abstract val screen: GuiScreen?

    class Closed(override val screen: GuiScreen) : GuiEvent(), EventPosting by Companion {
        companion object : EventBus()
    }

    class Displayed(override var screen: GuiScreen?) : GuiEvent(), EventPosting by Companion {
        companion object : EventBus()
    }
}