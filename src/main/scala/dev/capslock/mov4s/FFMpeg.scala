package dev.capslock.mov4s

object FFMpeg {
  val integrityAlg = "SHA-256"
  val outputDir    = os.pwd / "output"
  private def ensureOutputDir() = {
    if (!os.exists(outputDir)) {
      os.makeDir.all(outputDir)
    }
  }
  private def renameWithIntegrity(file: os.Path): os.Path = {
    val digest    = java.security.MessageDigest.getInstance(integrityAlg)
    val hashBytes = digest.digest(os.read.bytes(file))
    val hash      = hashBytes.map("%02x".format(_)).mkString
    os.move(file, outputDir / s"$hash.mkv")

    outputDir / s"$hash.mkv"
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

    MovieFile(renameWithIntegrity(tempOutput))
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

    MovieFile(renameWithIntegrity(tempOutput))
  }
}
