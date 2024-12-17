package dev.capslock.mov4s

import scala.concurrent.duration.FiniteDuration

final case class Conte(
    moviePath: Seq[Conte | MovieFile],
    integrities: Seq[Option[Integrity]],
    integrity: Option[Integrity] = None,
    de: Option[FiniteDuration] = None,
    a: Option[FiniteDuration] = None,
    orders: Seq[Order] = Seq.empty,
)
