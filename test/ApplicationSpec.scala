package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

import anorm.NotAssigned
import java.util.Date

import play.api.mvc._
import play.api.http._

object ApplicationSpec extends Specification {

  import models._

  "Application" should {

    "show the index on /" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val result = controllers.Application.index()(FakeRequest("GET", "/"))

        status(result) must equalTo (OK)
        contentType(result) must be some ("text/html")
        charset(result) must be some ("utf-8")

      }
    }

    "return a list of albums in xml on /api/albums" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val artist = Artist.create(Artist(NotAssigned, "TRIPLE H"))

        Album.create(
          Album(NotAssigned, "HHH", artist.id.get, new Date, Genre.Rock, 0, false)
        )
        
        val result = controllers.Application.listByApi(None, None, "xml")(FakeRequest("GET", "/api/albums.xml"))
        
        status(result) should equalTo (OK)

      }
    }
    
    "replace the duplicate artist" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        def albumNamed(name: String) = Album(NotAssigned, name, 0L, new Date, Genre.Rock, 0L, false)
        def artistNamed(name: String) = Artist(NotAssigned, name)
        
        List(
          albumNamed("coolAlbum")  -> artistNamed("john"),
          albumNamed("coolAlbum2") -> artistNamed ("john")
        ).foreach(
          Album.saveReplacingDuplicateArtist
        )
        
        Artist.findByName("john") must some

      }
    }
    
    "adds an artist and an album on POST /api/album with JSON body" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        def artists = controllers.Application.listArtistsByApi("json")(FakeRequest("GET", "/api/artists.json"))
        def albums  = controllers.Application.listByApi(None, None, "json")(FakeRequest("GET", "/api/albums.json"))
        
        contentAsString(artists) must not contain ("john")
        contentAsString(albums) must not contain ("album1")

        val save = controllers.Application.saveAlbumByApi()(
          FakeRequest(
            "POST",
            "/api/album",
            FakeHeaders(Map(HeaderNames.CONTENT_TYPE -> Seq(play.api.http.ContentTypes.JSON))),
            AnyContentAsJson(play.api.libs.json.Json.parse("{ \"name\":\"album1\", \"artist\":{ \"name\":\"john\" }, \"releaseDate\":\"2010-07-12 00:00:00\", \"genre\":\"Rock\" }"))
          )
        )

        contentAsString(artists) must contain ("john")
        contentAsString(albums) must contain ("album1")

      }
    }
    
    "adds an artist and an album on POST /api/album with XML body" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        def artists = controllers.Application.listArtistsByApi("xml")(FakeRequest("GET", "/api/artists.xml"))
        def albums  = controllers.Application.listByApi(None, None, "xml")(FakeRequest("GET", "/api/albums.xml"))

        contentAsString(artists) must not contain ("john")
        contentAsString(albums) must not contain ("album1")

        val save = controllers.Application.saveAlbumByApi()(
          FakeRequest(
            "POST",
            "/api/album",
            FakeHeaders(Map(HeaderNames.CONTENT_TYPE -> Seq(ContentTypes.XML))),
            AnyContentAsXml(<album><artist><name>john</name></artist><name>album1</name><release-date>2010-07-12 00:00:00</release-date><genre>Rock</genre><nvVotes>0</nvVotes></album>)
          )
        )
        
        contentAsString(artists) must contain ("john")
        contentAsString(albums) must contain ("album1")

      }
    }

  }

}
