package oyun.ws

import com.typesafe.config.Config
// import reactivemongo.api.bson._
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{ AsyncDriver, DefaultDB, MongoConnection }
import scala.concurrent.ExecutionContext.parasitic
import scala.concurrent.{ ExecutionContext, Future }

final class Mongo(config: Config)(implicit executionContext: ExecutionContext) {


  private val uri = config.getString("mongo.uri")
  private val driver = new AsyncDriver(Some(config.getConfig("reactivemongo")))
  private val parsedUri = MongoConnection.parseURI(uri)
  private val connection = Future.fromTry(parsedUri).flatMap(driver.connect)

  private def db: Future[DefaultDB] = connection.flatMap(_ database parsedUri.get.db.get)
  private def collNamed(name: String) = db.map(_ collection name)(parasitic)
  def securityColl = collNamed("security")

  def security[A](f: BSONCollection => Future[A]): Future[A] = securityColl flatMap f

}
