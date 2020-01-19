package oyun.ws
package ipc

import poker.format.{ Uci }
import play.api.libs.json._
import scala.util.{ Success, Try }

import oyun.ws.util.OyunJsObject.augment

sealed trait ClientOut extends ClientMsg

sealed trait ClientOutSite extends ClientOut
sealed trait ClientOutLobby extends ClientOut
sealed trait ClientOutMasa extends ClientOut

object ClientOut {

  case class Ping(lag: Option[Int]) extends ClientOutSite

  case class Unexpected(msg: JsValue) extends ClientOutSite

  case object WrongHole extends ClientOutSite

  case object Ignore extends ClientOutSite

  // lobby

  case class LobbyForward(payload: JsValue) extends ClientOutLobby


  // round

  case class MasaPlayerForward(payload: JsValue) extends ClientOutMasa
  case class MasaMove(uci: Uci, ackId: Option[Int]) extends ClientOutMasa
  case class MasaSit(side: String) extends ClientOutMasa
  case class MasaSitOutNext(value: Boolean) extends ClientOutMasa

  def parse(str: String): Try[ClientOut] =
    if (str == "null" || str == """{"t":"p"}""") emptyPing
    else
      Try(Json parse str) map {
        case o: JsObject =>
          o str "t" flatMap {
            case "p" => Some(Ping(o int "l"))

            // round
            case "move" =>
              for {
                d <- o obj "d"
                move <- d str "u" flatMap Uci.Move.apply
                ackId = d int "a"
              } yield MasaMove(move, ackId)
            case "join" | "hookIn" | "hookOut" =>
              Some(LobbyForward(o))
            case "sit" =>
              for {
                side <- o str "d"
              } yield MasaSit(side)
            case "sitoutNext" =>
              o boolean "d" map MasaSitOutNext.apply
            case _ => None
          } getOrElse Unexpected(o)
        case js => Unexpected(js)
      }
  
  private val emptyPing: Try[ClientOut] = Success(Ping(None))
}
