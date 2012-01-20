package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.Play.current

import models.{Genre, Album, Artist}
import models.AlbumFormat._

import java.util.Date

object Application extends Controller {

  import forms._

  def index = Action {
    Ok(views.html.index(getYearsToDisplay))
  }
  
  def list = Action {
    val albums = Album.findAll
    Ok(views.html.list(albums))
  }
  
  def search(filter: String) = Action {
    val albums = Album.findAll(filter)
    Ok(views.html.list(albums))
  }
  
  def listByGenreAndYear() = Action { implicit request =>
    genreAndYearForm.bindFromRequest.fold(
      { form =>
        BadRequest(views.html.index(getYearsToDisplay()))
      },
      { case (genre, year) =>
        val albums = Album.findByGenreAndYear(Genre(genre), year)
        Ok(views.html.listByGenreAndYear(genre, year, albums))
      }
    )
  }
  
  def listByApi(genre: Option[Int],  year: Option[String], format: String) = Action { implicit request =>
    val albums = genre.map { g =>
      Album.findByGenre(Genre(g))
    }.getOrElse(
      Album.findAll
    )
    val maybeFiltered = year.map { y =>
      albums.filter(_.releaseYear == y)
    }.getOrElse(
      albums
    )
    format match {
      case "json" => Ok(toJson(albums))
      case "xml" => Ok(views.xml.listByApi(albums))
    }
  }

  /**
   * List artists in xml or json format
   */
  def listArtistsByApi(format: String) = Action {
    import Artist._
    val artists = Artist.findAll()
    if (format == "json")
        Ok(toJson(artists))
    else
      Ok(views.xml.listArtistsByApi(artists))
  }
  
  /**
   * Create album
   */
  def form() = Action {
    Ok(views.html.form(albumForm))
  }

  /**
   * Create or update album
   */
  def save() = Action { implicit request =>
    albumForm.bindFromRequest.fold(
      { form =>
        Logger.debug("error form = %s".format(form))
        Ok(views.html.form(form))
      },
      { case (album, artist) =>
        request.body.asMultipartFormData.map { data =>
          // replace duplicate artist
          val artistId = Artist.findByName(artist.name).getOrElse(Artist.create(artist)).id.get
          // album cover
          data.file("cover").map { cover =>
            val path = "/public/shared/covers/" + album.id
            val newFile = Play.getFile(path)
            //delete old cover if exists
            if (newFile.exists())
              newFile.delete()
            cover.ref.file.renameTo(newFile)
  
            album.copy(hasCover = true)
          }.orElse(Some(album))
            .map(_.copy(artist = artistId))
            .map(Album.save)
        }

        //return to album list
        Redirect(routes.Application.list)
      }
    )

    
  }

  /**
   * Years to display for top albums form
   */
  def getYearsToDisplay(): List[Int] = {
    (Album.getFirstAlbumYear to Album.getLastAlbumYear).reverse.toList
  }

}
