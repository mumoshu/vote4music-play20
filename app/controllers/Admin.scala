package controllers

import play.api._
import play.api.mvc._
import models.Album

object Admin extends Controller {

  def login = Action {
    Redirect(routes.Application.list)
    Ok("")
  }
  
  def delete(id: Long) = Action {
    Album.findById(id).map { album =>
      Album.delete(album)
      Redirect(routes.Application.list)
    }.getOrElse(NotFound)
  }
  
  def form(id: Long) = Action {
    Album.findById(id).map { album =>
      Ok(views.html.form(album))
    }.getOrElse(NotFound)
  }

}
