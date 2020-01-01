package oyun.ws

package object ipc {

  trait ClientMsg

  sealed trait ClientCtrl extends ClientMsg

  object ClientCtrl {
    case object Disconnect extends ClientCtrl
    case class Broom(oldSeconds: Int) extends ClientCtrl
  }

  object ClientNull extends ClientMsg

}
