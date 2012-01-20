package models

import play.api.data.format.Formatter
import play.api.data.FormError

object Genre extends Enumeration(0) {
  type Genre = Value
  val Rock, Metal, Jazz, Blues, Pop, World, HipHop, Other = Value
}

