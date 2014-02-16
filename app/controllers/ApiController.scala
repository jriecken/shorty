package controllers

import scala.concurrent.Future

import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import services.UrlShorteningService

/**
 * Controller that exposes the URL shortener REST API.
 */
@Singleton
class ApiController @Inject() (urlShorteningService: UrlShorteningService) extends BaseController {
  /**
   * POST /v1/urls
   *
   * Shortens a URL
   */
  def shorten = ShortyAction.async { request =>
    val maybeUrl = request.body.asJson.flatMap(body => (body \ "long_url").asOpt[String])
    maybeUrl.map { url =>
      urlShorteningService.create(url).map { shortUrl =>
        Ok(Json.toJson(generateShortUrlView(shortUrl)))
      }.recover {
        case e: IllegalArgumentException => badRequest(e.getMessage)
      }
    }.getOrElse {
      Future.successful(badRequest("MISSING_URL"))
    }
  }

  /**
   * GET /v1/urls/:hash
   *
   * Expands a URL (does not increment the view count)
   */
  def load(hash: String) = ShortyAction.async { request =>
    urlShorteningService.load(hash).map { maybeShortUrl =>
      maybeShortUrl.map { shortUrl =>
        Ok(Json.toJson(generateShortUrlView(shortUrl)))
      }.getOrElse {
        notFound
      }
    }
  }

  /**
   * GET /v1/urls/:hash/stats
   *
   * Get click stats for a short URL.
   */
  def stats(hash: String) = ShortyAction.async { request =>
    urlShorteningService.load(hash).map { maybeShortUrl =>
      maybeShortUrl.map { shortUrl =>
        Ok(Json.toJson(StatsView(clicks = shortUrl.stats.clicks)))
      }.getOrElse {
        notFound
      }
    }
  }
}
