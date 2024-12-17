package dev.capslock.mov4s

class IntegritySpec extends munit.FunSuite {
  test("integrity") {
    val integrity =
      Integrity(
        "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
        "SHA-256",
      )
    assertEquals(integrity.verify("hello".getBytes), true)
  }
}
