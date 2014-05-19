package json

import org.specs2.mutable.SpecificationWithJUnit
import actors.Messages.{Swap, InvalidMove, Click}
import JsonSupport._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class JsonSupportTest extends SpecificationWithJUnit {

  "asMessage(json)" should {

    "build POJO" >> {
      import org.json4s.JsonDSL._
      val json = ("type" -> "Click") ~ ("index" -> 25)
      asMessage(json) must beSome(Click(25))
    }
  }

  "Swap" should {

    "serialize" >> {
      Swap(Seq(0 -> 1, 2 -> 3)).toJSONString === """{"type":"Swap","indexes":[{"0":"1"},{"2":"3"}]}"""
    }

    "deserialize" >> {
      asMessage("""{"type":"Swap","indexes":[{"0":"1"},{"2":"3"}]}""") must beSome(Swap(Seq(0 -> 1, 2 -> 3)))
    }
  }

  "InvalidMove message" should {

    "serialize" >> {
      InvalidMove().toJSONString === """{"type":"InvalidMove"}"""
    }

    "deserialize" >> {
      asMessage("""{"type":"InvalidMove"}""") must beSome(InvalidMove())
    }
  }

  "Click message" should {

    "be serialized" >> {
      asString(Click(25)) === """{"type":"Click","index":25}"""
    }

    "deserialized" >> {
      asMessage("""{"type":"Click","index":25}""") must beSome(Click(25))
    }
  }

  "unknown message deserialization" should {

    "be None" >> {

      asMessage("invalid") must beNone
    }
  }
}
