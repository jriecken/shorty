package utils

import org.specs2.mutable._

class Base62EncoderTest extends Specification {
  "Base62Encoder.encode" should {
    "throw an exception if the number is negative" in {
      Base62Encoder.encode(-100) must throwA[IllegalArgumentException]
    }

    "correctly encode Longs to Base62 Strings" in {
      Base62Encoder.encode(0) must equalTo("0")
      Base62Encoder.encode(62) must equalTo("10")
      Base62Encoder.encode(123456) must equalTo("w7e")
      Base62Encoder.encode(Int.MaxValue.toLong) must equalTo("2lkCB1")
      Base62Encoder.encode(Long.MaxValue) must equalTo("aZl8N0y58M7")
    }
  }

  "Base62Encoder.decode" should {
    "throw an exception if the string is empty" in {
      Base62Encoder.decode("") must throwA[IllegalArgumentException]
    }

    "throw an exception if there is a non-Base62 character in the string" in {
      Base62Encoder.decode("123456_8") must throwA[IllegalArgumentException]
    }

    "throw an exception if the string represents a number bigger than Long.MaxValue"in {
      Base62Encoder.decode("aZl8N0y58M8") must throwA[IllegalArgumentException]
    }

    "correctly decode Base62 Strings" in {
      Base62Encoder.decode("0") must equalTo(0)
      Base62Encoder.decode("10") must equalTo(62)
      Base62Encoder.decode("w7e") must equalTo(123456)
      Base62Encoder.decode("2lkCB1") must equalTo(Int.MaxValue)
      Base62Encoder.decode("aZl8N0y58M7") must equalTo(Long.MaxValue)
    }
  }
}
