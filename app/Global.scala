import java.text.SimpleDateFormat
import play.api._
import play.api.mvc._

import anorm.Id
import java.util.Date

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if (Album.findAll.isEmpty && Artist.findAll.isEmpty) {
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      def date(str: String): Date = dateFormat.parse(str)
      Seq(
        Album(Id(1), "coolAlbum", 1, date("2011-11-12 00:00:00"), Genre.Rock),
        Album(Id(2), "superAlbum", 1, date("2011-10-09 00:00:00"), Genre.Rock)
      ).foreach(Album.create)
      
      Seq(
        Artist(Id(1), "joe")
      ).foreach(Artist.create)
    }
  }
}
