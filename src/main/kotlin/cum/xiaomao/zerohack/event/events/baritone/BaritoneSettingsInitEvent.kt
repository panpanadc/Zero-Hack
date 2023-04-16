package cum.xiaomao.zerohack.event.events.baritone

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting

/**
 * Posted at the return of when Baritone's Settings are initialized.
 */
object BaritoneSettingsInitEvent : Event, EventPosting by EventBus()