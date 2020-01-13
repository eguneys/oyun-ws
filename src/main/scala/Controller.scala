package oyun.ws

import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import io.netty.handler.codec.http.HttpResponseStatus
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import util.RequestHeader

final class Controller(
  config: Config,
  auth: Auth,
  services: Services
)(implicit ec: ExecutionContext) {

  import Controller._
  import ClientActor.{ Deps, Req }
  // private val logger = Logger(getClass)

  def site(req: RequestHeader, emit: ClientEmit) = WebSocket(req) { sri => user =>
    Future successful endpoint(
      name = "site",
      behavior = SiteClientActor start {
        Deps(emit, Req(req, sri, user), services)
      },
      credits = 50,
      interval = 20.seconds
    )
  }

  def lobby(req: RequestHeader, emit: ClientEmit) = WebSocket(req) { sri => user =>
    Future successful endpoint(
      name = "lobby",
      behavior = LobbyClientActor start {
        Deps(emit, Req(req, sri, user), services)
      },
      credits = 30,
      interval = 30.seconds
    )
  }


  def masaPlay(id: Masa.Id, req: RequestHeader, emit: ClientEmit) = WebSocket(req) { sri => user =>
    Future successful endpoint(
      name = "masa/play",
      behavior = MasaClientActor.start(
        RoomActor.State(RoomId(id)),
        fromVersion(req)
      ) { Deps(emit, Req(req, sri, user), services) },
      credits = 100,
      interval = 20.seconds
    )
  }



  private def WebSocket(req: RequestHeader)(f: Sri => Option[User] => Response): Response =
    ValidSri(req) { sri =>
      auth(req) flatMap f(sri)
    }

  private def ValidSri(req: RequestHeader)(f: Sri => Response): Response = req.sri match {
    case Some(validSri) => f(validSri)
    case None => Future successful Left(HttpResponseStatus.BAD_REQUEST)
  }

  // private def notFound = Left(HttpResponseStatus.NOT_FOUND)

  private def fromVersion(req: RequestHeader): Option[SocketVersion] =
    req queryParameter "v" flatMap (_.toIntOption) map SocketVersion.apply

}

object Controller {

  val logger = Logger(getClass)

  final class Endpoint(val behavior: ClientBehavior, val rateLimit: RateLimit)

  def endpoint(
    name: String,
    behavior: ClientBehavior,
    credits: Int,
    interval: FiniteDuration
  ) = {
    Right(
      new Endpoint(
        behavior,
        new RateLimit(
          maxCredits = credits,
          intervalMillis = interval.toMillis.toInt,
          name = name
        )
      )
    )
  }

  type Response = Future[Either[HttpResponseStatus, Endpoint]]

}
