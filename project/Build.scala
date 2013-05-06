import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "media-monkey"
    val appVersion      = "1.0"
    val scalaVersion    = "2.10.0-9"

    val appDependencies = Seq(
        "reactivemongo" % "reactivemongo_2.9.2" % "0.1-SNAPSHOT",
        "play.modules.reactivemongo" % "play2-reactivemongo_2.9.2" % "0.1-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
      .settings(
        resolvers ++= Seq(
          "sgodbillon" at "https://bitbucket.org/sgodbillon/repository/raw/master/snapshots/",
          Resolver.url("Typesafe Ivy Snapshots", url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns)
        )
      )

}