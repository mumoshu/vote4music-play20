package helpers

import models.User

object secure {

  def title = "Please login"
//  def check(username: String): Boolean = true
  def check(user: User, name: String): Boolean = user.name == name

}
