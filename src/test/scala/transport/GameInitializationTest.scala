package transport

import drivers.{GameDriver, ServerSpec}
import org.specs2.specification.Scope
import actors.Messages.{WaitingForPair, UnknownInvitation}
import actors.Messages


class GameInitializationTest extends ServerSpec with GameDriver {

  trait ctx extends Scope

  "Join" should {

    "fail for unknown game" in new ctx {
      val player = playerFor("1")
      player.connect()

      player.messages must containMessageEventually(UnknownInvitation())
    }

    "join single player" in new ctx {
      val id = givenGameInvitation()
      val player = playerFor(id)
      player.connect()

      player.messages must containMessageEventually(WaitingForPair())
    }

    "join two players and start the game" in new ctx {

      val id = givenGameInvitation(imageUrl = "image.gif")

      val player1 = playerFor(id)
      val player2 = playerFor(id)

      player1.connect()
      player2.connect()

      player1.messages must containMessageEventually(Messages.InitGame("image.gif"))
      player1.messages must containMessageEventually(beAnInstanceOf[Messages.StartGame])

      player2.messages must containMessageEventually(Messages.InitGame("image.gif"))
      player2.messages must containMessageEventually(beAnInstanceOf[Messages.StartGame])
    }
  }
}
