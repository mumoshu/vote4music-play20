package controllers

import play.api._
import play.api.mvc._

import models.Room

object AsyncController extends Controller {
  
  def testWebSocket() = WebSocket.async[String] { request =>
    Room.join()
  }


}
