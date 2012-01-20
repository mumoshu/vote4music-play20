package controllers

import play.api._
import play.api.mvc._
import play.api.data._

import forms._

object Secure extends Controller with Secured {

  def login = Action {
    Ok(views.html.login(loginForm))
  }

  def doLogin = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithError => BadRequest(views.html.login(formWithError)),
      user => Redirect(routes.Application.list).withSession("username" -> user._1)
    )
  }

  def logout() = IsAuthenticated { username =>
    { request =>
      Ok(views.html.index(Application.getYearsToDisplay)).discardingCookies("username")
    }
  }
  
}
