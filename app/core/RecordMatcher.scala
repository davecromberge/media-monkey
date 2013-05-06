package core

import play.api._

import app._
import models._
import repositories._
import scala.concurrent._
/*
  makes use of the categorical matching function as well as the string similarity function defined in the component registry -

  these can be learned using a supervisory approach to produce different measures over time.

*/

class RecordMatcher(customer: Customer, packages: Iterable[Package]) extends ComponentRegistry {
   implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
   lazy val cm = getCategoryMatcher
   lazy val sm = getSimilarityMatcher
   
   def findBestMatch: Future[Package] = {
     Utils.collect[(BigDecimal, Package)](
       packages.map { p => 
          score(customer.attributes.get, p.attributes.get, p)
       }.toSeq
     ).map { packageScores => {
              packageScores.maxBy { case (score, pack) => score } ._2
        }
     } 
   }
 
   def score(xs: Iterable[Attribute], ys: Iterable[Attribute], pack: Package): Future[(BigDecimal, Package)] = {
      
      val result =
      xs.filter(_.value.isDefined)
        .toList
        .sortBy(_.code)
        .zip(ys.filter(_.value.isDefined)
               .toList
               .sortBy(_.code))
        .map { case (attrX, attrY) =>
                 val areSimilar = sm.calculate(attrX.value, attrY.value) >= minStringSimilarity
                 val criteria = Criteria(attrX, areSimilar, attrX.value, attrY.value, xs, ys)
                 cm.weight(criteria)
                   .zip(cm.calculate(criteria))
                   .map { case (w, s) => w * s }
             }
      Utils.collect(result.toSeq)
           .map(_.sum)
           .map(bestScore => 
              bestScore -> pack)
   }
}
 