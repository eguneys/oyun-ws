package oyun.ws

import akka.actor.typed.{ ActorSystem, Scheduler }
import com.softwaremill.macwire._
import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.ExecutionContext

object Boot extends App {

  lazy val config: Config = ConfigFactory.load
  lazy val clientSystem: ClientSystem = ActorSystem(Clients.behavior, "clients")
  implicit def scheduler: Scheduler = clientSystem.scheduler
  implicit def executionContext: ExecutionContext = clientSystem.executionContext

  lazy val groupedWithin = wire[util.GroupedWithin]
  lazy val oyunRedis = wire[Oyun]
  lazy val oyunHandlers = wire[OyunHandler]
  lazy val roundCrowd = wire[RoundCrowd]
  lazy val services = wire[Services]
  lazy val users = wire[Users]
  lazy val auth = wire[Auth]
  lazy val mongo = wire[Mongo]
  lazy val lobby = wire[Lobby]
  lazy val controller = wire[Controller]
  lazy val router = wire[Router]
  lazy val nettyServer = wire[netty.NettyServer]
  lazy val monitor = wire[Monitor]

  wire[OyunWsServer].start
}

final class OyunWsServer(
  nettyServer: netty.NettyServer,
  handlers: OyunHandler, // must eagerly instanciate!
  monitor: Monitor
)() {

  def start(): Unit = {

    monitor.start

    nettyServer.start

  }
  
}

object OyunWsServer {

  val connections = new java.util.concurrent.atomic.AtomicInteger
  
}
