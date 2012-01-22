package controllers

import play.api._
import play.api.mvc._

import models.User

trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("username")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Secure.login)

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => Option[User] => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(Some(User(user)))(request))
  }
  
//  def MayAuthenticated(f: Option[models.User] => Result): Action[AnyContent] = Action { request =>
//    val user = username(request).map(models.User.apply _)
//    f(user)
//  }
//
  def MayAuthenticated(f: Option[User] => Request[AnyContent] => Result): Action[AnyContent] = Action { request =>
    val user = username(request).map(User.apply _)
    f(user)(request)
  }

}
