package repositories

import models._

import play.api.Play.current
import play.modules.reactivemongo._ 

import reactivemongo.api._ 
import reactivemongo.bson._
import reactivemongo.core.commands._
import reactivemongo.bson.handlers.DefaultBSONHandlers.{ DefaultBSONReaderHandler, DefaultBSONDocumentWriter }

import scala.concurrent.{ExecutionContext, Future}

trait PackageComponent {
  class PackageRepo extends MongoRepository[Package] with Repository[Package] {

    override val collection = db("packages")
    override implicit val reader = Package.PackageBSONReader
    override implicit val writer = Package.PackageBSONWriter

    def all: Future[List[Package]] = {
      val query = BSONDocument()
      collection.find(query).toList
    }

    def get(id: String): Future[Option[Package]] = {
      require(!id.isEmpty)
      collection.find(BSONDocument("_id" -> new BSONObjectID(id)))
                .headOption
    }

    def delete(id: String): Future[LastError] = {
      require(!id.isEmpty)
      collection.remove(BSONDocument("_id" -> new BSONObjectID(id)))
    }

    def update(id: String, pack: Package): Future[LastError] = {
      require(!id.isEmpty)
      require(!pack.name.isEmpty)

      val modifier = BSONDocument(
        "$set" -> BSONDocument(
            "name" -> BSONString(pack.name)))
          collection.update(BSONDocument("_id" -> new BSONObjectID(id)), modifier)
    }

    def insert(pack: Package): Future[LastError] = {
      require(!pack.name.isEmpty)
      collection.insert(pack)
    }
  }
}
