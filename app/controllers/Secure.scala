package controllers

import play.api._
import play.api.mvc._
import play.api.data._

import forms._

object Secure extends Controller with Secured {

  def login = MayAuthenticated { implicit user => implicit request =>
    Ok(views.html.login(loginForm))
  }

  def doLogin = MayAuthenticated { implicit user => implicit request =>
    loginForm.bindFromRequest.fold(
      formWithError => BadRequest(views.html.login(formWithError)),
      user => Redirect(routes.Application.list).withSession("username" -> user._1)
    )
  }

  def logout() = IsAuthenticated { implicit user =>
    { request =>
      Redirect(routes.Application.index()).withNewSession
    }
  }
  
}
