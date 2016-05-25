package httpclient.state

import java.net.URL
import java.nio.charset.StandardCharsets
import scala.util.Try
import scalaz._
import skinny.http.{HTTP, Request, Response, Method}
import org.json4s.JValue
import org.json4s.native.JsonMethods

object Http {
 
  private def paramRequest(method: Method, url: String, params: (String, String)*): State[Session, Response] = State[Session, Response] {
    case SomeSession(cookie, lastRes, lastReq, lastUrl) =>
      val next = nextUrl(url, lastUrl)
      val req = Request(next.toString).queryParams(params:_*).header("Cookie", cookie.toString)
      val newSession = invokeRequest(method, req, next)
      (newSession, newSession.lastRes)
    case EmptySession =>
      val req = Request(url).queryParams(params:_*)
      val newSession = invokeRequest(method, req, new URL(url))
      (newSession, newSession.lastRes)
  } 
  
  def get(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.GET, url, params:_*)
  def head(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.HEAD, url, params:_*)
  def post(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.POST, url, params:_*)
  def put(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.PUT, url, params:_*)
  def delete(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.DELETE, url, params:_*)
  def options(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.OPTIONS, url, params:_*)
  def trace(url: String, params: (String, String)*): State[Session, Response] = paramRequest(Method.TRACE, url, params:_*)

  private def bodyRequest(method: Method, url: String, body: String): State[Session, Response] = State[Session, Response] {
    case SomeSession(cookie, lastRes, lastReq, lastUrl) =>
      val next = nextUrl(url, lastUrl)
      val req = Request(next.toString).body(body.getBytes(StandardCharsets.UTF_8)).header("Cookie", cookie.toString)
      val newSession = invokeRequest(method, req, next)
      (newSession, newSession.lastRes)
    case EmptySession =>
      val req = Request(url).body(body.getBytes(StandardCharsets.UTF_8))
      val newSession = invokeRequest(method, req, new URL(url))
      (newSession, newSession.lastRes)
  } 

  def post(url: String, body: String): State[Session, Response] = bodyRequest(Method.POST, url, body)
  def put(url: String, body: String): State[Session, Response] = bodyRequest(Method.PUT, url, body)

  private def jsonRequest(method: Method, url: String, json: JValue): State[Session, Response] = State[Session, Response] {
    case SomeSession(cookie, lastRes, lastReq, lastUrl) =>
      val next = nextUrl(url, lastUrl)
      val req = Request(next.toString).body(toBytes(json), "application/json; charset=utf-8").header("Cookie", cookie.toString)
      val newSession = invokeRequest(method, req, next)
      (newSession, newSession.lastRes)
    case EmptySession =>
      val req = Request(url).body(toBytes(json), "application/json; charset=utf-8")
      val newSession = invokeRequest(method, req, new URL(url))
      (newSession, newSession.lastRes)
  } 

  def post(url: String, json: JValue): State[Session, Response] = jsonRequest(Method.POST, url, json)
  def put(url: String, json: JValue): State[Session, Response] = jsonRequest(Method.PUT, url, json)

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

case class HttpException(mes: String) extends RuntimeException(mes)
