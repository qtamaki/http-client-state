## HTTP Client State

The Skinny's HTTP Client Wrapper on CLI.

[HTTP Client Wrapper](https://github.com/ponkotuy/http-client-wrapper) based.

State Monad version http client wrapper tools.

### Usage

```sh
sbt console
```

```scala
get("myfleet.moe").run(Session.empty)._2 // get simple
get("google.com", "q" -> "myfleet").run(Session.empty)._2 // with args
post("hoge.com/session", "username" -> "ponkotuy", "password" -> "*****").run(Session.empty)._2 // post form
(for {
  _ <- post("hoge.com/session", ("username" -> "ponkotuy") ~ ("password" -> "*****")) // post json by using json4s
  r <- get("hoge.com/image/1") // get with cookies
} yield r.status).run(Session.empty)._2
get("myfleet.moe").run(Session.empty)._2 // get raw Response
(for {
  _ <- get("ponkotuy.com")
  r <- get("/index.html") // use host settings
} yield r).run(Session.empty)._2
get("https://google.com", "q" -> "myfleet").run(Session.empty)._2 // https protocol
```

You can use get, post, head, put, delete, options and trace.
