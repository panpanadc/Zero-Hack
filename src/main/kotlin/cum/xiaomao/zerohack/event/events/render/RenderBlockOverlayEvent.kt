package cum.xiaomao.zerohack.event.events.render

import cum.xiaomao.zerohack.event.*
import net.minecraftforge.client.event.RenderBlockOverlayEvent

class RenderBlockOverlayEvent(override val event: RenderBlockOverlayEvent) : Event, ICancellable, WrappedForgeEvent, EventPosting by Companion {
    val type: RenderBlockOverlayEvent.OverlayType by event::overlayType

    companion object : EventBus()
}