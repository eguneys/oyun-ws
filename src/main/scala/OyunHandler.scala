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

  oyun.setHandlers({
    case Oyun.chans.lobby.out => lobbyHandler
  })
}
