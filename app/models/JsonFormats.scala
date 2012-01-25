package models

import play.api.libs.json._
import play.api.libs.json.Generic._

import anorm.{Id, Pk}
import java.util.Date

import models._
import Genre.Genre

object JsonFormats {

  implicit val pkLongFormat = new Format[Pk[Long]] {
    def reads(json: JsValue) = json match {
      case JsNumber(num) => Id(num.longValue)
      case _ => throw new RuntimeException("number expected")
    }

    def writes(o: Pk[Long]) = JsNumber(o.get)
  }

  implicit val dateFormat = new Format[java.util.Date] {
    val f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def reads(json: JsValue) = json match {
      case JsString(str) => f.parse(str)
      case _ => throw new RuntimeException("string expected")
    }

    def writes(o: Date) = JsString(f.format(o))
  }

  implicit val genreFormat = new Format[Genre] {
    def reads(json: JsValue) = json match {
      case JsString(str) => Genre.withName(str)
      case _ => throw new RuntimeException("string expected")
    }

    def writes(o: Genre.Genre) = JsString(o.toString)
  }

  implicit val albumFormat = productFormat7("id", "name", "artist", "releaseDate", "genre", "nbVotes", "hasCover")(Album.apply)(Album.unapply)

  implicit val artistFormat = play.api.libs.json.Generic.productFormat2("id", "name")(Artist.apply)(Artist.unapply)

  implicit val albumWithArtistFormat = new Format[(Album, Artist)] {
    def reads(json: JsValue) = json match {
      case o: JsObject => (
        Album(
          fromJson[Pk[Long]](o \ "id"),
          fromJson[String](o \ "name"),
          fromJson[Long](o \ "artist" \ "id"),
          fromJson[Date](o \ "releaseDate"),
          fromJson[Genre](o \ "genre"),
          fromJson[Int](o \ "nbVotes"),
          fromJson[Boolean](o \ "hasCover")
        ),
        fromJson[Artist](o \ "artist")
      )
      case _ => throw new RuntimeException("object expected")
    }

    def writes(o: (Album, Artist)) = o match {
      case (album, artist) => JsObject(Seq(
        "id" -> toJson(album.id),
        "name" -> toJson(album.name),
        "artist" -> toJson(artist),
        "releaseDate" -> toJson(album.releaseDate),
        "genre" -> toJson(album.genre),
        "nbVotes" -> toJson(album.nbVotes),
        "hasCover" -> toJson(album.hasCover)
      ))
    }
  }

}
