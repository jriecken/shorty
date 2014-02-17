package controllers

import org.specs2.mutable.Specification
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import test.WithTestApplication

/**
 * This test performs a simple happy-path end-to-end through the entire workflow with no mocks.
 */
class EndToEndIntegrationTest extends Specification with HeaderNames {
  "The application" should {
    "work end-to-end" in new WithTestApplication(useMongo = true) {
      // Shorten a URL
      val shortenResult = route(FakeRequest(POST, "/v1/urls").withBody(Json.obj(
        "long_url" -> "http://www.google.com"
      ))).get
      status(shortenResult) must equalTo(OK)
      val shortenedUrl = contentAsJson(shortenResult).as[ShortUrlView]

      // "Visit" it a few times
      val visit1 = route(FakeRequest(GET, s"/${shortenedUrl.hash}")).get
      status(visit1) must equalTo(FOUND)
      header(LOCATION, visit1) must beSome.which(_ == "http://www.google.com")
      val visit2 = route(FakeRequest(GET, s"/${shortenedUrl.hash}")).get
      status(visit2) must equalTo(FOUND)
      header(LOCATION, visit2) must beSome.which(_ == "http://www.google.com")

      // Retrieve the click stats
      val statsResult = route(FakeRequest(GET, s"/v1/urls/${shortenedUrl.hash}/stats")).get
      status(statsResult) must equalTo(OK)
      contentAsJson(statsResult).as[StatsView] must equalTo(StatsView(clicks = 2))
    }
  }
}
