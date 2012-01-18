package controllers

import play.api._
import play.api.mvc._
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._

import models.{Genre, Album}
import models.AlbumFormat._

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
  
}
