
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.skinny-framework" %% "skinny-http-client" % "1.3.20",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.scalaz" %% "scalaz-core" % "7.2.2"
)

initialCommands in console :=
  """
  import skinny.http._
  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.native.JsonMethods._
  import httpclient.state._
  import httpclient.state.Http._
  """
