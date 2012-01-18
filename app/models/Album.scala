package models

import play.api.mvc._
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import java.text.SimpleDateFormat
import java.util.Date

import Genre._

case class Album(id: Pk[Long], name: String, artist: Long, releaseDate: Date, genre: Genre, nbVote: Long = 0, hasCover: Boolean = false) {

  def copyReplacingArtist(artist: Artist): Album = copy(artist = artist.id.get)
  
  def copyVoted: Album = copy(nbVote = nbVote + 1)
  
  def releaseYear: String = Album.formatYear.format(releaseDate)
  
}

object Album {

  val formatYear = new SimpleDateFormat("yyyy")
  
  def findByName(name: String): Option[Album] = DB.withConnection { implicit connection =>
    SQL(
      "select * from album where name = {name}"
    ).on(
      'name -> name
    ).as(Album.simple.singleOpt)
  }
  
  def findByGenreAndYear(genre: Genre, year: String): List[Album] = DB.withConnection { implicit connection =>
    SQL(
      "select * from album where genre = {genre} order by nbVote desc"
    ).on(
      'genre -> genre
    ).as(
      Album.simple *
    ).filter(_.releaseYear == year)
  }
  
  def findAll(name: String): List[Album] = DB.withConnection { implicit connection =>
    SQL(
      "select * from album where name like %{name}% limit {limit}"
    ).on(
      'name -> name,
      'limit -> 100
    ).as(Album.simple *)
  }
  
  def findAll(): List[Album] = DB.withConnection { implicit connection =>
    SQL("select * from album").as(Album.simple *)
  }
  
  def create(album: Album): Album = DB.withConnection { implicit connection =>

    val id: Long = album.id.getOrElse {
      SQL("select next value from album_seq").as(scalar[Long].single)
    }

    SQL(
      """
        insert into album values (
          {id}, {name}, {artist}, {releaseDate}, {genre}, {nbVote}, {hasCover}
      """
    ).on(
      'id -> id,
      'name -> album.name,
      'artist -> album.artist,
      'releaseDate -> album.releaseDate,
      'genre -> album.genre,
      'nbVote -> album.nbVote,
      'hasCover -> album.hasCover
    ).executeUpdate()

    album.copy(id = Id(id))
  }
  
  def getFirstAlbumYear: Int = DB.withConnection { implicit connection =>
    SQL(
      "select max(a.releaseDate) from Album a"
    ).as(
      scalar[Date].singleOpt
    ).map { result =>
      formatYear.format(result).toInt
    }.getOrElse(1990)
  }
  
  def getLastAlbumYear: Int = DB.withConnection { implicit connection =>
    SQL(
      "select miin(a.releaseDate) from Album a"
    ).as(
      scalar[Date].singleOpt
    ).orElse(
      Some(new Date)
    ).map { result =>
      formatYear.format(result).toInt
    }.get
  }
  
  implicit def genreColumn: Column[Genre] = Column.nonNull[Genre] { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case int: Int => Right(Genre(int))
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Genre for column " + qualified))
    }
  }
  
  val simple: RowParser[Album] = {
    get[Pk[Long]]("album.id") ~
    get[String]("album.name") ~
    get[Long]("album.artist") ~
    get[Date]("album.releaseDate") ~
    get[Genre]("album.genre") ~
    get[Int]("album.nbVote") ~
    get[Boolean]("album.hasCover") map {
      case id~name~artist~releaseDate~genre~nbVote~hasCover => Album(
        id, name, artist, releaseDate, genre, nbVote, hasCover
      )
    }
  }
}
