package controllers

import scala.concurrent.Future

import javax.inject.{Inject, Singleton}
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.UrlShorteningService

@Singleton
class UiController @Inject() (urlShorteningService: UrlShorteningService) extends BaseController {
  /**
   * GET /
   *
   * Show the URL shortening form
   */
  def index = ShortyAction {
    Ok(views.html.index())
  }

  /**
   * GET /docs/api
   *
   * Show the API Docs
   */
  def api = ShortyAction {
    val shortDomain = Play.application.configuration.getString("application.shortDomain").get
    Ok(views.html.api(shortDomain))
  }

  /**
   * GET /:hash
   *
   * Redirect to a short url (or show the 404 page). Increments the view count
   * for the hash
   */
  def redirect(hash: String) = ShortyAction.async { request =>
    urlShorteningService.load(hash).flatMap { maybeShortUrl =>
      maybeShortUrl.map { shortUrl =>
        urlShorteningService.trackClick(hash).map { _ =>
          Found(shortUrl.long_url)
        }
      }.getOrElse {
        Future.successful(NotFound(views.html.notFound()))
      }
    }
  }

  /**
   * GET /:hash/stats
   *
   * Show a page that has information about how many times a short URL has been clicked.
   */
  def stats(hash: String) = ShortyAction.async { request =>
    urlShorteningService.load(hash).map { maybeShortUrl =>
      maybeShortUrl.map { shortUrl =>
        Ok(views.html.stats(
          shortUrl = generateShortUrlView(shortUrl),
          clickCount = shortUrl.stats.clicks)
        )
      }.getOrElse {
        NotFound(views.html.notFound())
      }
    }
  }
}
