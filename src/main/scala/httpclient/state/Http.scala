package httpclient.state

import java.net.URL
import java.nio.charset.StandardCharsets
import scala.util.Try
import scalaz.State
import skinny.http.{HTTP, Request, Response, Method}
import org.json4s.JValue
import org.json4s.native.JsonMethods

object Http {
 
  def run[A](st:State[Session,A]):(Session, A) = st.run(Session.empty)
  
  def get(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.GET, url) { req => req.queryParams(params:_*) }
  def head(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.HEAD, url) { req => req.queryParams(params:_*) }
  def post(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.POST, url) { req => req.queryParams(params:_*) }
  def put(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.PUT, url) { req => req.queryParams(params:_*) }
  def delete(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.DELETE, url) { req => req.queryParams(params:_*) }
  def options(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.OPTIONS, url) { req => req.queryParams(params:_*) }
  def trace(url: String, params: (String, String)*): State[Session, Response] = buildRequest(Method.TRACE, url) { req => req.queryParams(params:_*) }

  def post(url: String, body: String): State[Session, Response] = buildRequest(Method.POST, url) { req => req.body(body.getBytes(StandardCharsets.UTF_8)) }
  def put(url: String, body: String): State[Session, Response] = buildRequest(Method.PUT, url) { req => req.body(body.getBytes(StandardCharsets.UTF_8)) }

  def post(url: String, json: JValue): State[Session, Response] = buildRequest(Method.POST, url) { req => req.body(toBytes(json), "application/json; charset=utf-8") }
  def put(url: String, json: JValue): State[Session, Response] = buildRequest(Method.PUT, url) { req => req.body(toBytes(json), "application/json; charset=utf-8") }

  private def buildRequest(method: Method, url: String)(f: Request => Request): State[Session, Response] = State[Session, Response] {
    case SomeSession(cookie, lastRes, lastReq, lastUrl) =>
      val next = nextUrl(url, lastUrl)
      val req = f(Request(next.toString)).header("Cookie", cookie.toString)
      val newSession = invokeRequest(method, req, next)
      (newSession, newSession.lastRes)
    case EmptySession =>
      val init = initUrl(url)
      val req = f(Request(init))
      val newSession = invokeRequest(method, req, new URL(init))
      (newSession, newSession.lastRes)
  } 

  private def invokeRequest(method: Method, req: Request, nextUrl: URL): SomeSession = {
    def f(req: Request): Response = {
      val res = HTTP.request(method, req)
      if(res.status / 100 == 3) {
        res.header("Location").map { loc =>
          f(req.copy(url = loc))
        }.getOrElse(throw new HttpException("Fail redirect"))
      } else res
    }
    val res = f(req)
    println(req.contentType)
    printSummary(res)
    val cookie = res.header("Set-Cookie").map(Cookie.fromStr).getOrElse(Cookie.empty)
    SomeSession(cookie, res, req, nextUrl)
  }

  private def initUrl(url: String): String = {
    if(url.contains("://")) {
      url
    } else {
      "http://" + url
    }
  }
  
  private def nextUrl(url: String, lastUrl: URL): URL = {
    // urlが/で始まっていればサイト内絶対パスそうでなければ最終ページからの相対パス
    def mkList = if(url.startsWith("/")) {
          List(lastUrl.getHost, url.tail)
        } else {
          List(lastUrl.getHost, lastUrl.getPath.tail, url)
        }
     // ://が含まれていれば絶対URL、そうでなければ相対パスと認識
    if(url.contains("://")) { 
      new URL(url)
    } else {
      new URL(List(lastUrl.getProtocol, "://", mkList.mkString("/")).mkString)
    }
  }

  private def printSummary(res: Response): Unit = {
    println(res.header(null).getOrElse(""))
    if(res.asString.length > 100) println(res.asString.take(100) + "...") else println(res.asString)
    println()
  }

  private def toBytes(json: JValue): Array[Byte] = JsonMethods.pretty(JsonMethods.render(json)).getBytes(StandardCharsets.UTF_8)
}

