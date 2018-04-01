lazy val root = (project in file("."))
  .settings(
    organization in ThisBuild := "edu.rose",
    scalaVersion in ThisBuild := "2.11.8",
    name := "feature_finder",
    sparkVersion := "2.3.0",

    coverageHighlighting := true,

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "2.3.0" % "provided",
      "org.apache.spark" %% "spark-streaming" % "2.3.0" % "provided",
      "org.apache.spark" %% "spark-sql" % "2.3.0" % "provided",
      "org.apache.spark" %% "spark-mllib" % "2.3.0",

      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "com.holdenkarau" %% "spark-testing-base" % "2.3.0_0.9.0" % "test",
      "org.scalanlp" %% "breeze" % "0.13.2",
      "org.scalanlp" %% "breeze-natives" % "0.13.2",
      "org.scalanlp" %% "breeze-viz" % "0.13.2"
    ),

    resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/"
      // Resolver.sonatypeRepo("public")
  )
