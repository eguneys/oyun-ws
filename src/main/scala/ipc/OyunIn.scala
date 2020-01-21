package oyun.ws
package ipc

import poker.format.{ Uci }

import play.api.libs.json._

import poker.Side

sealed trait OyunIn {
  def write: String
  def critical: Boolean = false // will be buffered and reset after oyun reboots
}

object OyunIn {
  sealed trait Site extends OyunIn

  sealed trait Lobby extends OyunIn
  sealed trait Masa extends OyunIn

  case class TellSri(sri: Sri, userId: Option[User.ID], payload: JsValue) extends Site with Lobby {
    def write = s"tell/sri $sri ${optional(userId)} ${Json.stringify(payload)}"
  }

  case class ConnectUser(user: User) extends Site {
    def write = s"connect/user ${user.id}"
  }

  case object WsBoot extends Site {
    def write = "boot"
    override def critical = true
  }

  type SriUserId = (Sri, Option[User.ID])
  case class ConnectSris(sris: Iterable[SriUserId]) extends Lobby {
    private def render(su: SriUserId) = s"${su._1}${su._2.fold("")(" " + _)}"
    def write = s"connect/sris ${commas(sris map render)}"
  }

  case class DisconnectSris(sris: Iterable[Sri]) extends Lobby {
    def write = s"disconnect/sris ${commas(sris)}"
  }


  case class MasaPlayerDo(fullId: Masa.FullId, payload: JsValue) extends Masa {
    def write = s"m/do $fullId ${Json.stringify(payload)}"
  }

  case class MasaMove(fullId: Masa.FullId, uci: Uci) extends Masa {
    def write =
      s"m/move $fullId ${uci.uci}"
    override def critical = true
  }

  case class MasaSit(fullId: Masa.FullId, side: String) extends Masa {
    def write = s"m/sit $fullId $side"
  }

  case class MasaSitOutNext(masaId: Masa.Id, side: Side, value: Boolean) extends Masa {
    def write = s"m/sitoutnext $masaId ${writeSide(side)} ${boolean(value)}"
  }

  case class RoundOnlines(many: Iterable[RoundCrowd.Output]) extends Masa {
    private def one(r: RoundCrowd.Output) =
      if (r.isEmpty) r.room.roomId.value
      else s"${r.room.roomId}${r.room.members}"
    def write = s"r/ons ${commas(many map one)}"
  }

  private def commas(as: Iterable[Any]): String = if (as.isEmpty) "-" else as mkString ","
  private def boolean(b: Boolean): String = if (b) "+" else "-"
  private def optional(s: Option[String]): String = s getOrElse "-"
  private def writeSide(s: Side): String = s.index.toString
}
