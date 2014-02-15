package services

import scala.concurrent.Future

import javax.inject.Singleton
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.{FindAndModify, Update}

/**
 * A counter implementation that uses an atomically incremented value in a document in Mongo.
 *
 * The benefit of this counter is that it generates small numbers which translates to shorter
 * urls. The drawback is that it is relatively slow.
 */
@Singleton
class MongoCounterService extends CounterService {
  private def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("counters")

  def nextValue: Future[BigInt] = {
    // Atomically increment the counter using Mongo's findAndModify command
    val selector = BSONDocument("_id" -> "counter")
    val modifier = BSONDocument("$inc" -> BSONDocument("count" -> 1L))
    val command = FindAndModify(
      collection.name,
      selector,
      Update(modifier, fetchNewObject = true),
      upsert = true
    )
    collection.db.command(command).map { maybeCount =>
      // Since we're upserting, the counter should never be null, but in case it is, just return 1
      BigInt(maybeCount.flatMap(_.getAs[Long]("count")).getOrElse(1L))
    }
  }
}
