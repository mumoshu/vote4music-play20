import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "vote4music"
    val appVersion      = "1.0"

    resolvers += "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots"

    val appDependencies = Seq(
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
