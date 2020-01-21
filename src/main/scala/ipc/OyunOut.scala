package oyun.ws
package ipc

import poker.Side

sealed trait OyunOut

sealed trait SiteOut extends OyunOut
sealed trait LobbyOut extends OyunOut
sealed trait RoomOut extends OyunOut
sealed trait MasaOut extends RoomOut

sealed trait AnyRoomOut extends MasaOut

object OyunOut {

  // site
  case class TellSri(sri: Sri, json: JsonString) extends SiteOut with LobbyOut

  // room
  case class RoomStop(roomId: RoomId) extends AnyRoomOut

  // lobby
  case class TellLobby(json: JsonString) extends LobbyOut
  case class TellSris(sri: Seq[Sri], json: JsonString) extends LobbyOut


  // round
  case class MasaVersion(
    masaId: Masa.Id,
    version: SocketVersion,
    flags: MasaEventFlags,
    tpe: String,
    data: JsonString) extends MasaOut
  
  case class MasaPlayerStore(
    masaId: Masa.Id,
    store: Masa.PlayerStore) extends MasaOut

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

      case "room/stop" =>
        Some(RoomStop(RoomId(args)))

      case "m/ver" =>
        get(args, 6) {
          case Array(roomId, version, f, only, tpe, data) =>
            version.toIntOption map { sv =>
              val flags = MasaEventFlags(
                player = Side(only)
              )
              MasaVersion(Masa.Id(roomId), SocketVersion(sv), flags, tpe, JsonString(data))
            }
        }
      case "m/players" =>
        get(args, 2) {
          case Array(masaId, value) =>
            Some(MasaPlayerStore(Masa.Id(masaId), Masa.PlayerStore(value)))
        }
    }
  }

}
