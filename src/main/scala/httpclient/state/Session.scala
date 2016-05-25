package httpclient.state

import java.net.URL
import skinny.http.{HTTP, Request, Response}

sealed trait Session
object EmptySession extends Session
case class SomeSession(cookie: Cookie, lastRes: Response, lastReq: Request, lastUrl: URL) extends Session

object Session {
  def empty = EmptySession
}