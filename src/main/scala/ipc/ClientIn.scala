package oyun.ws
package ipc

// import play.api.libs.json._

sealed trait ClientIn extends ClientMsg {
  def write: String
}

object ClientIn {

  case object Pong extends ClientIn {
    val write = "0"
  }

  case object Resync extends ClientIn {
    val write = cliMsg("resync")
  }

  case object Disconnect extends ClientIn {
    val write = cliMsg("bye")
  }

  // private def cliMsg[A: Writes](t: String, data: A): String = Json stringify Json.obj(
  //   "t" -> t,
  //   "d" -> data
  // )

  // private def cliMsg(t: String, data: JsonString): String = s"""{"t":"$t","d":${data.value}}"""
  // private def cliMsg(t: String, data: JsonString, version: SocketVersion): String =
  //   s"""{"t":"$t","v":$version,"d":${data.value}}"""
  // private def cliMsg(t: String, int: Int): String      = s"""{"t":"$t","d":$int}"""
  // private def cliMsg(t: String, bool: Boolean): String = s"""{"t":"$t","d":$bool}"""
  private def cliMsg(t: String): String                = s"""{"t":"$t"}"""
  
}
