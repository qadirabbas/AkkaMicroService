
val akkaVersion = "2.5.12"
val akkaHttpVersion = "10.1.5"
val cassandraDriverVersion="3.1.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}
libraryDependencies ++=Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverVersion,
  "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.google.code.gson" % "gson" % "2.8.0",
  "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.6.0"
)