package repositories

import play.api.Play.current
import play.modules.reactivemongo._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers._
import reactivemongo.core.commands._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Properties

/*
  ReactiveMongo is a scala driver that provides fully non-blocking and asynchronous I/O operations.

  http://reactivemongo.org/

*/

trait MongoRepository[T] {
  implicit val reader: BSONReader[T]
  implicit val writer: BSONWriter[T]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val mongoLabUri = Properties.envOrElse("MONGOLAB_URI", "127.0.0.1:27017")
  val connection = MongoConnection(List(mongoLabUri))
  val db = connection("media-monkey")
  def collection: reactivemongo.api.Collection
}

trait Repository[T] {
  def get(id: String): Future[Option[T]]
  def all: Future[List[T]]
  def delete(id: String): Future[Any]
  def update(id: String, item: T): Future[Any]
  def insert(item: T): Future[Any]
}
