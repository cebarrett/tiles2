import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

	val appName = "myApp"
	val appVersion = "0.0.1-SNAPSHOT"

	val appDependencies = Seq(
		"junit" % "junit-dep" % "4.8.2"
	)

	val main = play.Project(appName, appVersion, appDependencies).settings(
		// Add your own project settings here
	)

}
