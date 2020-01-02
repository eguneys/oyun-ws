package oyun.ws

import java.util.concurrent.ConcurrentHashMap
//import scala.concurrent.ExecutionContext

final class RoundCrowd(
)() {

  import RoundCrowd._

  private val rounds = new ConcurrentHashMap[RoomId, RoundState](32768)

  def size = rounds.size

}

object RoundCrowd {

  case class RoundState(
    room: RoomCrowd.RoomState = RoomCrowd.RoomState()
  ) {

  }
  
}
