package controllers

import play.api.Play
import play.api.Play.current
import play.api.data._
import play.api.data.format._
import play.api.data.format.Formats._

import models._
import formats._

import java.util.Date

object forms {
  
  val genreAndYearForm = Form(
    of(
      "genre" -> number(min = 0, max = Genre.maxId),
      "year" -> text(minLength = 4, maxLength = 4)
    )
  )

  val albumForm = Form(
    of(
//      "cover" -> text,
      "album" -> of(Album.apply _, Album.unapply _)(
        "id" -> of[anorm.Pk[Long]],
        "name" -> text,
        "artist" -> ignored(0L),
        "releaseDate" -> of[Date],
        "genre" -> of[Genre.Genre],
        "nbVote" -> ignored(0L),
        "hasCover" -> ignored(false)
      ),
      "artist" -> of(Artist.apply _, Artist.unapply _)(
        "id" -> ignored(anorm.NotAssigned),
        "name" -> text
      )
    )
  )

  val loginForm = Form(
    of(
      "username" -> text,
      "password" -> text
    ) verifying ("Invalid username or password", result => result match {
      case (username, password) => username == Play.configuration.getString("application.admin") && password == Play.configuration.getString("application.adminpwd")
    })
  )

}
