import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.data.FormError

import models._
import anorm.{Id, Pk}

// Used for binding Forms from HTTP requests
package object formats {

  implicit def genreFormat = new Formatter[Genre.Genre] {

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.flatMap {
        str =>
          scala.util.control.Exception.allCatch[Genre.Genre]
            .either(Genre.withName(str))
            .left.map(e => Seq(FormError(key, "error.genre", Nil)))
      }
    }

    def unbind(key: String, value: Genre.Genre) = Map(key -> value.toString)
  }

  implicit def pkLongFormat = new Formatter[Pk[Long]] {

    override val format = Some("format.numeric", Nil)

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.map {
        str =>
          scala.util.control.Exception.allCatch[Pk[Long]]
            .opt(Id(str.toLong))
            .getOrElse(anorm.NotAssigned)
      }
    }

    def unbind(key: String, value: Pk[Long]) = Map(key -> value.get.toString)

  }

}
