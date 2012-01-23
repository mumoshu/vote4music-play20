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
import models.JsonFormats._

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
  
  def listByApi(genre: Option[String],  year: Option[String], format: String) = Action { implicit request =>
    val albums = genre.map { g =>
      Album.findByGenre(Genre.withName(g))
    }.getOrElse(
      Album.findAllWithArtists()
    )
    val maybeFiltered = year.map { y =>
      albums.filter(_._1.releaseYear == y)
    }.getOrElse(
      albums
    )
    format match {
      case "json" => Ok(toJson(maybeFiltered))
      case "xml" => Ok(views.xml.listByApi(maybeFiltered))
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
          val updatedAlbum = album.copy(artist = artistId)
          val savedAlbum = updatedAlbum.id match {
            case anorm.NotAssigned => Album.create(updatedAlbum)
            case _ => Album.save(updatedAlbum)
          }
          // album cover
          data.file("cover").map { cover =>
            play.api.Logger.debug("cover=%s".format(cover))
            val path = "/public/shared/covers/" + savedAlbum.id
            val newFile = Play.getFile(path)
            //delete old cover if exists
            if (newFile.exists())
              newFile.delete()
            cover.ref.file.renameTo(newFile)
  
            savedAlbum.copy(hasCover = true)
          }.map(Album.save)

        }

        //return to album list
        Redirect(routes.Application.list)
      }
    )
  }}

  /**
   * Save album via API
   */
  def saveAlbumByApi() = Action { request =>
    request.contentType.map {
      case t if t == ContentTypes.XML  => saveAlbumXml()(request)
      case t if t == ContentTypes.JSON => saveAlbumJson()(request)
      case t                           => BadRequest("Content-type %s is not supported.".format(t))
    }.getOrElse(
      BadRequest("Content-type must be %s or %s, but was not given.")
    )
  }

  /**
   * Save album via JSON API
   */
  def saveAlbumJson() = Action { implicit request =>
    albumForm.bindFromRequest.fold(
      form => BadRequest(toJson(form.errors.map(_.message))),
      { case (album, artist) =>
        Artist.findByName(artist.name).orElse(Some(Artist.create(artist))).map { artist =>
          Album.save(album.copy(artist = artist.id.get))
          Ok
        }.get
      }
    )
  }

  /**
   * Save album via XML API
   */
  def saveAlbumXml() = Action { implicit request =>
    // parse xml document
    albumFormForXml.bindFromRequest.fold(
      form => BadRequest(toJson(form.errors.map(_.message))),
      // get the album and the artist
      { case (album, artist) =>
        Artist.findByName(artist.name).orElse(Some(Artist.create(artist))).map { artist =>
          //save in db
          Album.save(album.copy(artist = artist.id.get))
          Ok
        }.get
      }
    )
  }

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
