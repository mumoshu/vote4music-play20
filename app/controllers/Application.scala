package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.Play.current

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

  val loginForm = Form(
  of(
  "username" -> text,
  "password" -> text
  ) verifying ("Invalid username or password", result => result match {
    case (username, password) => username == Play.configuration.getString("application.admin") && password == Play.configuration.getString("application.adminpwd")
  })
  )

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
