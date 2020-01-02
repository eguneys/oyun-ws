package oyun.ws

final class RoomCrowd()() {



}

object RoomCrowd {

  case class RoomState(
    anons: Int = 0,
    users: Map[User.ID, Int] = Map.empty) {

    def nbUsers = users.size
    def nbMembers = anons + nbUsers
    def isEmpty = nbMembers < 1

    

  }


}
