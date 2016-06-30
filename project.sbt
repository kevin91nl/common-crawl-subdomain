name := "CCSubdomain"

version := "0.0.1"

scalaVersion := "2.10.6"

packAutoSettings

val sparkV = "1.6.2"
val hadoopV = "2.7.1"
val jwatV = "1.0.0"
val guavaV = "19.0"
val jsoupV = "1.7.2"
val warcUtilsV = "1.3"

resolvers += "nl.surfsara" at "http://beehub.nl/surfsara-repo/releases"

libraryDependencies ++= Seq(
    "org.jwat" % "jwat-common" % jwatV,
    "org.jwat" % "jwat-warc" % jwatV,
    "org.jwat" % "jwat-gzip" % jwatV,
    "nl.surfsara" % "warcutils" % warcUtilsV,
    "org.jsoup" % "jsoup" % jsoupV
)

val sparkDependencies = Seq(
    "org.apache.spark" %% "spark-core" % sparkV,
    "org.apache.hadoop" % "hadoop-client" % hadoopV
)

libraryDependencies ++= sparkDependencies.map(_ % "provided")

/*lazy val mainRunner = project.in(file("mainRunner")).dependsOn(RootProject(file("."))).settings(
    libraryDependencies ++= sparkDependencies.map(_ % "compile")
)*/