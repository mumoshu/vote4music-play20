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
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def list = Action {
    val albums = Album.findAll
    Ok(views.html.list(albums))
  }
  
  def search(filter: String) = Action {
    val albums = Album.findAll(filter)
    Ok(views.html.list(albums))
  }
  
  def listByGenreAndYear(genre: Int, year: String) = Action {
    val albums = Album.findByGenreAndYear(Genre(genre), year)
    Ok(views.html.listByGenreAndYear(genre, year, albums))
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
  
  import forms._

  /**
   * Create album
   */
  def form() = Action {
    Ok(views.html.form(albumForm))
  }

  /**
   * Create or update album
   *
   * @param album
   * @param artist
   * @param cover
   */
  def save() = Action { implicit request =>
    albumForm.bindFromRequest.fold(
      form => Ok(views.html.form(form)),
      { case (cover, album, artist) =>
        request.body.asMultipartFormData.map { data =>
          // replace duplicate artist
          val artistId = Artist.findByName(artist.name).getOrElse(Artist.create(artist)).id.get
          // album cover
          data.file("cover").map { coverFile =>
            val path = "/public/shared/covers/" + album.id
            val newFile = Play.getFile(path)
            //delete old cover if exists
            if (newFile.exists())
              newFile.delete()
            coverFile.ref.file.renameTo(newFile)
  
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

  def login = Action {
    Ok(views.html.login(loginForm))
  }

  def doLogin = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithError => BadRequest(views.html.login(formWithError)),
      user => Redirect(routes.Application.list).withSession("username" -> user._1)
    )
  }
  
}
