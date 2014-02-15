package test

import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.execute.{AsResult, Result}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.test._
import play.modules.reactivemongo.ReactiveMongoPlugin

/**
 * Helper that initializes a fake play application, optionally enabling mongo and pointing at a test mongo
 * database.
 *
 * If mongo is enabled, the database is dropped before the body is invoked.
 *
 * Logging is turned off so that ReactiveMongo doesn't spam the console with junk when it is restarted with
 * every test.
 */
abstract class WithTestApplication(useMongo: Boolean = false) extends WithApplication(FakeApplication(
  additionalConfiguration = Map(
    "application.nodeId" -> 1,
    "application.shortDomain" -> "http://localhost",
    "mongodb.db" -> "shorty-test",
    "logger.root" -> "OFF",
    "logger.application" -> "OFF",
    "logger.play" -> "OFF"
  ),
  withoutPlugins = if (useMongo)
    Seq()
  else
    Seq("play.modules.reactivemongo.ReactiveMongoPlugin")
)) {
  override def around[T: AsResult](t: => T): Result = super.around {
    if (useMongo) {
      Await.ready(ReactiveMongoPlugin.db.drop(), 1.second)
    }
    t
  }
}
