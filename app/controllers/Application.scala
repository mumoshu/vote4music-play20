package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.Play.current

import models.{Genre, Album, Artist, Room}
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
      form => {
        BadRequest(views.html.index(getYearsToDisplay()))
      },
      { case (genre, year) =>
        val albums = Album.findByGenreAndYear(genre, year)
        Ok(views.html.listByGenreAndYear(genre, year, albums)(user))
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
      form => {
        Logger.debug("error form = %s".format(form))
        Ok(views.html.form(form))
      },
      data => {
        // replace duplicate artist
        val album = Album.saveReplacingDuplicateArtist(data)
        request.body.asMultipartFormData.map { data =>
          // album cover
          data.file("cover").map { cover =>
            play.api.Logger.debug("cover=%s".format(cover))
            val path = "/public/shared/covers/" + album.id
            val newFile = Play.getFile(path)
            //delete old cover if exists
            if (newFile.exists())
              newFile.delete()
            cover.ref.file.renameTo(newFile)
  
            album.copy(hasCover = true)
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
      case t if t startsWith("text/xml")  => saveAlbumXml()(request)
      case t if t startsWith("application/json") => saveAlbumJson()(request)
      case t                           => BadRequest("Content-type %s is not supported.".format(t))
    }.getOrElse(
      BadRequest("Content-type must be %s or %s, but was not given.")
    )
  }

  /**
   * Save album via JSON API
   */
  def saveAlbumJson() = Action { implicit request =>
    import models.JsonFormats._
    import play.api.libs.json._
    import play.api.libs.json.Format._
    request.body.asJson.map(play.api.libs.json.fromJson[(Album, Artist)]).map { data =>
      Album.saveReplacingDuplicateArtist(data)
      Ok
    }.getOrElse(BadRequest("bad req"))
//    albumForm.bindFromRequest.fold(
//      form =>
//        BadRequest(toJson(form.errors.map(_.message))),
//      data => {
//        Album.saveReplacingDuplicateArtist(data)
//        Ok
//      }
//    )
  }

  /**
   * Save album via XML API
   */
  def saveAlbumXml() = Action { implicit request =>
    // parse xml document
    albumFormForXml.bindFromRequest.fold(
      form =>
        BadRequest(toJson(form.errors.map(_.message))),
      // get the album and the artist
      data => {
        Album.saveReplacingDuplicateArtist(data)
        Ok
      }
    )
  }

  def vote() = Action { implicit request =>
    Form("id" -> number).bindFromRequest.fold(
      form =>
        BadRequest,
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

  // WebSocket tests

  def publishEvent() = Action { implicit reqeust =>
    import play.api.data.format.Formats.stringFormat
    Form("message" -> of[String]).bindFromRequest.fold(
      form =>
        BadRequest(form.errors.map(_.message).mkString(", ")),
      message => {
        try {
          Thread.sleep(5000);
        } catch  {
          case e: InterruptedException =>
            Logger.error(e.getMessage());
        }
        Room.publish(message);
        Ok
      }
    )
  }

  def testWebSocket() = MayAuthenticated { implicit user => { implicit request =>
    Ok(views.html.testWebSocket())
  }}

}
