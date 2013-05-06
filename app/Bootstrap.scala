import play.api._
import play.api.Play.{current}
import play.api.libs.concurrent._

import scala.concurrent._
import scala.util.{Random}

import app._
import core._
import models._
import repositories._


object Bootstrap extends ComponentRegistry {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def init: Unit = {
    blocking {
      customerRepo.all.map(customers => 
                                 customers.map ( customer => 
                                   customerRepo.delete(customer.id.get))) 
    }

    blocking {
      packageRepo.all.map(packages => 
                                 packages.map ( pack => 
                                   packageRepo.delete(pack.id.get))) 
    }

    blocking {
      attributeRepo.deleteAll
    }

    blocking {
      attributeRepo.insert(Attribute("entity_type", "Entity type", None))
      attributeRepo.insert(Attribute("industry", "Industry", None))
      attributeRepo.insert(Attribute("offer", "Offer", None))
      attributeRepo.insert(Attribute("compensation", "Compensation", None))
      attributeRepo.insert(Attribute("location", "Location", None))
    }

    blocking {
      generateCustomers(50)
    }
    
    blocking {
      generatePackages(30)
    }
  }

  private def doMatching: Unit = {
    val packages = packageRepo.all

    customerRepo.all.map { customers =>
      customers.map { customer =>
        packages.map { packages =>
          new RecordMatcher(customer, packages).findBestMatch
                                               .map { pack =>
                                                      println("Found package " + pack.name + " for customer " + customer.name)
                                                    }
        }
      }
    }
  }

  private def generateCustomers(num: Int): Unit = {

    val attributes: Future[Seq[Attribute]] = attributeRepo.all
    (1 to num).map { x => 
      val customerAttributes = attributes.map (attrs =>
        attrs.map { 
          case Attribute("entity_type", name, _) => 
            Attribute("entity_type", name, Option(generate(EntityTypes.list)))
          case Attribute("industry", name, _) => 
            Attribute("industry", name, Option(generate(Industry.list)))
          case Attribute("offer", name, _) => 
            Attribute("offer", name, Option(generate(Offer.list)))
          case Attribute("compensation", name, _) => 
            Attribute("compensation", name, Option(generate(Compensation.list)))
          case Attribute("location", name, _) => 
            Attribute("location", name, Option(generate(Location.list)))
        }
      )

      customerAttributes.map (x => 
        customerRepo.insert(Customer(None, getName, None, Some(x)))
      )
    }
  }

  private def generatePackages(num: Int): Unit = {

    val attributes: Future[Seq[Attribute]] = attributeRepo.all
    (1 to num).map { x => 
      val packageAttributes = attributes.map (attrs =>
        attrs.map { 
          case Attribute("entity_type", name, _) => 
            Attribute("entity_type", name, Option(generate(EntityTypes.list)))
          case Attribute("industry", name, _) => 
            Attribute("industry", name, Option(generate(Industry.list)))
          case Attribute("offer", name, _) => 
            Attribute("offer", name, Option(generate(Offer.list)))
          case Attribute("compensation", name, _) => 
            Attribute("compensation", name, Option(generate(Compensation.list)))
          case Attribute("location", name, _) => 
            Attribute("location", name, Option(generate(Location.list)))
        }
      )

      packageAttributes.map { x => 
        packageRepo.insert(Package(None, generate(Names.list) + " package" + Random.nextInt(Names.list.size).toString, Some(x)))
      }
    }
  }

  private def getName: String = {
    generate(Names.list) + " " + generate(Names.list) + " " + generate(Names2.list) 
  }

  private def generate(list: Seq[Any]): String = {
    list(Random.nextInt(list.size)).toString
  }
}

object Names {
  val list = Seq(
    "Warm",
    "Sky",
    "Frost",
    "Light",
    "Young",
    "Cold",
    "Snow",
    "Ice",
    "Bright",
    "Gold",
    "Mountain"
  )
}

object Names2 {
  val list = Seq(
    "Traders",
    "Investors",
    "Brokers",
    "Retail",
    "Commerce",
    "Retailers",
    "Experts",
    "e-Commerce"
  )
}

object EntityTypes {
  val list = Seq(
    "Individual",
    "Organization"
  )
}

object Industry {
  val list = Seq(
    "Food",
    "Commerce",
    "Online media",
    "Medical"
  )
}

object Offer {
  val list = Seq(
    "Rent",
    "Offer",
    "Lease",
    "Sale"
  )
}

object Compensation {
  val list = Seq(
    "Credit",
    "Cash",
    "Cash and credit"
  )
}

object Location {
  val list = Seq(
    "Global",
    "USA",
    "South Africa",
    "UK"
  )
}
