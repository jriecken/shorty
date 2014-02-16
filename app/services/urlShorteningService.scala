package services

import java.net.URI
import java.util.Date

import scala.concurrent.Future
import scala.util.Try

import javax.inject.{Inject, Singleton}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.Base62Encoder
import utils.JsonFormats._

/**
 * Data object for Short Url click statistics.
 */
case class ShortUrlStats(
  clicks: Long
)
object ShortUrlStats {
  implicit val shortUrlStatsFormat = Json.format[ShortUrlStats]
}

/**
 * Data object for Short Urls.
 */
case class ShortUrl(
  _id: String, // This is the "hash"
  long_url: String,
  created: Date = new Date,
  stats: ShortUrlStats = ShortUrlStats(0)
)
object ShortUrl {
  implicit val shortUrlFormat = Json.format[ShortUrl]
}

/**
 * Service that manages shortened URLs
 */
trait UrlShorteningService {
  /**
   * Shorten a URL.
   *
   * The future will fail with a IllegalArgumentException if the URL
   * is invalid or is longer than 2048 characters
   *
   * @param url The URL to shorten
   * @return The shortened URL.
   */
  def create(url: String): Future[ShortUrl]

  /**
   * Expand a URL.
   *
   * @param hash The short hash of the URL.
   * @return Information about the shortened URL or None if it does not exist.
   */
  def load(hash: String): Future[Option[ShortUrl]]

  /**
   * Track a click to a short URL.
   *
   * @param hash The short hash of the URL.
   * @return Whether the click was tracked - i.e. true if the short URL exists.
   */
  def trackClick(hash: String): Future[Boolean]
}

@Singleton
class UrlShorteningServiceImpl @Inject() (counterService: CounterService) extends UrlShorteningService {
  private def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("urls")

  private def isValidUrl(url: String): Boolean = {
    Try(new URI(url)).map { uri =>
      val scheme = uri.getScheme.toLowerCase
      uri.isAbsolute && (scheme == "http" || scheme == "https")
    }.getOrElse(false)
  }

  def create(url: String): Future[ShortUrl] = {
    if (url.length > 2048) {
      Future.failed(new IllegalArgumentException("URL_TOO_LONG"))
    } else if (!isValidUrl(url)) {
      Future.failed(new IllegalArgumentException("INVALID_URL"))
    } else {
      collection.find(Json.obj("long_url" -> url)).one[ShortUrl].flatMap { maybeUrl =>
        // Return existing short URL if there is one.
        maybeUrl.map(Future.successful).getOrElse {
          // Otherwise we need to create a new one.
          counterService.nextValue.flatMap { value =>
            val toInsert = ShortUrl(
              _id = Base62Encoder.encode(value),
              long_url = url
            )
            collection.insert(toInsert).map(_ => toInsert)
          }
        }
      }
    }
  }

  def load(hash: String): Future[Option[ShortUrl]] = {
    collection.find(Json.obj("_id" -> hash)).one[ShortUrl]
  }

  def trackClick(hash: String): Future[Boolean] = {
    collection.update(Json.obj("_id" -> hash), Json.obj("$inc" -> Json.obj("stats.clicks" -> 1))).map { lastError =>
      lastError.n == 1
    }
  }
}
