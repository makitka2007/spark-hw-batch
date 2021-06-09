name := "spark-hw-batch"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.1"
libraryDependencies += "io.delta" %% "delta-core" % "0.8.0"
libraryDependencies += "org.apache.spark" %% "spark-avro" % "3.1.1"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

(scalastyleTarget in Compile) := baseDirectory.value / "scalastyle-result.xml"