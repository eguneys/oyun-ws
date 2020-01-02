package oyun.ws
package ipc

sealed trait OyunIn {
  def write: String
  def critical: Boolean = false // will be buffered and reset after oyun reboots
}

object OyunIn {
  sealed trait Site extends OyunIn

  sealed trait Lobby extends OyunIn

  case class ConnectUser(user: User) extends Site {
    def write = s"connect/user ${user.id}"
  }

  case object WsBoot extends Site {
    def write = "boot"
    override def critical = true
  }
}
