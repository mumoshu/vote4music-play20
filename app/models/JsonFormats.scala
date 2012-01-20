package models

import play.api.libs.json._
import anorm.{Id, Pk}
import java.util.Date
import Genre.Genre

object JsonFormats {

  implicit val pkLongFormat = new Format[Pk[Long]] {
    def reads(json: JsValue) = json match {
      case JsNumber(num) => Id(num.longValue)
    }

    def writes(o: Pk[Long]) = JsNumber(o.get)
  }

  implicit val dateFormat = new Format[java.util.Date] {
    val f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def reads(json: JsValue) = json match {
      case JsString(str) => f.parse(str)
    }

    def writes(o: Date) = JsString(f.format(o))
  }

  implicit val genreFormat = new Format[Genre] {
    def reads(json: JsValue) = json match {
      case JsNumber(num) => Genre(num.toInt)
    }

    def writes(o: Genre.Genre) = JsNumber(o.id)
  }

}
