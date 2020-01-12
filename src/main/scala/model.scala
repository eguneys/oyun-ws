package oyun.ws

import poker.Side

trait StringValue extends Any {
  def value: String
  override def toString = value
}
trait IntValue extends Any {
  def value: Int
  override def toString = value.toString
}

case class User(id: User.ID) extends AnyVal

object User {
  type ID = String
}

object Masa {

  case class Id(value: String) extends AnyVal with StringValue {
    def full(userId: User.ID) = FullId(s"$value$userId")
  }

  case class FullId(value: String) extends AnyVal with StringValue {
    def masaId = Id(value take 8)
    def userId = value drop 8
  }

  case class MasaPlayer(side: Side) {

  }

}


case class Sri(value: String) extends AnyVal with StringValue

object Sri {
  type Str = String
  def random = Sri(util.Util.random string 12)
  def from(str: String): Option[Sri] =
    if (str contains ' ') None
    else Some(Sri(str))
}



case class IpAddress(value: String) extends AnyVal with StringValue

case class Path(value: String) extends AnyVal with StringValue

case class ChapterId(value: String) extends AnyVal with StringValue

case class JsonString(value: String) extends AnyVal with StringValue

case class SocketVersion(value: Int) extends AnyVal with IntValue with Ordered[SocketVersion] {
  def compare(other: SocketVersion) = Integer.compare(value, other.value)
}


case class RoomId(value: String) extends AnyVal with StringValue

object RoomId {
  def apply(v: StringValue): RoomId = RoomId(v.value)
}

case class ReqId(value: Int) extends AnyVal with IntValue

case class MasaEventFlags(
  player: Option[poker.Side]
)
