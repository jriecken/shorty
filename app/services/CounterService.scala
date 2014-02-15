package services

import scala.concurrent.Future

/**
 * Service that generates a unique sequence of BigInt values
 */
trait CounterService {
  /**
   * Get the next value from the counter.
   */
  def nextValue: Future[BigInt]
}
