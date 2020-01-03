package oyun.ws

final class Services(
  oyunRedis: Oyun,
  val users: Users,
  val lobby: Lobby,
  val roundCrowd: RoundCrowd) {

  def oyun = oyunRedis.emit

}
