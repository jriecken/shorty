package controllers

import scala.concurrent.Future

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import services.ShortUrl

object BaseController {
  val NodeIdHeader = "X-Shorty-NodeId"
  val ResponseTimeHeader = "X-Shorty-ResponseTime"
}

trait BaseController extends Controller {
  import BaseController._

  /**
   * Action wrapper that adds some custom headers and handles unexpected exceptions.
   */
  object ShortyAction extends ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]) = {
      val start = System.currentTimeMillis
      block(request).map { result =>
        result.withHeaders(
          NodeIdHeader -> Play.application.configuration.getString("application.nodeId").get,
          ResponseTimeHeader -> (System.currentTimeMillis - start).toString
        )
      }.recover {
        case e: Exception => internalServerError(e.getMessage)
      }
    }
  }

  /**
   * Transforms a short url model to an API response view.
   */
  protected def generateShortUrlView(shortUrl: ShortUrl) = {
    val shortDomain = Play.application.configuration.getString("application.shortDomain").get
    ShortUrlView(
      short_url = s"$shortDomain/${shortUrl._id}",
      hash = shortUrl._id,
      long_url = shortUrl.long_url,
      created = shortUrl.created
    )
  }

  protected def notFound = NotFound(Json.obj("error" -> "NOT_FOUND"))
  protected def badRequest(reason: String) = BadRequest(Json.obj("error" -> reason))
  protected def internalServerError(reason: String) = InternalServerError(Json.obj("error" -> reason))
}
