package dev.capslock.mov4s

@main def hello(): Unit =
  FFMpeg.concatNoEncoding(
    MovieFile(os.pwd / "input.mp4"),
    MovieFile(os.pwd / "input.mp4"),
  )
