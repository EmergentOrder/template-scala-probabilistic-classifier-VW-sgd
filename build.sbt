import AssemblyKeys._

assemblySettings

name := "template-scala-probabilistic-classifier-VW-sgd"

organization := "io.prediction"

resolvers += Resolver.sonatypeRepo("snapshots")   

libraryDependencies ++= {
   val artifact = "vw-jni-" + sys.props("os.arch")
   val osName = if(sys.props("os.name")=="Mac OS X") "mac" else "Linux"
   Seq(
  "io.prediction"    %% "core"          % "0.9.2" % "provided",
  "org.apache.spark" %% "spark-core"    % "1.3.0" % "provided",
  "org.apache.spark" %% "spark-mllib"   % "1.3.0" % "provided",
  "com.github.EmergentOrder" % artifact % "1.0.4-SNAPSHOT" classifier osName)
}
