package transport

import drivers.{GameDriver, ServerSpec}
import org.specs2.specification.Scope
import actors.Messages
import concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/19/14
 */
class MovesTest extends ServerSpec with GameDriver {

  trait ctx extends Scope

  "Click" should {

    "be invalid on illegal click" in new ctx {
      val (player, _) = givenInitializedGame {
        case _: Messages.StartGame => Messages.Click(0)
      }

      player.messages must containMessage(Messages.InvalidMove()).eventually(retries = 10, sleep = 1000.milliseconds)
    }

    "be Swap on legal click" in new ctx {

      val (player, _) = givenInitializedGame {
        case _: Messages.StartGame => Messages.Click(14)
      }

      player.messages must containMessage(Messages.Swap(Seq(14 -> 15))).eventually(retries = 10, sleep = 1000.milliseconds)
    }
  }
}
