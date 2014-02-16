name := "shorty"

version := "1.0"

scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-Xlint")

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)

play.Project.playScalaSettings
