package controllers

import play.api.mvc._

object Helper {
  
  case class Before[T](block: Request[T] => Option[Result]) extends (Request[T] => Option[Result]){
    def apply(request: Request[T]) = block(request)
  }
  
  def ActionWithBeforeInterceptor(action: => Result)(implicit before: Before[AnyContent]): Action[AnyContent] = Action { request =>
    before(request).getOrElse(action)
  }

  def ActionWithBeforeInterceptor(action: Request[AnyContent] => Result)(implicit before: Before[AnyContent]): Action[AnyContent] = Action { request =>
    before(request).getOrElse(action(request))
  }
}
