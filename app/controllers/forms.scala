package controllers

import play.api.Play
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import java.util.Date

import models._
import formats._

object forms {
  
  val genreAndYearForm = Form(
    tuple(
      "genre" -> of[Genre.Genre],
      "year" -> text(minLength = 4, maxLength = 4)
    )
  )

  val albumForm = Form(
    tuple(
      "album" -> mapping(
        "id" -> of[anorm.Pk[Long]],
        "name" -> text,
        "artist" -> ignored(0L),
        "releaseDate" -> of[Date],
        "genre" -> of[Genre.Genre],
        "nbVotes" -> ignored(0L),
        "hasCover" -> ignored(false)
      )(Album.apply)(Album.unapply),
      "artist" -> mapping(
        "id" -> ignored(anorm.NotAssigned: anorm.Pk[Long]),
        "name" -> text
      )(Artist.apply)(Artist.unapply)
    )
  )

  val albumFormForXml = Form(
    tuple(
      "album" -> mapping(
        "id" -> ignored(anorm.NotAssigned: anorm.Pk[Long]),
        "name" -> text,
        "artist" -> ignored(0L),
        "release-date" -> of[Date],
        "genre" -> of[Genre.Genre],
        "nbVotes" -> ignored(0L),
        "hasCover" -> ignored(false)
      )(Album.apply)(Album.unapply),
      "artist" -> mapping(
        "id" -> ignored(anorm.NotAssigned: anorm.Pk[Long]),
        "name" -> text
      )(Artist.apply)(Artist.unapply)
    )
  )

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("Invalid username or password", result => result match {
      case (username, password) => (for (
        u <- Play.configuration.getString("application.admin") if u == username;
        p <- Play.configuration.getString("application.adminpwd") if p == password)
          yield true
      ).isDefined
    })
  )

}
