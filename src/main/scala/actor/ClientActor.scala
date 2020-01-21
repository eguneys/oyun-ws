package oyun.ws

import akka.actor.typed.scaladsl.{ ActorContext }//, Behaviors }
// import akka.actor.typed.Behavior

import ipc._
import util.Util.nowSeconds

object ClientActor {

  case class State(
    lastPing: Int = nowSeconds)


  def onStart(deps: Deps, ctx: ActorContext[ClientMsg]): Unit = {
    OyunWsServer.connections.incrementAndGet
    busChansOf(deps.req) foreach { Bus.subscribe(_, ctx.self) }
  }

  def onStop(state: State, deps: Deps, ctx: ActorContext[ClientMsg]): Unit = {
    import deps._
    OyunWsServer.connections.decrementAndGet
    busChansOf(req) foreach { Bus.unsubscribe(_, ctx.self) }
  }

  def sitePing(state: State, deps: Deps, msg: ClientOut.Ping): State = {
    // for { l <- msg.lag, u <- deps.req.user } 
    //   deps.services.lag(u.id -> l)
    state.copy(lastPing = nowSeconds)
  }

  def globalReceive(
    state: State,
    deps: Deps,
    ctx: ActorContext[ClientMsg],
    msg: ClientOutSite
  ): State = {
    import deps._

    msg match {
      case msg: ClientOut.Ping =>
        clientIn(ClientIn.Pong)
        sitePing(state, deps, msg)
      case ClientOut.Ignore =>
        state
      case _ =>
        state
    }
  }

  def clientInReceive(state: State, deps: Deps, msg: ClientIn): Option[State] = msg match {
    case in: ClientIn =>
      deps clientIn in
      None
  }


  private def busChansOf(req: Req) =
    Bus.channel.all :: Bus.channel.sri(req.sri) :: Nil

  def Req(req: util.RequestHeader, sri: Sri, user: Option[User]): Req = Req(
    name = req.name,
    ip = req.ip,
    sri = sri,
    user = user
  )

  case class Req(
    name: String,
    ip: IpAddress,
    sri: Sri,
    user: Option[User]) {
    def userId = user.map(_.id)
    override def toString = s"${user getOrElse "Anon"} $name"
  }

  case class Deps(
    clientIn: ClientEmit,
    req: Req,
    services: Services) {

    def oyunIn = services.oyun
    def users = services.users
    def roundCrowd = services.roundCrowd
  }
  
}
