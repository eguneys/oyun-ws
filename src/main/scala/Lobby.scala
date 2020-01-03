package oyun.ws

import scala.concurrent.duration._

import ipc.ClientIn.LobbyPong
import ipc.OyunIn

final class Lobby(
  oyun: Oyun,
  groupedWithin: util.GroupedWithin
) {

  private val oyunIn = oyun.emit.lobby

  val connect = groupedWithin[(Sri, Option[User.ID])](6, 500.millis) { connects =>
    oyunIn(OyunIn.ConnectSris(connects))
  }

  val disconnect = groupedWithin[Sri](50, 500.millis) { sris =>
    oyunIn(OyunIn.DisconnectSris(sris))
  }

  object pong {
    private var value = LobbyPong(0, 0)

    def get = value

    def update(members: Int, rounds: Int): Unit = {
      value = LobbyPong(members, rounds)
    }
  }
  
}
