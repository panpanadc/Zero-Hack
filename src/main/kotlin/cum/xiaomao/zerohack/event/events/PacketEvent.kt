package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.*
import net.minecraft.network.Packet

sealed class PacketEvent(val packet: Packet<*>) : Event {
    class Receive(packet: Packet<*>) : PacketEvent(packet), ICancellable by Cancellable(), EventPosting by Companion {
        companion object : EventBus()
    }

    class PostReceive(packet: Packet<*>) : PacketEvent(packet), EventPosting by Companion {
        companion object : EventBus()
    }

    class Send(packet: Packet<*>) : PacketEvent(packet), ICancellable by Cancellable(), EventPosting by Companion {
        companion object : EventBus()
    }

    class PostSend(packet: Packet<*>) : PacketEvent(packet), EventPosting by Companion {
        companion object : EventBus()
    }
}