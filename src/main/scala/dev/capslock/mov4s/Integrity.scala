package dev.capslock.mov4s

case class Integrity(hash: String, alg: String) {
  def verify(data: Array[Byte]): Boolean = {
    val digest    = java.security.MessageDigest.getInstance(alg)
    val hashBytes = digest.digest(data)
    hashBytes.map("%02x".format(_)).mkString == hash
  }
}
