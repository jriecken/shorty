package controllers

import java.util.Date

import play.api.libs.json.Json
import utils.JsonFormats._

case class ShortUrlView(
  short_url: String,
  hash: String,
  long_url: String,
  created: Date
)

object ShortUrlView {
  implicit val shortUrlViewFormat = Json.format[ShortUrlView]
}

case class StatsView(clicks: Long)

object StatsView {
  implicit val countViewFormat = Json.format[StatsView]
}
