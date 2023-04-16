package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting

sealed class InputEvent(val state: Boolean) : Event {
    class Keyboard(val key: Int, state: Boolean) : InputEvent(state), EventPosting by Companion {
        companion object : EventBus()
    }

    class Mouse(val button: Int, state: Boolean) : InputEvent(state), EventPosting by Companion {
        companion object : EventBus()
    }
}
