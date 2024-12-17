package dev.capslock.mov4s

import scala.concurrent.duration.FiniteDuration

object FFMpeg {
  val integrityAlg = "SHA-256"
  val outputDir    = os.pwd / "output"
  private def ensureOutputDir() = {
    if (!os.exists(outputDir)) {
      os.makeDir.all(outputDir)
    }
  }
  private def renameWithIntegrity(file: os.Path): (os.Path, Integrity) = {
    val digest    = java.security.MessageDigest.getInstance(integrityAlg)
    val hashBytes = digest.digest(os.read.bytes(file))
    val hash      = hashBytes.map("%02x".format(_)).mkString
    os.move(file, outputDir / s"$hash.mkv")

    val path      = outputDir / s"$hash.mkv"
    val integrity = Integrity(hash, integrityAlg)

    (path, integrity)
  }
  def concatReencoding(files: MovieFile*): MovieFile = {
    ensureOutputDir()
    val inputFragments =
      files.map(_.path.toString()).flatMap(f => List("-i", f))
    val fileCount = files.length
    val filterComplex = (0 until fileCount)
      .flatMap { i =>
        List(s"[$i:v]", s"[$i:a]")
      }
      .mkString(" ") + s"concat=n=$fileCount:v=1:a=1 [v] [a]"
    val mapper =
      List("-map", "[v]", "-map", "[a]")

    val tempOutput = os.temp(suffix = ".mkv")
    val params = List("-y") ++ inputFragments ++ List(
      "-filter_complex",
      filterComplex,
    ) ++ mapper ++ List(tempOutput.toString)

    println(params)

    os.proc("ffmpeg", params)
      .call(stdout = os.Inherit, stderr = os.Inherit, stdin = os.Inherit)

    val (path, integrity) = renameWithIntegrity(tempOutput)
    MovieFile(path)
  }

  def concatNoEncoding(files: MovieFile*): MovieFile = {
    ensureOutputDir()

    val cutFileBody = files.map(f => s"file '${f.path}'").mkString("\n")
    val cutFile     = os.temp(suffix = ".txt")
    os.write.over(cutFile, cutFileBody)

    val tempOutput = os.temp(suffix = ".mkv")
    val params = List(
      "-y",
      "-f",
      "concat",
      "-safe",
      "0",
      "-i",
      cutFile.toString,
      "-c",
      "copy",
      tempOutput.toString,
    )

    os.proc("ffmpeg", params)
      .call(stdout = os.Inherit, stderr = os.Inherit, stdin = os.Inherit)

    val (path, integrity) = renameWithIntegrity(tempOutput)
    MovieFile(path)
  }

  def cut(
      file: MovieFile,
      start: Option[FiniteDuration],
      end: Option[FiniteDuration],
      draft: Boolean = false,
  ): MovieFile = {
    ensureOutputDir()

    val tempOutput = os.temp(suffix = ".mkv")
    val draftFragment =
      if (draft)
        List(
          "-tune",
          "zerolatency",
          "-deadline",
          "good",
          "-cpu-used",
          "5",
          "-row-mt",
          "1",
        )
      else List()

    val startFragment = start match {
      case Some(s) => List("-ss", formatFiniteDuration(s))
      case None    => List()
    }
    val endFragment = end match {
      case Some(e) => List("-to", formatFiniteDuration(e))
      case None    => List()
    }
    val params = List(
      "-y",
    ) ++ startFragment ++ endFragment ++ List(
      "-i",
      file.path.toString(),
      "-c:v",
      "vp9",
    ) ++ draftFragment ++ List(
      tempOutput.toString,
    )

    os.proc("ffmpeg", params)
      .call(stdout = os.Inherit, stderr = os.Inherit, stdin = os.Inherit)

    val (path, integrity) = renameWithIntegrity(tempOutput)
    MovieFile(path)
  }
  def cutNoEncoding(
      file: MovieFile,
      start: FiniteDuration,
      end: FiniteDuration,
  ): MovieFile = {
    ensureOutputDir()

    val tempOutput = os.temp(suffix = ".mkv")
    val params = List(
      "-y",
      "-ss",
      formatFiniteDuration(start),
      "-i",
      file.path.toString(),
      "-to",
      formatFiniteDuration(end),
      "-c",
      "copy",
      tempOutput.toString,
    )

    os.proc("ffmpeg", params)
      .call(stdout = os.Inherit, stderr = os.Inherit, stdin = os.Inherit)

    val (path, integrity) = renameWithIntegrity(tempOutput)
    MovieFile(path)
  }

  private def formatFiniteDuration(duration: FiniteDuration): String = {
    val totalMilliseconds = duration.toMillis

    val hours   = totalMilliseconds / 3600000
    val minutes = (totalMilliseconds % 3600000) / 60000
    val seconds = (totalMilliseconds % 60000) / 1000
    val millis  = totalMilliseconds  % 1000

    f"$hours%02d:$minutes%02d:$seconds%02d.$millis%03d"
  }
}
