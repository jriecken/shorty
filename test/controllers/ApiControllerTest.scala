package controllers

import scala.concurrent.Future

import controllers.BaseController._
import org.mockito.Mockito.reset
import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import services.{ShortUrl, ShortUrlStats, UrlShorteningService}
import test.WithTestApplication

class ApiControllerTest extends Specification with Mockito with HeaderNames {
  val urlShorteningService = mock[UrlShorteningService]
  val controller = new ApiController(urlShorteningService)

  "ApiController.create" should {
    "400 if a url is not provided in the body" in new WithTestApplication {
      reset(urlShorteningService)

      val payload = Json.obj()
      val result = controller.create().apply(FakeRequest(POST, "/v1/urls").withBody(payload))

      status(result) must equalTo(BAD_REQUEST)
      contentAsJson(result).as[Map[String,String]] must equalTo(Map("error" -> "MISSING_URL"))
      header(NodeIdHeader, result) must beSome.which(_ == "1")
    }

    "400 if the creation throws an IllegalArgumentException" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.create(anyString) returns Future.failed(new IllegalArgumentException("INVALID_URL"))

      val payload = Json.obj("long_url" -> "http://www.google.com")
      val result = controller.create().apply(FakeRequest(POST, "/v1/urls").withBody(payload))

      status(result) must equalTo(BAD_REQUEST)
      contentAsJson(result).as[Map[String,String]] must equalTo(Map("error" -> "INVALID_URL"))
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).create("http://www.google.com")
    }

    "Return a shortened URL" in new WithTestApplication {
      reset(urlShorteningService)

      val createdShortUrl = ShortUrl(
        _id = "ABC",
        long_url = "http://www.google.com"
      )
      urlShorteningService.create(anyString) returns Future.successful(createdShortUrl)

      val payload = Json.obj("long_url" -> "http://www.google.com")
      val result = controller.create().apply(FakeRequest(POST, "/v1/urls").withBody(payload))

      val expectedResponse = ShortUrlView(
        short_url = "http://localhost/ABC",
        hash = "ABC",
        long_url = "http://www.google.com",
        created = createdShortUrl.created
      )

      status(result) must equalTo(OK)
      contentAsJson(result).as[ShortUrlView] must equalTo(expectedResponse)
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).create("http://www.google.com")
    }
  }

  "ApiController.load" should {
    "404 if the short URL doesn't exist" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.load(anyString) returns Future.successful(None)

      val result = controller.load("ABC").apply(FakeRequest(GET, "/v1/urls/ABC"))

      status(result) must equalTo(NOT_FOUND)
      contentAsJson(result).as[Map[String,String]] must equalTo(Map("error" -> "NOT_FOUND"))
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }

    "Return an existing shortened URL" in new WithTestApplication {
      reset(urlShorteningService)

      val shortUrl = ShortUrl(
        _id = "ABC",
        long_url = "http://www.google.com"
      )
      urlShorteningService.load(anyString) returns Future.successful(Some(shortUrl))

      val result = controller.load("ABC").apply(FakeRequest(GET, "/v1/urls/ABC"))

      val expectedResponse = ShortUrlView(
        short_url = "http://localhost/ABC",
        hash = "ABC",
        long_url = "http://www.google.com",
        created = shortUrl.created
      )

      status(result) must equalTo(OK)
      contentAsJson(result).as[ShortUrlView] must equalTo(expectedResponse)
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }
  }

  "ApiController.stats" should {
    "404 if the short URL doesn't exist" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.load(anyString) returns Future.successful(None)

      val result = controller.stats("ABC").apply(FakeRequest(GET, "/v1/urls/ABC/stats"))

      status(result) must equalTo(NOT_FOUND)
      contentAsJson(result).as[Map[String,String]] must equalTo(Map("error" -> "NOT_FOUND"))
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }

    "Return stats for an existing shortened URL" in new WithTestApplication {
      reset(urlShorteningService)

      val shortUrl = ShortUrl(
        _id = "ABC",
        long_url = "http://www.google.com",
        stats = ShortUrlStats(clicks = 42)
      )
      urlShorteningService.load(anyString) returns Future.successful(Some(shortUrl))

      val result = controller.stats("ABC").apply(FakeRequest(GET, "/v1/urls/ABC"))

      val expectedResponse = StatsView(clicks = 42)

      status(result) must equalTo(OK)
      contentAsJson(result).as[StatsView] must equalTo(expectedResponse)
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }
  }
}
