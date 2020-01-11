package oyun.ws

import com.typesafe.scalalogging.Logger

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ Behavior, PostStop }

import ipc._

object MasaClientActor {

  private val logger = Logger(getClass)

  import ClientActor._

  case class State(
    room: RoomActor.State,
    player: Option[Masa.MasaPlayer],
    site: ClientActor.State = ClientActor.State()) {

    def busChans: List[Bus.Chan] =
      Bus.channel.room(room.id) :: Nil
  }

  def start(
    roomState: RoomActor.State,
    player: Option[Masa.MasaPlayer])(deps: Deps): Behavior[ClientMsg] = Behaviors.setup { ctx =>
    import deps._
    val state = State(roomState, player)
    onStart(deps, ctx)
    req.user foreach { users.connect(_, ctx.self) }
    state.busChans foreach { Bus.subscribe(_, ctx.self) }
    apply(state, deps)
  }

  private def apply(state: State, deps: Deps): Behavior[ClientMsg] =
    Behaviors
      .receive[ClientMsg] { (ctx, msg) =>
        import deps._

        def masaId = Masa.Id(state.room.id.value)
        def fullId = req.userId map { uid =>
          masaId.full(uid)
        }

        msg match {

          case ClientOut.MasaPlayerForward(payload) =>
            fullId foreach { fid =>
              oyunIn.masa(OyunIn.MasaPlayerDo(fid, payload))
            }
            Behaviors.same
          case ClientOut.MasaSit(side) =>
            fullId foreach { fid =>
              oyunIn.masa(OyunIn.MasaSit(fid, side))
            }
            Behaviors.same
          case msg: ClientOutSite =>
            val siteState = globalReceive(state.site, deps, ctx, msg)
            if (siteState == state.site) Behaviors.same
            else apply(state.copy(site = siteState), deps)
          case m =>
            logger.warn(s"client out unhandled round $m")
            Behaviors.same
        }
      }.receiveSignal {
        case (ctx, PostStop) =>
          onStop(state.site, deps, ctx)
          state.busChans foreach { Bus.unsubscribe(_, ctx.self) }
          Behaviors.same
      }

}
