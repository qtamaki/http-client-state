## HTTP Client State

The Skinny's HTTP Client Wrapper on CLI.

[HTTP Client Wrapper](https://github.com/ponkotuy/http-client-wrapper) based.

State Monad version http client wrapper tools.

### Usage

```sh
sbt console
```

```scala
run(get("myfleet.moe"))._2 // get simple
run(get("google.com", "q" -> "myfleet"))._2 // with args
run(post("hoge.com/session", "username" -> "ponkotuy", "password" -> "*****"))._2 // post form
run(for {
  _ <- post("hoge.com/session", ("username" -> "ponkotuy") ~ ("password" -> "*****")) // post json by using json4s
  r <- get("hoge.com/image/1") // get with cookies
} yield r.status)._2
run(get("myfleet.moe"))._2 // get raw Response
run(for {
  _ <- get("ponkotuy.com")
  r <- get("/index.html") // use host settings
} yield r)._2
run(get("https://google.com", "q" -> "myfleet")._2 // https protocol
```

You can use get, post, head, put, delete, options and trace.
