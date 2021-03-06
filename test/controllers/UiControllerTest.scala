package controllers

import scala.concurrent.Future

import BaseController._
import org.mockito.Mockito.reset
import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.http.HeaderNames
import play.api.test._
import play.api.test.Helpers._
import services.{ShortUrl, ShortUrlStats, UrlShorteningService}
import test.WithTestApplication

class UiControllerTest extends Specification with Mockito with HeaderNames {
  val urlShorteningService = mock[UrlShorteningService]
  val controller = new UiController(urlShorteningService)

  "UiController.index" should {
    "render the index page" in new WithTestApplication {
      reset(urlShorteningService)

      val result = controller.index().apply(FakeRequest(GET, "/"))

      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/html")
      contentAsString(result) must contain ("Shorty")
      header(NodeIdHeader, result) must beSome.which(_ == "1")
    }
  }

  "UiController.api" should {
    "render the API docs page" in new WithTestApplication {
      reset(urlShorteningService)

      val result = controller.api().apply(FakeRequest(GET, "/api"))

      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/html")
      contentAsString(result) must contain ("API Docs")
      contentAsString(result) must contain ("http://localhost")
      header(NodeIdHeader, result) must beSome.which(_ == "1")
    }
  }

  "UiController.redirect" should {
    "go to the 404 page if the short URL doesn't exist" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.load(anyString) returns Future.successful(None)

      val result = controller.redirect("ABC").apply(FakeRequest(GET, "/ABC"))

      status(result) must equalTo(NOT_FOUND)
      contentType(result) must beSome.which(_ == "text/html")
      contentAsString(result) must contain("Not Found")
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }

    "302 redirect to the long url associated with the hash" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.load(anyString) returns Future.successful(Some(ShortUrl(
        _id = "ABC",
        long_url = "http://www.google.com"
      )))
      urlShorteningService.trackClick(anyString) returns Future.successful(true)

      val result = controller.redirect("ABC").apply(FakeRequest(GET, "/ABC"))

      status(result) must equalTo(FOUND)
      header(LOCATION, result) must beSome.which(_ == "http://www.google.com")
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
      there was one(urlShorteningService).trackClick("ABC")
    }
  }

  "UiController.stats" should {
    "go to the 404 page if the short URL doesn't exist" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.load(anyString) returns Future.successful(None)

      val result = controller.stats("ABC").apply(FakeRequest(GET, "/ABC/stats"))

      status(result) must equalTo(NOT_FOUND)
      contentType(result) must beSome.which(_ == "text/html")
      contentAsString(result) must contain("Not Found")
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }

    "show a page with stats on the short URL" in new WithTestApplication {
      reset(urlShorteningService)

      urlShorteningService.load(anyString) returns Future.successful(Some(ShortUrl(
        _id = "ABC",
        long_url = "http://www.google.com",
        stats = ShortUrlStats(clicks = 42)
      )))

      val result = controller.stats("ABC").apply(FakeRequest(GET, "/ABC/stats"))

      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/html")
      contentAsString(result) must contain("http://localhost/ABC")
      contentAsString(result) must contain("http://www.google.com")
      contentAsString(result) must contain("42")
      header(NodeIdHeader, result) must beSome.which(_ == "1")

      there was one(urlShorteningService).load("ABC")
    }
  }
}
