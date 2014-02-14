package utils

import java.util.Date

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._

/**
 * Play Json formatter that formats Date objects as ISO-8601 strings
 */
object JsonFormats {
  implicit object DateFormat extends Format[Date] {
    val formatter = ISODateTimeFormat.dateTime()

    def reads(json: JsValue) = JsSuccess(new DateTime(json.as[String]).toDate)
    def writes(date: Date) = JsString(formatter.print(new DateTime(date).withZone(DateTimeZone.UTC)))
  }
}
