package oyun.ws

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._

final class RoomCrowd(
  groupedWithin: util.GroupedWithin
)() {

  import RoomCrowd._

  private val rooms = new ConcurrentHashMap[RoomId, RoomState](1024)

  def connect(roomId: RoomId, user: Option[User]): Unit = publish(
    roomId,
    rooms.compute(roomId, (_, cur) => Option(cur).getOrElse(RoomState()) connect user)
  )

  def disconnect(roomId: RoomId, user: Option[User]): Unit = {
    val room = rooms.computeIfPresent(roomId, (_, room) => {
      val newRoom = room disconnect user
      if (newRoom.isEmpty) null else newRoom
    })
    if (room != null) publish(roomId, room)
  }

  private def publish(roomId: RoomId, room: RoomState): Unit =
    outputBatch(outputOf(roomId, room))

  private val outputBatch = groupedWithin[Output](1024, 1.second) { outputs =>

  }



}

object RoomCrowd {

  case class Output(
    roomId: RoomId,
    members: Int,
    users: Iterable[User.ID],
    anons: Int
  )

  def outputOf(roomId: RoomId, room: RoomState) = Output(
    roomId = roomId,
    members = room.nbMembers,
    users = room.users.keys,
    anons = room.anons
  )

  case class RoomState(
    anons: Int = 0,
    users: Map[User.ID, Int] = Map.empty) {

    def nbUsers = users.size
    def nbMembers = anons + nbUsers
    def isEmpty = nbMembers < 1


    def connect(user: Option[User]) = user.fold(copy(anons = anons + 1)) { u =>
      copy(users = users.updatedWith(u.id){ cur => Some(cur.fold(1)(_ + 1)) })
    }

    def disconnect(user: Option[User]) = user.fold(copy(anons = anons - 1)) { u =>
      copy(users = users.updatedWith(u.id){_.map(_ - 1).filter(_ > 0)})
    }
    

  }


}
