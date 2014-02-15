package controllers

import scala.concurrent.Future

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

object BaseController {
  val NodeIdHeader = "X-Shorty-NodeId"
  val ResponseTimeHeader = "X-Shorty-ResponseTime"
}

trait BaseController extends Controller {
  import BaseController._

  /**
   * Action wrapper that adds some custom headers
   */
  object ActionWithHeaders extends ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]) = {
      val start = System.currentTimeMillis
      block(request).map { result =>
        result.withHeaders(
          NodeIdHeader -> Play.application.configuration.getString("application.nodeId").get,
          ResponseTimeHeader -> (System.currentTimeMillis - start).toString
        )
      }
    }
  }
}
