package oyun.ws

import java.util.concurrent.ConcurrentHashMap

final class History[K <: StringValue, V <: ipc.ClientIn.HasVersion](
  historySize: Int,
  initialCapacity: Int) {

  private val histories = new ConcurrentHashMap[String, List[V]](initialCapacity)

  def add(key: K, event: V): Unit =
    histories.compute(
      key.toString,
      (_, cur) => event :: Option(cur).getOrElse(Nil).take(historySize)
    )

  def getFrom(key: K, versionOpt: Option[SocketVersion]): Option[List[V]] = {
    val allEvents = histories.getOrDefault(key.toString, Nil)
    versionOpt
      .fold(Option(allEvents.take(5))) { since =>
        if (allEvents.headOption.fold(true)(_.version <= since)) Some(Nil)
        else {
          val events = allEvents.takeWhile(_.version > since)
          if (events.size == events.headOption.fold(0)(_.version.value) - since.value) Some(events)
          else None
        }
      }.map(_.reverse)
  }

  def stop(key: K) = histories.remove(key.toString)

  def hasEvents(key: K) = Option(histories get key.toString).exists(_.nonEmpty)

  def size = histories.size
}

object History {

  val masa = new History[Masa.Id, ipc.ClientIn.MasaVersioned](20, 32768)
  
}
