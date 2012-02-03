import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "vote4music"
    val appVersion      = "1.0"

    val furyu = Resolver.url("Furyu", url("http://furyu.github.com/repo/"))(Resolver.ivyStylePatterns)

    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
      resolvers += furyu
    )

}
