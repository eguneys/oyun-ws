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
    site: ClientActor.State = ClientActor.State()) {

    def busChans: List[Bus.Chan] =
      Bus.channel.room(room.id) :: Nil
  }

  def start(
    roomState: RoomActor.State,
    fromVersion: Option[SocketVersion])(deps: Deps): Behavior[ClientMsg] = Behaviors.setup { ctx =>
    import deps._
    val state = State(roomState)
    onStart(deps, ctx)
    req.user foreach { users.connect(_, ctx.self) }
    state.busChans foreach { Bus.subscribe(_, ctx.self) }

    def masaId = Masa.Id(state.room.id.value)

    def oPlayer = req.userId flatMap { uid =>
      MasaCache.masa.get(masaId, uid)
    }

    History.masa.getFrom(Masa.Id(roomState.id.value), fromVersion) match {
      case None => clientIn(ClientIn.Resync)
      case Some(events) => {
        events map { versionFor(state, oPlayer, _) } foreach clientIn
      }
    }
    apply(state, deps)
  }

  def versionFor(state: State, oPlayer: Option[Masa.MasaPlayer], msg: ClientIn.MasaVersioned): ClientIn.Payload = {
    if (msg.flags.player.exists(s => oPlayer.fold(true)(_.side != s))) msg.skip
    else msg.full
  }

  private def apply(state: State, deps: Deps): Behavior[ClientMsg] =
    Behaviors
      .receive[ClientMsg] { (ctx, msg) =>
        import deps._

        def masaId = Masa.Id(state.room.id.value)
        def fullId = req.userId map { uid =>
          masaId.full(uid)
        }

        def oPlayer = req.userId flatMap { uid =>
          MasaCache.masa.get(masaId, uid)
        }

        msg match {
          case versioned: ClientIn.MasaVersioned =>
            clientIn(versionFor(state, oPlayer, versioned))
            Behaviors.same
          case ClientOut.MasaPlayerForward(payload) =>
            fullId foreach { fid =>
              oyunIn.masa(OyunIn.MasaPlayerDo(fid, payload))
            }
            Behaviors.same
          case ClientOut.MasaMove(uci, ackId) =>
            fullId foreach { fid =>
              clientIn(ClientIn.Ack(ackId))
              oyunIn.masa(OyunIn.MasaMove(fid, uci))
            }
            Behaviors.same
          case ClientOut.MasaSit(side) =>
            fullId foreach { fid =>
              oyunIn.masa(OyunIn.MasaSit(fid, side))
            }
            Behaviors.same
          case ClientOut.MasaSitOutNext(value) =>
            oPlayer foreach { player =>
              oyunIn.masa(OyunIn.MasaSitOutNext(masaId, player.side, value))
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
