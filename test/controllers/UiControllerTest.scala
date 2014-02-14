package controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class UiControllerTest extends Specification {

  "UiController" should {
    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Shorty")
    }
  }
}
