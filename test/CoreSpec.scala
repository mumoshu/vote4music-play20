package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

import java.util.Date
import java.util.Calendar

import models.{Album, Genre}

object CoreSpec extends Specification {

  "Album object" should {

    "finds albums released in 2010" in {

      def date(year: Int, month: Int, day: Int) = {
        val c = Calendar.getInstance
        c.set(year, month, day)
        c.getTime
      }
      def album(name: String, releaseDate: Date) = Album(anorm.NotAssigned, name, 0, releaseDate, Genre.Rock, 0, false)

      val albums = Album.filterByYear(
        List(
          album(name = "album1", releaseDate = date(2010, 1, 1)),
          album(name = "album1", releaseDate = date(2009, 1, 1))
        ),
        "2010"
      )

      albums must be size (1)

    }
  }

}
