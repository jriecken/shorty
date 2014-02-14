package utils

import scala.annotation.tailrec

/**
 * Utility for converting numbers to/from a Base 62 representation (i.e. [a-zA-Z0-9])
 */
object Base62Encoder {
  private val Characters = (('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')).mkString
  private val Base = Characters.length
  private val BigIntBase = BigInt(Base)

  /**
   * Encodes a positive Base 10 number into a Base 62 string.
   *
   * @param num The number to encode.
   * @return The number as a Base 62 string.
   * @throws IllegalArgumentException if the number is negative.
   */
  def encode(num: Long): String = {
    if (num < 0) {
      throw new IllegalArgumentException("Can't encode negative numbers")
    }

    // Convert a Base 10 number into a list of Base 62 digits
    @tailrec
    def convertBase(i: Long, acc: List[Int]): List[Int] = {
      val div = i / Base
      val rem = (i % Base).toInt
      if (div == 0) {
        rem :: acc
      } else {
        convertBase(div, rem :: acc)
      }
    }

    // Convert the number and map the Base 62 digits to a character representation of the digit.
    convertBase(num, Nil).map(Characters.charAt).mkString
  }

  /**
   * Decodes a Base 62 string into a positive Base 10 number.
   *
   * @param str The string to decode.
   * @return The Base 10 number that the string represents.
   * @throws IllegalArgumentException if the string is empty, contains a non-Base 62 character,
   *                                  or represents a number larger than Long.MaxValue
   */
  def decode(str: String): Long = {
    if (str.isEmpty) {
      throw new IllegalArgumentException("Can't decode an empty string")
    }

    // Create a list of (character, power of 62)
    val decoded = str.zip(str.indices.reverse).foldLeft(BigInt(0)) { (result, charAndPower) =>
      val (char, power) = charAndPower
      val charValue = Characters.indexOf(char)
      if (charValue < 0) {
        throw new IllegalArgumentException(s"Invalid character: $char")
      } else {
        result + (BigIntBase.pow(power) * BigInt(charValue))
      }
    }

    if (!decoded.isValidLong) {
      throw new IllegalArgumentException("String represents a number larger than Long.MaxValue")
    } else {
      decoded.toLong
    }
  }

}
