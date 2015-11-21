## HTTP Client Wrapper

### Usage

```sh
sbt console
```

```scala
get("myfleet.moe") // get simple
get("google.com", "q" -> "myfleet") // with args
post("hoge.com/session", "username" -> "ponkotuy", "password" -> "*****") // post form
val session = post("hoge.com/session", ("username" -> "ponkotuy") ~ ("password" -> "*****")) // post json from json4s
session.get("hoge.com/image/1") // get with cookies
get("myfleet.moe").res // get raw Response
host = Host("ponkotuy.com")
get("/index.html") // Use host settings
protocol = Protocol("https")
get("google.com", "q" -> "myfleet") // https protocol
```

You can use head, put, delete options and trace.
