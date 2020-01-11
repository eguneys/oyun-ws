package oyun.ws

package object ipc {

  trait ClientMsg

  sealed trait ClientCtrl extends ClientMsg

  object ClientCtrl {
    case object Disconnect extends ClientCtrl
    case class Broom(oldSeconds: Int) extends ClientCtrl
  }

  object ClientNull extends ClientMsg

  type ~[+A, +B] = Tuple2[A, B]
  object ~ {
    def apply[A, B](x: A, y: B)                              = Tuple2(x, y)
    def unapply[A, B](x: Tuple2[A, B]): Option[Tuple2[A, B]] = Some(x)
  }

}
