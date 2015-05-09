import AssemblyKeys._

assemblySettings

name := "template-scala-parallel-classification"

organization := "io.prediction"

resolvers += Resolver.sonatypeRepo("snapshots")   

libraryDependencies ++= Seq(
  "io.prediction"    %% "core"          % pioVersion.value % "provided",
  "org.apache.spark" %% "spark-core"    % "1.3.0" % "provided",
  "org.apache.spark" %% "spark-mllib"   % "1.3.0" % "provided",
  "com.github.EmergentOrder" % "vw-jni-3.16.0-36-generic-amd64" % "1.0.4-SNAPSHOT")
