package json

import org.specs2.mutable.SpecificationWithJUnit
import actors.Messages.{InvalidMove, Click}
import JsonSupport._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class JsonSupportTest extends SpecificationWithJUnit {

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
