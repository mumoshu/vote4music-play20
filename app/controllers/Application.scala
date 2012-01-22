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

object Application extends Controller with Secured {

  import forms._

  def index() = MayAuthenticated { implicit user => request =>
    Ok(views.html.index(getYearsToDisplay))
  }
  
  def list = MayAuthenticated { implicit user => { implicit request =>
    val albums = Album.findAllWithArtists
    Ok(views.html.list(albums))
  }}
  
  def search(filter: Option[String]) = MayAuthenticated { implicit user => { request =>
    val albums = filter.map(Album.findAllWithArtists _).getOrElse(Album.findAllWithArtists)
    Ok(views.html.list(albums))
  }}
  
  def listByGenreAndYear() = MayAuthenticated { implicit user => { implicit request =>
    genreAndYearForm.bindFromRequest()(request).fold(
      { form =>
        BadRequest(views.html.index(getYearsToDisplay()))
      },
      { case (genre, year) =>
        val albums = Album.findByGenreAndYear(Genre(genre), year)
        Ok(views.html.listByGenreAndYear(Genre(genre), year, albums)(user))
      }
    )
  }}
  
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
  def form() = MayAuthenticated { implicit user => { implicit request =>
    Ok(views.html.form(albumForm))
  }}

  /**
   * Create or update album
   */
  def save() = MayAuthenticated { implicit user =>  { implicit request =>
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

    
  }}
  
  def vote() = Action { implicit request =>
    Form("id" -> number).bindFromRequest.fold(
      form => BadRequest,
      id => {
        Album.findById(id).map(a => a.copy(nbVotes = a.nbVotes + 1)).map(Album.save).map { a =>
          Ok(a.nbVotes.toString)
        }.getOrElse {
          NotFound
        }
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
