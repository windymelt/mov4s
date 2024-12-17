val scala3Version = "3.6.2"

lazy val root = project
  .in(file("."))
  .settings(
    name                                   := "mov4s",
    version                                := "0.1.0-SNAPSHOT",
    scalaVersion                           := scala3Version,
    libraryDependencies += "com.lihaoyi"   %% "os-lib" % "0.11.3",
    libraryDependencies += "org.scalameta" %% "munit"  % "1.0.0" % Test,
  )
