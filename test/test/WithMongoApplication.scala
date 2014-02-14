package test

import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.execute.{AsResult, Result}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.test._
import play.modules.reactivemongo.ReactiveMongoPlugin

/**
 * Helper that initializes play pointing at a test mongo database. The database is dropped after each test.
 *
 * Logging is turned off.
 */
abstract class WithMongoApplication extends WithApplication(FakeApplication(additionalConfiguration = Map(
  "mongodb.db" -> "shorty-test",
  "logger.root" -> "OFF",
  "logger.application" -> "OFF",
  "logger.play" -> "OFF"
))) {
  override def around[T: AsResult](t: => T): Result = super.around {
    Await.ready(ReactiveMongoPlugin.db.drop(), 1.second)
    t
  }
}


