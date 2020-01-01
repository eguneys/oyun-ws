package oyun.ws

import com.typesafe.scalalogging.Logger

final class RateLimit(
  maxCredits: Int,
  intervalMillis: Int,
  name: String
) {

  import RateLimit._

  private def makeClearAt: Long = nowMillis + intervalMillis

  private var credits: Long = maxCredits
  private var clearAt: Long = makeClearAt
  private var logged: Boolean = false

  def apply(msg: => String = ""): Boolean =
    if (credits > 0) {
      credits -= 1
      true
    } else if (clearAt < nowMillis) {
      credits = maxCredits
      clearAt = makeClearAt
      true
    } else {
      if (!logged) {
        logged = true
        logger.info(s"$name MSG: $msg")
      }
      false
    }
}

object RateLimit {
  type Charge = Cost => Unit
  type Cost = Int

  private def nowMillis = System.currentTimeMillis()

  private val logger = Logger(getClass)
  
}
