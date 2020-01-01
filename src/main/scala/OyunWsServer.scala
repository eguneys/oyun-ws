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

  lazy val router = wire[Router]
  lazy val nettyServer = wire[netty.NettyServer]

  wire[OyunWsServer].start
}

final class OyunWsServer(
  nettyServer: netty.NettyServer
)() {

  def start(): Unit = {

    nettyServer.start

  }
  
}

object OyunWsServer {

  val connections = new java.util.concurrent.atomic.AtomicInteger
  
}
