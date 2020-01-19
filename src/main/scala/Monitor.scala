package oyun.ws

import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import kamon.Kamon
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

final class Monitor(config: Config,
  services: Services)(implicit scheduler: akka.actor.typed.Scheduler, ec: ExecutionContext) {

  import Monitor._


  def start(): Unit = {

    val version = System.getProperty("java.version")
    val memory = Runtime.getRuntime().maxMemory / 1024 / 1024
    val useEpoll = config.getBoolean("netty.useEpoll")
    val useKamon = config.getString("kamon.influxdb.hostname").nonEmpty

    logger.info(s"oyun-ws netty epoll=$useEpoll kamon=$useKamon")
    logger.info(s"Java version $version, memory: $memory")

    if (useKamon) kamon.Kamon.loadModules()

    scheduler.scheduleWithFixedDelay(5.seconds, 1949.millis) { () =>
      periodicMetrics()
    }

  }


  private def periodicMetrics() = {

    val members = OyunWsServer.connections.get
    val rounds = services.roundCrowd.size
    services.lobby.pong.update(members, rounds)

    connection.current update members
    historyMasaSize.update(History.masa.size)
    masaCacheSize.update(MasaCache.masa.size)
    busSize.update(Bus.size)
    
  }

  
}

object Monitor {

  private val logger = Logger(getClass)

  object connection {
    val current = Kamon.gauge("connection.current").withoutTags
  }

  val historyMasaSize = Kamon.gauge("history.masa.size").withoutTags

  val masaCacheSize = Kamon.gauge("cache.masa.size").withoutTags

  val busSize = Kamon.gauge("bus.size").withoutTags
  
}
