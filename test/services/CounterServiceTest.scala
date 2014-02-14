package services

import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import test.WithMongoApplication

class CounterServiceTest extends Specification with NoTimeConversions {
  "CounterService" should {
    val counterService = new CounterServiceImpl

    "create/increment a counter" in new WithMongoApplication {
      Await.result(counterService.increment("foo"), 1.second) must equalTo(1)
      Await.result(counterService.increment("foo"), 1.second) must equalTo(2)
      Await.result(counterService.increment("foo"), 1.second) must equalTo(3)

      Await.result(counterService.increment("bar"), 1.second) must equalTo(1)
      Await.result(counterService.increment("bar"), 1.second) must equalTo(2)
    }

    "randomly increment a counter" in new WithMongoApplication {
      val c1 = Await.result(counterService.incrementRandom("foo", 10), 1.second)
      c1 must be between(1, 11)
      val c2 = Await.result(counterService.incrementRandom("foo", 10), 1.second)
      c2 must be between(c1, c1 + 11)
    }
  }
}
