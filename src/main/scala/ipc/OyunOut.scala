package oyun.ws
package ipc

sealed trait OyunOut

sealed trait SiteOut extends OyunOut
sealed trait LobbyOut extends OyunOut

object OyunOut {

  // site
  case class TellSri(sri: Sri, json: JsonString) extends SiteOut with LobbyOut

  // lobby
  case class TellLobby(json: JsonString) extends LobbyOut
  case class TellSris(sri: Seq[Sri], json: JsonString) extends LobbyOut
  

  // impl

  private def get(args: String, nb: Int)(
    f: PartialFunction[Array[String], Option[OyunOut]]
  ): Option[OyunOut] =
    f.applyOrElse(args.split(" ", nb), (_: Array[String]) => None)

  def read(str: String): Option[OyunOut] = {
    val parts = str.split(" ", 2)
    val args = parts.lift(1) getOrElse ""
    parts(0) match {
      case "tell/sri" =>
        get(args, 2) {
          case Array(sri, payload) => Some(TellSri(Sri(sri), JsonString(payload)))
        }
    }
  }

}
