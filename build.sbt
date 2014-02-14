name := "shorty"

version := "0.1"

scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-Xlint")

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta"
)     

play.Project.playScalaSettings
