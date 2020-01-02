package oyun.ws

import io.netty.handler.codec.http.HttpResponseStatus
import scala.concurrent.Future

import util.RequestHeader

final class Router(controller: Controller) {

  def apply(req: RequestHeader, emit: ClientEmit): Controller.Response =
    req.path drop 1 split "/" match {
      case Array("lobby", "socket") => controller.lobby(req, emit)
      case Array("lobby", "socket", _) => controller.lobby(req, emit)
      case _ => Future successful Left(HttpResponseStatus.NOT_FOUND)
    }

}
