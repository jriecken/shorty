package services

import scala.concurrent.Future
import scala.util.Random

import javax.inject.Singleton
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.{FindAndModify, Update}

/**
 * Service that manages atomically incremented counters in Mongo.
 */
trait CounterService {
  /**
   * Atomically increment the specified named counter.
   *
   * @return The new value of the counter (in a Future).
   */
  def increment(counter: String): Future[Long]

  /**
   * Atomically increment the specified named counter a random amount.
   *
   * @param counter The name of the counter.
   * @param amount Increment a random amount between 1 and this number (inclusive).
   * @return The new value of the counter (in a Future).
   */
  def incrementRandom(counter: String, amount: Int): Future[Long]
}

@Singleton
class CounterServiceImpl extends CounterService {
  private def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("counters")

  def increment(counter: String): Future[Long] = {
    incrementRandom(counter, 0)
  }

  def incrementRandom(counter: String, amount: Int): Future[Long] = {
    val toIncrement = (if (amount == 0) 1 else Random.nextInt(amount) + 1).toLong

    // Atomically increment the counter using mongo's findAndModify command
    val selector = BSONDocument("_id" -> counter)
    val modifier = BSONDocument("$inc" -> BSONDocument("count" -> toIncrement))
    val command = FindAndModify(
      collection.name,
      selector,
      Update(modifier, fetchNewObject = true),
      upsert = true // Just in case the counter isn't there
    )
    val result = collection.db.command(command).map {
      maybeCount =>
      // Since we're upserting, the counter should never be null, but in case it is, just return the toIncrement value
        maybeCount.flatMap(_.getAs[Long]("count")).getOrElse(toIncrement)
    }
    result
  }
}
