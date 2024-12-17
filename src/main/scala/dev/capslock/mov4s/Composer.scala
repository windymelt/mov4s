package dev.capslock.mov4s

import dev.capslock.mov4s.FFMpeg.cut
import scala.util.boundary

object Composer {
  def run(conte: Conte): MovieFile = boundary {
    // TODO: if integrity, check cache and verify the integrity
    conte.integrity.foreach { integrity =>
      val fileContent =
        os.read.bytes(os.pwd / "output" / s"${integrity.hash}.mkv")
      if integrity.verify(fileContent) then
        boundary.break(MovieFile(os.pwd / "output" / s"${integrity.hash}.mkv"))
      // integrity does not match. regenerating
    }

    if conte.moviePath.isEmpty then throw new Exception("moviePath is empty")

    // if corresponding integrity is defined, verify the integrity
    val actualMovies =
      conte.moviePath.zip(conte.integrities).map {
        case (moviePath, integrity) =>
          val file = moviePath match
            case m @ MovieFile(path)          => m
            case c0 @ Conte(_, _, _, _, _, _) => run(c0) // TODO: check cache

          if integrity.isDefined then
            val fileContent = os.read.bytes(file.path)
            if !integrity.get.verify(fileContent) then
              throw new Exception("integrity verification failed")

          file
      }

    val isMultiple = conte.moviePath.length > 1
    val needCut    = conte.de.isDefined || conte.a.isDefined

    (isMultiple, needCut) match
      case (false, false) => actualMovies.head
      case (false, true)  => cut(actualMovies.head, conte.de, conte.a)
      case (true, false) =>
        FFMpeg.concatNoEncoding(
          actualMovies*,
        ) // TODO: check whether encoding or not
      case (true, true) =>
        val combined = FFMpeg.concatNoEncoding(
          actualMovies*,
        )
        cut(combined, conte.de, conte.a)

    // if none of de and a is defined, just combine the movie files
    // if de is defined, cut the movie files from de to a
    // if a is defined, cut the movie files from 0 to a
    // before cutting movie, combine the movie
    // if moviePath is just one, return it without combining
  }
}
