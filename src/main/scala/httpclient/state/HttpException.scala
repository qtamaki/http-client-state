package httpclient.state

case class HttpException(mes: String) extends RuntimeException(mes)
