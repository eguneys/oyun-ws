package oyun.ws

import java.util.concurrent.ConcurrentHashMap
//import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import ipc._

import poker.Side

final class RoundCrowd(
  oyun: Oyun,
  groupedWithin: util.GroupedWithin
)() {

  import RoundCrowd._

  private val rounds = new ConcurrentHashMap[RoomId, RoundState](128)

  def connect(roomId: RoomId, user: Option[User], player: Option[Side]): Unit = publish(
    roomId,
    rounds.compute(roomId, (_, cur) => {
      Option(cur).getOrElse(RoundState()).connect(user, player)
    })
  )

  def disconnect(roomId: RoomId, user: Option[User], player: Option[Side]): Unit = {
    rounds.computeIfPresent(roomId, (_, round) => {
      val newRound = round.disconnect(user, player)
      publish(roomId, newRound)
      if (newRound.isEmpty) null else newRound
    })
  }


  private def publish(roomId: RoomId, room: RoundState): Unit =
    outputBatch(outputOf(roomId, room))


  private val outputBatch = groupedWithin[Output](256, 500.millis) { outputs =>

    val aggregated = outputs.foldLeft(Map.empty[RoomId, Output]) {
      case (crowds, crowd) =>
        crowds.updated(crowd.room.roomId, crowd)
    }.values

    oyun.emit.masa(OyunIn.RoundOnlines(aggregated))
  }

  def size = rounds.size

}

object RoundCrowd {

  case class Output(room: RoomCrowd.Output) {
    def isEmpty = room.members == 0
  }

  def outputOf(roomId: RoomId, round: RoundState) = Output(
    room = RoomCrowd.outputOf(roomId, round.room)
  )

  case class RoundState(
    room: RoomCrowd.RoomState = RoomCrowd.RoomState()
  ) {

    def connect(user: Option[User], player: Option[Side]) = copy(
      room = room connect user
    )
    def disconnect(user: Option[User], player: Option[Side]) = copy(
      room = room disconnect user
    )

    def isEmpty = room.isEmpty
  }
  
}
