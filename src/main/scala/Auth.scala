package oyun.ws

import reactivemongo.api.bson._
import scala.concurrent.{ ExecutionContext, Future }

import util.RequestHeader

final class Auth(mongo: Mongo)(implicit executionContext: ExecutionContext) {

  def apply(req: RequestHeader): Future[Option[User]] =
    sessionIdFromReq(req) match {
      case Some(sid) =>
        mongo.security {
          _.find(
            BSONDocument("_id" -> sid, "up" -> true),
            Some(BSONDocument("_id" -> false, "user" -> true))
          ).one[BSONDocument]
        } map { p =>
          p flatMap {
            _.getAsOpt[User.ID]("user") map User.apply
          }
        }
      case None => {
        Future successful None
      }
    }

  private val cookieName = "oyun2"
  private val sessionIdKey = "sessionId"
  private val sessionIdRegex = s"""$sessionIdKey=(\\w+)""".r.unanchored

  def sessionIdFromReq(req: RequestHeader): Option[String] =
    req cookie cookieName flatMap {
      case sessionIdRegex(id) => Some(id)
      case _ => None
    } orElse 
      req.queryParameter(sessionIdKey)

}
