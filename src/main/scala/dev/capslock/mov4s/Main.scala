package dev.capslock.mov4s

@main def hello(): Unit =
  Composer.run(
    Conte(
      Seq(MovieFile(os.pwd / "1.webm")),
      Seq(None),
      Some(
        Integrity(
          "2f844f578ffa2ba0040590804aa0420771f3d3b0f2dcc5fc8d2ddf4ced06777f",
          "SHA-256",
        ),
      ),
      Some(concurrent.duration.FiniteDuration(22, concurrent.duration.MINUTES)),
      Some(concurrent.duration.FiniteDuration(41, concurrent.duration.MINUTES)),
    ),
  )
