package models

import play.api._

import reactivemongo.bson._
import reactivemongo.bson.handlers._

case class Package(
  id: Option[String],
  name: String,
  attributes: Option[Seq[Attribute]]
)

object Package {

  implicit object PackageBSONReader extends BSONReader[Package] {
    def fromBSON(document: BSONDocument): Package = {
      require(document != null)

      val doc = document.toTraversable
      Package(
        Option(doc.getAs[BSONObjectID]("_id").get.stringify),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[TraversableBSONArray]("attributes").map(attributes => 
          attributes.toList.map(_.asInstanceOf[BSONDocument].toTraversable)
                           .map(attrDoc =>
                                  Attribute(attrDoc.getAs[BSONString]("code").get.value,
                                            attrDoc.getAs[BSONString]("name").get.value,
                                            Option(attrDoc.getAs[BSONString]("value").get.value))
                               )
        )
      )
    }
  }

  implicit object PackageBSONWriter extends BSONWriter[Package] {
    def toBSON(pack: Package) = {
      require(pack != null)

      var attributes = new AppendableBSONArray
      pack.attributes.get.map (attr =>
        attributes += BSONDocument(
          "code" -> BSONString(attr.code),
          "name" -> BSONString(attr.name),
          "value" -> BSONString(attr.value.getOrElse("")))
      )

      BSONDocument(
        "_id" -> pack.id.map(new BSONObjectID(_)).getOrElse(BSONObjectID.generate),
        "name" -> BSONString(pack.name),
        "attributes" -> attributes
      )
    }
  }
}
