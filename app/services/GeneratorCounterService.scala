package services

import scala.concurrent.Future

import play.api.Play
import play.api.Play.current

/**
 * A counter implementation that generates numbers that are unique across a cluster of
 * servers and somewhat time-ordered.
 *
 * The number returned consists of (from most significant to least significant bits):
 *  - A timestamp representing the number of seconds since the epoch
 *  - A 6-bit node identifier (configured in "application.nodeId" in application.conf)
 *  - A 10-bit rolling counter
 *
 * This allows 1024 ids to be generated per second, per app node with up to 32 nodes
 * running the shortening service.
 *
 * The benefit of this counter is that it can generate new values very fast. The drawback is that it
 * creates fairly large numbers which correspond to longer URLs (up to 8 characters)
 */
class GeneratorCounterService extends CounterService {
  lazy val nodeId = Play.application.configuration.getLong("application.nodeId").get

  // The rolling counter
  private var roller = -1

  def nextValue: Future[BigInt] = {
    nextValueInternal(System.currentTimeMillis)
  }

  // Helper that is used for unit tests to get consistent results
  private[services] def nextValueInternal(now: Long) = {
    val nowInSeconds = now / 1000L
    val nextRollingValue = this.synchronized[Long] {
      roller = (roller + 1) % 1024
      roller
    }
    Future.successful(BigInt(nextRollingValue + (1024 * nodeId) + (65536 * nowInSeconds)))
  }
}
