package models

import play.api.mvc._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Generic._

import anorm._
import anorm.SqlParser._
import java.util.Date

import Genre._
import java.text.SimpleDateFormat

case class Album(id: Pk[Long], name: String, artist: Long, releaseDate: Date, genre: Genre, nbVotes: Long = 0, hasCover: Boolean = false) {

  def copyReplacingArtist(artist: Artist): Album = copy(artist = artist.id.get)
  
  def copyVoted: Album = copy(nbVotes = nbVotes + 1)
  
  def releaseYear: String = Album.formatYear.format(releaseDate)
  
}

object Album {
  
  val formatYear = new SimpleDateFormat("yyyy")

  def findById(id: Long): Option[Album] = DB.withConnection { implicit connection =>
    SQL(
      "select * from album where id = {id}"
    ).on(
      'id -> id
    ).as(Album.simple.singleOpt)
  }

  def findByGenre(genre: Genre): List[(Album, Artist)] = DB.withConnection { implicit connection =>
    SQL(
      """
        select * from album
        join artist on album.artist = artist.id
        where genre = {genre}
      """
    ).on(
      'genre -> genre.id
    ).as((Album.simple ~ Artist.simple) map {
      case album~artist => (album, artist)
    } *)
  }
  
  def delete(album: Album) = DB.withConnection { implicit connection =>
    SQL(
      "delete from album where id = {id}"
    ).on(
      'id -> album.id
    ).executeUpdate()
  }

  def findByName(name: String): Option[Album] = DB.withConnection { implicit connection =>
    SQL(
      "select * from album where name = {name}"
    ).on(
      'name -> name
    ).as(Album.simple.singleOpt)
  }
  
  def findByGenreAndYear(genre: Genre, year: String): List[(Album, Artist)] = DB.withConnection { implicit connection =>
    SQL(
      """
        select * from album
        join artist on album.artist = artist.id
        where genre = {genre} order by nbVotes desc
      """
    ).on(
      'genre -> genre
    ).as(
      Album.simple ~ Artist.simple map {
        case album~artist => (album, artist)
      } *
    ).filter(_._1.releaseYear == year)
  }
  
  def findAll(name: String): List[Album] = DB.withConnection { implicit connection =>
    SQL(
      "select * from album where name like {name} limit {limit}"
    ).on(
      'name -> ("%" + name + "%"),
      'limit -> 100
    ).as(Album.simple *)
  }
  
  def findAll(): List[Album] = DB.withConnection { implicit connection =>
    SQL("select * from album").as(Album.simple *)
  }
  
  def findAllWithArtists(name: String): List[(Album, Artist)] = DB.withConnection { implicit connection =>
    SQL(
      """
        select * from album join artist on album.artist = artist.id
        where album.name like {name} limit {limit}
      """).on(
      'name -> ("%" + name + "%"),
      'limit -> 100
    ).as((Album.simple ~ Artist.simple).map {
      case album~artist => (album, artist)
    } *)
  }

  def findAllWithArtists(): List[(Album, Artist)] = DB.withConnection { implicit connection =>
    SQL("select * from album join artist on album.artist = artist.id").as(
      (Album.simple ~ Artist.simple).map {
        case album~artist => (album, artist)
      } *)
  }

  implicit val genreToStatement = new ToStatement[Genre] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Genre): Unit = s.setInt(index, aValue.id)
  }
  
  def create(album: Album): Album = DB.withConnection { implicit connection =>

    val id: Long = album.id.getOrElse {
      SQL("select next value for album_seq").as(scalar[Long].single)
    }

    SQL(
      """
        insert into album values (
          {id}, {name}, {artist}, {releaseDate}, {genre}, {nbVotes}, {hasCover}
        )
      """
    ).on(
      'id -> id,
      'name -> album.name,
      'artist -> album.artist,
      'releaseDate -> album.releaseDate,
      'genre -> album.genre,
      'nbVotes -> album.nbVotes,
      'hasCover -> album.hasCover
    ).executeUpdate()

    album.copy(id = Id(id))
  }
  
  def update(album: Album): Album = DB.withConnection { implicit connection =>
    SQL(
      """
        update album
        set name = {name}, artist = {artist}, releaseDate = {releaseDate}, genre = {genre}, nbVotes = {nbVotes}, hasCover = {hasCover}
        where id = {id}
      """
    ).on(
      'id -> album.id,
      'name -> album.name,
      'artist -> album.artist,
      'releaseDate -> album.releaseDate,
      'genre -> album.genre,
      'nbVotes -> album.nbVotes,
      'hasCover -> album.hashCode
    ).executeUpdate()

    album
  }

  def save(album: Album): Album = if (album.id.isDefined) update(album) else create(album)
  
  def getFirstAlbumYear: Int = DB.withConnection { implicit connection =>
    SQL(
      "select min(a.releaseDate) from Album a"
    ).as(
      scalar[Date].singleOpt
    ).map { result =>
      formatYear.format(result).toInt
    }.getOrElse(1990)
  }
  
  def getLastAlbumYear: Int = DB.withConnection { implicit connection =>
    SQL(
      "select max(a.releaseDate) from Album a"
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
    get[Int]("album.nbVotes") ~
    get[Boolean]("album.hasCover") map {
      case id~name~artist~releaseDate~genre~nbVotes~hasCover => Album(
        id, name, artist, releaseDate, genre, nbVotes, hasCover
      )
    }
  }
}
