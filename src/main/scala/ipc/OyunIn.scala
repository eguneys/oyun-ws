package oyun.ws
package ipc

import play.api.libs.json._

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

  case class MasaSit(fullId: Masa.FullId, side: String) extends Masa {
    def write = s"m/sit $fullId $side"
  }


  private def commas(as: Iterable[Any]): String = if (as.isEmpty) "-" else as mkString ","
  private def optional(s: Option[String]): String = s getOrElse "-"
}
