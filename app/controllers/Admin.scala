package controllers

import play.api._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.mvc._
import java.util.Date
import models._

object Admin extends Controller with Secured {

  def login() = Action {
    Redirect(routes.Application.list)
  }

  import forms._

//  def login = Action {
//    Ok(views.html.login(loginForm))
//  }
//
//  def authenticate = Action { implicit request =>
//    val (username, password) = loginForm.bindFromRequest.get
//    Redirect(routes.Application.list()).withSession("username" -> username)
//  }
  
  def delete(id: Long) = IsAuthenticated { username => { request =>
    Album.findById(id).map { album =>
      Album.delete(album)
      Redirect(routes.Application.list)
    }.getOrElse(NotFound)
  }}
  
  def form(id: Long) = IsAuthenticated { username => { request =>
    Album.findById(id).map { album =>
      Ok(views.html.form(albumForm.fill(album, null)))
    }.getOrElse(NotFound)
  }}

}
