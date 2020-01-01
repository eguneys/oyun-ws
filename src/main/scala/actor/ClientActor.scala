package oyun.ws

import akka.actor.typed.scaladsl.{ ActorContext }//, Behaviors }
// import akka.actor.typed.Behavior

import ipc._

object ClientActor {

  case class State()


  def onStart(deps: Deps, ctx: ActorContext[ClientMsg]): Unit = {
    OyunWsServer.connections.incrementAndGet

  }

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

  }
  
}
