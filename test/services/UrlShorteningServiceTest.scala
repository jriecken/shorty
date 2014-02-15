package services

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import test.WithTestApplication

import org.mockito.Mockito.reset
import org.specs2.mock.Mockito
import utils.Base62Encoder

class UrlShorteningServiceTest extends Specification with NoTimeConversions with Mockito {
  val counterService = mock[CounterService]
  val urlShorteningService = new UrlShorteningServiceImpl(counterService)

  "UrlShorteningService.create" should {
    "fail if the url is longer than 2048 characters" in {
      val longUrl = "http://example.com/" + (1 to 2000).mkString
      Await.result(urlShorteningService.create(longUrl), 1.second) must throwA(new IllegalArgumentException("URL_TOO_LONG"))
    }

    "fail if the url is not valid" in {
      val invalidUrl = "foo bar baz"
      Await.result(urlShorteningService.create(invalidUrl), 1.second) must throwA(new IllegalArgumentException("INVALID_URL"))
    }

    "shorten a new URL" in new WithTestApplication(useMongo = true) {
      reset(counterService)
      counterService.nextValue returns Future.successful(BigInt(100))

      val urlToShorten = "http://www.example.com"
      val shortUrl = Await.result(urlShorteningService.create(urlToShorten), 2.seconds)
      shortUrl._id must equalTo(Base62Encoder.encode(BigInt(100)))
      shortUrl.long_url must equalTo(urlToShorten)
      shortUrl.stats.clicks must equalTo(0)

      there was one(counterService).nextValue
    }

    "return an existing shortened URL" in new WithTestApplication(useMongo = true) {
      reset(counterService)
      counterService.nextValue returns Future.successful(BigInt(100))

      val urlToShorten = "http://www.example2.com"
      val shortUrl = Await.result(urlShorteningService.create(urlToShorten), 2.seconds)

      there was one(counterService).nextValue

      reset(counterService)

      // Try to create it again, should return exactly the same as we initially shortened
      val newShortUrl = Await.result(urlShorteningService.create(urlToShorten), 2.second)
      newShortUrl must equalTo(shortUrl)

      there was no(counterService).nextValue
    }
  }

  "UrlShorteningService.load" should {
    "return None if the URL does not exist" in new WithTestApplication(useMongo = true) {
      val maybeShortUrl = Await.result(urlShorteningService.load("AAA"), 2.seconds)

      maybeShortUrl must beNone
    }

    "return an existing shortened URL" in new WithTestApplication(useMongo = true) {
      reset(counterService)
      counterService.nextValue returns Future.successful(BigInt(100))

      val urlToShorten = "http://www.example.com"
      val shortUrl = Await.result(urlShorteningService.create(urlToShorten), 2.seconds)
      shortUrl._id must equalTo(Base62Encoder.encode(BigInt(100)))
      shortUrl.long_url must equalTo(urlToShorten)
      shortUrl.stats.clicks must equalTo(0)

      there was one(counterService).nextValue

      val maybeShortUrl = Await.result(urlShorteningService.load(shortUrl._id), 2.seconds)
      maybeShortUrl must beSome(shortUrl)
    }
  }

  "UrlShorteningService.trackClick" should {
    "do nothing for a nonexisting short URL" in new WithTestApplication(useMongo = true) {
      val changed = Await.result(urlShorteningService.trackClick("AAAA"), 2.seconds)
      changed must beFalse
    }

    "increment the count of an existing short URL" in new WithTestApplication(useMongo = true) {
      reset(counterService)
      counterService.nextValue returns Future.successful(BigInt(100))

      val urlToShorten = "http://www.example.com"
      val shortUrl = Await.result(urlShorteningService.create(urlToShorten), 2.seconds)

      there was one(counterService).nextValue

      // Click the link a few times
      Await.result(urlShorteningService.trackClick(shortUrl._id), 2.seconds) must beTrue
      Await.result(urlShorteningService.trackClick(shortUrl._id), 2.seconds) must beTrue
      Await.result(urlShorteningService.trackClick(shortUrl._id), 2.seconds) must beTrue
      Await.result(urlShorteningService.trackClick(shortUrl._id), 2.seconds) must beTrue

      val loadedUrl = Await.result(urlShorteningService.load(shortUrl._id), 2.seconds)
      loadedUrl must beSome(shortUrl.copy(stats = ShortUrlStats(clicks = 4)))
    }
  }
}
