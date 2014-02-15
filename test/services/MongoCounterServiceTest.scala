package services

import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import test.WithTestApplication

class MongoCounterServiceTest extends Specification with NoTimeConversions {
  val counterService = new MongoCounterService

  "CounterService.nextValue" should {
    "get the next value" in new WithTestApplication(useMongo = true) {
      Await.result(counterService.nextValue, 1.second) must equalTo(BigInt(1))
      Await.result(counterService.nextValue, 1.second) must equalTo(BigInt(2))
      Await.result(counterService.nextValue, 1.second) must equalTo(BigInt(3))
      Await.result(counterService.nextValue, 1.second) must equalTo(BigInt(4))
      Await.result(counterService.nextValue, 1.second) must equalTo(BigInt(5))
    }
  }
}
