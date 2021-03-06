package core

import play.api._

import com.wcohen.ss._
import com.wcohen.ss.api._
import com.wcohen.ss.tokens._

/*
  String similarity attempts to examine the similarity of two strings = apple ~ aple - against a specified threshold.
  This caters for spelling errors as well as related strings. The second string library is used, as basic Levenshtein distance measures are insufficient.
  Although Jaro-Winkler is used here, the token based SoftTFDIF is preferable.
  The library supports the concept of a learner function allowing supervisory learning.
  
  http://secondstring.sourceforge.net/doc/iiweb03.pdf
*/

trait SimilarityMetric {
  def calculate(s1: Option[String], s2: Option[String]): BigDecimal
}

object JaroWinkler extends SimilarityMetric {
  def calculate(s1: Option[String], s2: Option[String]): BigDecimal = {
    require(s1.isDefined)
    require(s2.isDefined)

    val tokenizer = new SimpleTokenizer(false, true)
    val distance = new JaroWinkler
    distance.score(s1.getOrElse(""), s2.getOrElse(""))
  }
}
