package oyun.ws

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ Behavior, PostStop }

import ipc._

object LobbyClientActor {

  case class State(
    idle: Boolean = false,
    site: ClientActor.State = ClientActor.State()
  )

  import ClientActor._

  def start(deps: Deps): Behavior[ClientMsg] = Behaviors.setup { ctx =>
    // import deps._
    onStart(deps, ctx)
    // req.user foreach { users.connect(_, ctx.self, silently = true) }
    // services.lobby.connect(req.sri -> req.user.map(_.id))
    // Bus.subscribe(Bus.channel.lobby, ctx.self)
    apply(State(), deps)
  }
  

  private def apply(state: State, deps: Deps): Behavior[ClientMsg] =
    Behaviors
      .receive[ClientMsg] { (ctx, msg) =>
        import deps._

        msg match {
          case msg: ClientOut.Ping =>
            clientIn(services.lobby.pong.get)
            apply(state.copy(site = sitePing(state.site, deps, msg)), deps)
          case _ => Behaviors.same
        }

      }
      .receiveSignal {
        case (ctx, PostStop) =>
          onStop(state.site, deps, ctx)
          Behaviors.same
      }
}
