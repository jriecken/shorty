package utils

import org.specs2.mutable._

class Base62EncoderTest extends Specification {
  "Base62Encoder.encode" should {
    "throw an exception if the number is negative" in {
      Base62Encoder.encode(BigInt(-100)) must throwA[IllegalArgumentException]
    }

    "correctly encode Longs to Base62 Strings" in {
      Base62Encoder.encode(BigInt(0)) must equalTo("0")
      Base62Encoder.encode(BigInt(62)) must equalTo("10")
      Base62Encoder.encode(BigInt(123456)) must equalTo("w7e")
      Base62Encoder.encode(BigInt(Int.MaxValue)) must equalTo("2lkCB1")
      Base62Encoder.encode(BigInt(Long.MaxValue)) must equalTo("aZl8N0y58M7")
    }
  }

  "Base62Encoder.decode" should {
    "throw an exception if the string is empty" in {
      Base62Encoder.decode("") must throwA[IllegalArgumentException]
    }

    "throw an exception if there is a non-Base62 character in the string" in {
      Base62Encoder.decode("123456_8") must throwA[IllegalArgumentException]
    }

    "correctly decode Base62 Strings" in {
      Base62Encoder.decode("0") must equalTo(BigInt(0))
      Base62Encoder.decode("10") must equalTo(BigInt(62))
      Base62Encoder.decode("w7e") must equalTo(BigInt(123456))
      Base62Encoder.decode("2lkCB1") must equalTo(BigInt(Int.MaxValue))
      Base62Encoder.decode("aZl8N0y58M7") must equalTo(BigInt(Long.MaxValue))
    }
  }
}
