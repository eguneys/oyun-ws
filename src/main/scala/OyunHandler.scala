package oyun.ws

import com.typesafe.scalalogging.Logger
// import scala.concurrent.ExecutionContext

import ipc._

final class OyunHandler(
  oyun: Oyun
)() {

  import OyunOut._
  import Bus.publish


  private val logger = Logger(getClass)

  private val siteHandler: Emit[OyunOut] = {
    case TellSri(sri, payload) => publish(_ sri sri, ClientIn.Payload(payload))
    case msg => logger.warn(s"Unhandled site: $msg")
  }

  private val lobbyHandler: Emit[OyunOut] = {
    case site: SiteOut => siteHandler(site)
    case msg => logger.warn(s"Unhandled lobby: $msg")
  }

  private val masaHandler: Emit[OyunOut] = {
    implicit def masaRoomId(masaId: Masa.Id): RoomId = RoomId(masaId)
    ({
      case MasaVersion(masaId, version, flags, tpe, data) =>
        val versioned = ClientIn.MasaVersioned(version, flags, tpe, data)
        // History.round.add(masaId, versioned)
        publish(_ room masaId, versioned)
      case msg => roomHandler(msg)
    })
  }

  private val roomHandler: Emit[OyunOut] = {
    case msg => logger.warn(s"Unhandled room: $msg")
  }


  oyun.setHandlers({
    case Oyun.chans.lobby.out => lobbyHandler
    case Oyun.chans.masa.out => masaHandler
    case chan => in => logger.warn(s"Unknown channel $chan sent $in")
  })
}
