package oyun.ws

import ipc.ClientIn.LobbyPong

final class Lobby(
) {

  object pong {
    private var value = LobbyPong(0, 0)

    def get = value

    def update(members: Int, rounds: Int): Unit = {
      value = LobbyPong(members, rounds)
    }
  }
  
}
