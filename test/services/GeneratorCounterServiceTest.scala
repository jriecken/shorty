package services

import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import test.WithTestApplication

class GeneratorCounterServiceTest extends Specification with NoTimeConversions {
  "GeneratorCounter.nextValue" should {
    "generate a value that contains the time, node identifier, and an incrementing counter" in new WithTestApplication {
      // WithTestApplication sets the nodeId to 1

      val counterService = new GeneratorCounterService
      Await.result(counterService.nextValueInternal(1000), 10.millis) must equalTo(BigInt(66560))
      Await.result(counterService.nextValueInternal(1000), 10.millis) must equalTo(BigInt(66561))
      Await.result(counterService.nextValueInternal(1000), 10.millis) must equalTo(BigInt(66562))
      Await.result(counterService.nextValueInternal(2000), 10.millis) must equalTo(BigInt(132099))

      val newCounterService = new GeneratorCounterService
      for (i <- 0 to 1023) {
        Await.result(newCounterService.nextValueInternal(1000), 10.millis) must equalTo(BigInt(66560 + i))
      }
      // It should rollover
      Await.result(newCounterService.nextValueInternal(1000), 10.millis) must equalTo(BigInt(66560))
    }
  }
}
