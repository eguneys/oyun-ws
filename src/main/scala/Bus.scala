package oyun.ws

import akka.actor.typed.ActorRef

import ipc.ClientMsg

object Bus {

  type Chan = String

  private val impl = new util.EventBus[ClientMsg, Chan, ActorRef[ClientMsg]](
    initialCapacity = 65535,
    publish = (actor, event) => actor ! event
  )

  def subscribe = impl.subscribe _
  def unsubscribe = impl.unsubscribe _

  def publish(chan: Chan, event: ClientMsg): Unit =
    impl.publish(chan, event)

  def publish(chan: ChanSelect, event: ClientMsg): Unit =
    impl.publish(chan(channel), event)


  def publish(msg: Msg): Unit =
    impl.publish(msg.channel, msg.event)

  case class Msg(event: ClientMsg, channel: Chan)

  type ChanSelect = Bus.channel.type => Chan

  object channel {
    def sri(s: Sri) = s"sri/${s.value}"
    val all = "all"
    val lobby = "lobby"
  }

  def msg(event: ClientMsg, chan: ChanSelect) =
    Msg(event, chan(channel))

  def size = impl.size
  def sizeOf(chan: ChanSelect) = impl sizeOf chan(channel)

  
}
