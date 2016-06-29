name := "CCSubdomain"
version := "0.0.1"
scalaVersion := "2.10.6"

packAutoSettings

val sparkV = "1.6.2"
val hadoopV = "2.7.1"
val jwatV = "1.0.0"
val guavaV = "19.0"

resolvers += "nl.surfsara" at "http://beehub.nl/surfsara-repo/releases"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % sparkV % "compile",
    "org.apache.hadoop" % "hadoop-client" % hadoopV % "compile",
    "org.jwat" % "jwat-common" % jwatV,
    "org.jwat" % "jwat-warc" % jwatV,
    "org.jwat" % "jwat-gzip" % jwatV,
    "nl.surfsara" % "warcutils" % "1.3"
    //"com.google.guava" % "guava" % guavaV
)
