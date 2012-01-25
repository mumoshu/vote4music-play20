package models

import play.api._
import libs.json.{JsValue, Format}
import play.api.mvc._
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Artist(id :Pk[Long], name: String)

object Artist {
  
  def findByName(name: String): Option[Artist] = DB.withConnection { implicit connection =>
    SQL(
      "select * from artist where name = {name}"
    ).on(
      'name -> name
    ).as(Artist.simple.singleOpt)
  }
  
  def findAll(): List[Artist] = DB.withConnection { implicit connection =>
    SQL("select * from artist").as(Artist.simple *)
  }
  
  def create(artist: Artist): Artist = DB.withConnection { implicit connection =>
    
    val id: Long = artist.id.getOrElse {
      SQL("select next value for artist_seq").as(scalar[Long].single)
    }
    
    SQL(
      """
        insert into artist values (
          {id}, {name}
        )
      """
    ).on(
      'id -> id,
      'name -> artist.name
    ).executeUpdate()

    artist.copy(id = Id(id))
  }
  
  def replaceDuplicate(artist: Artist): Artist = DB.withConnection { implicit connection =>
    findByName(artist.name).getOrElse(Artist.create(artist))
  }
  
  val simple: RowParser[Artist] = {
    get[Pk[Long]]("artist.id") ~
    get[String]("artist.name") map {
      case id~name => Artist(id, name)
    }
  }
}
