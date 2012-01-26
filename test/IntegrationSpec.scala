package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import anorm._

import java.text.SimpleDateFormat

import models._

import fr.javafreelance.fluentlenium.core.filter.FilterConstructor._

object IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in {

      running(TestServer(3333), HTMLUNIT) { browser =>
        val artist = Artist.create(
          Artist(NotAssigned, "joe")
        )

        val album = Album.create(
          Album(NotAssigned, "coolAlbum", artist.id.get, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2010-11-12 00:00:00"), Genre.Rock, 0, false)
        )

        // Open the home page, and check that no error occured
        browser.goTo("http://localhost:3333/")

        browser.$("title").first.getText must not equalTo("Application error")

        browser.goTo("http://localhost:3333/albums")

        browser.$("td").first.getText must equalTo ("coolAlbum")

        browser.$("a", withText("New album")).click()
        browser.$("input", withName("album.name")).text("black album")
        browser.$("input", withName("artist.name")).text("metallica")
        browser.$("input", withName("album.releaseDate")).click()
        browser.$("input", withName("album.releaseDate")).text("1990-01-01")
        browser.$("#saveAlbum").click()
        browser.$("td", withText("metallica")).size must equalTo (1)
        
        // Security test
        browser.goTo("http://localhost:3333/admin/delete?id=1")
        // Check  that login page opens.
        browser.$("title").first.getText must equalTo ("vote4Music - Login")

      }
    }
  }

}
