package oyun.ws

import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import io.lettuce.core._
// import io.lettuce.core.pubsub._
import java.util.concurrent.ConcurrentLinkedQueue
import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }

import ipc._

final class Oyun(config: Config)
  (implicit ec: ExecutionContext) {

  import Oyun._

  object status {
    private var value: Status = Online
    def setOffline() = { value = Offline }
    def setOnline(init: () => Unit) = {
      value = Online
      init()
      buffer.flush()
    }
    def isOnline: Boolean = value == Online
  }

  private object buffer {
    case class Buffered(chan: String, msg: String)
    private val queue = new ConcurrentLinkedQueue[Buffered]()

    def enqueue(chan: String, msg: String) = {
      queue offer Buffered(chan, msg)
    }

    @tailrec def flush(): Unit = {
      val next = queue.poll()
      if (next != null) {
        connIn.async.publish(next.chan, next.msg)
        flush()
      }
    }
  }

  private val logger = Logger(getClass)
  private val redis = RedisClient create RedisURI.create(config.getString("redis.uri"))
  private val connIn = redis.connectPubSub
  private val connOut = redis.connectPubSub

  val emit: Emits = Await.result({
    logger.info(s"Redis connection")
    connectAll
  }, 3.seconds)

  private def connectAll: Future[Emits] =
    connect[OyunIn.Lobby](chans.lobby) map {
      case lobby =>
        new Emits(
          lobby
        )
    }


  private def connect[In <: OyunIn](chan: Chan): Future[Emit[In]] = {

    val emit: Emit[In] = in => {
      val msg = in.write
      val path = msg.takeWhile(' '.!=)
      println(msg)
      if (status.isOnline) {
        connIn.async.publish(chan.in, msg)
      } else if (in.critical) {
        buffer.enqueue(chan.in, msg)
      } else {
        // dropped
        logger.info(s"[redis.drop] $chan.in, $path")
      }
    }

    val promise = Promise[Emit[In]]

    connOut.async.subscribe(chan.out) thenRun { () =>
      connIn.async.publish(chan.in, OyunIn.WsBoot.write)
      promise success emit
    }

    promise.future
  }
  
}

object Oyun {

  sealed trait Status
  case object Online extends Status
  case object Offline extends Status

  sealed abstract class Chan(value: String) {
    val in = s"$value-in"
    val out = s"$value-out"
  }

  object chans {
    object lobby extends Chan("lobby")
  }

  final class Emits(
    val lobby: Emit[OyunIn.Lobby]
  )
  
}
