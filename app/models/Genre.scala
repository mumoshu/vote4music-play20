package models

object Genre extends Enumeration(0) {
  type Genre = Value
  val Rock, Metal, Jazz, Blues, Pop, World, HipHop, Other = Value
}
