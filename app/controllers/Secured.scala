package controllers

import play.api._
import play.api.mvc._

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
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
  
  def MayAuthenticated(f: Option[models.User] => Result): Action[AnyContent] = Action { request =>
    val user = username(request).map(models.User.apply _)
    f(user)
  }
  
  def MayAuthenticated(f: (Request[AnyContent], Option[models.User]) => Result): Action[AnyContent] = Action { request =>
    val user = username(request).map(models.User.apply _)
    f(request, user)
  }

}
