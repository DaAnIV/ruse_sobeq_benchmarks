name := "sobeq"
version := "0.1"
scalaVersion := "2.13.12"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"

// https://mvnrepository.com/artifact/org.json4s/json4s-jackson
libraryDependencies += "org.json4s" %% "json4s-jackson" % "4.0.2"

// https://mvnrepository.com/artifact/org.mozilla/rhino
libraryDependencies += "org.mozilla" % "rhino" % "1.7.13"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major <= 12 =>
      Seq()
    case _ =>
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4" % "test")
  }
}

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assembly / mainClass := Some("BenchmarkRunner")
