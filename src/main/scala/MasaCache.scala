package oyun.ws

import java.util.concurrent.ConcurrentHashMap

final class MasaCache[K <: StringValue](
  initialCapacity: Int) {

  private val tables = new ConcurrentHashMap[String, Masa.PlayerStore](initialCapacity)

  def add(key: K, store: Masa.PlayerStore): Unit =
    tables.put(key.toString, store)
  
  def get(key: K, userId: User.ID): Option[Masa.MasaPlayer] = {
    val store = Option(tables.get(key.toString))
    store.flatMap(_(userId))
  }

  def stop(key: K) = tables.remove(key.toString)

  def size = tables.size

}

object MasaCache {

  val masa = new MasaCache[Masa.Id](64)

}
