import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.data.FormError

import models._
import anorm.{Id, Pk}

// Used for binding Forms from HTTP requests
package object formats {

  implicit def genreFormat = new Formatter[Genre.Genre] {

    override val format = Some("format.numeric", Nil)

    def bind(key: String, data: Map[String, String]) = {
      intFormat.bind(key, data).right.flatMap {
        i =>
          scala.util.control.Exception.allCatch[Genre.Genre]
            .either(Genre(i))
            .left.map(e => Seq(FormError(key, "error.number", Nil)))
      }
    }

    def unbind(key: String, value: Genre.Genre) = Map(key -> value.id.toString)
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
