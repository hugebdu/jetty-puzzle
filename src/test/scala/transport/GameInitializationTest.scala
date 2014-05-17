package transport

import drivers.{GamesRegistryDriver, WebSocketDriver, ServerSpec}
import org.specs2.specification.Scope
import actors.Messages.{WaitingForPair, UnknownInvitation}
import actors.Messages

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/17/14
 */
class GameInitializationTest extends ServerSpec with GamesRegistryDriver {

  trait ctx extends Scope with WebSocketDriver {
    def playerFor(gameId: String) = clientFor(s"ws://localhost:8080/ws/game/$gameId")
  }

  "Join" should {

    "fail for unknown game" in new ctx {
      val player = playerFor("1")
      player.connect()

      player.messages must containMessage(UnknownInvitation())
    }

    "join single player" in new ctx {
      val id = givenGameInvitation()
      val player = playerFor(id)
      player.connect()

      player.messages must containMessage(WaitingForPair())
    }

    "join two players and start the game" in new ctx {

      val id = givenGameInvitation(imageUrl = "image.gif")

      val player1 = playerFor(id)
      val player2 = playerFor(id)

      player1.connect()
      player2.connect()

      player1.messages must containMessage(Messages.InitGame("image.gif"))
      player2.messages must containMessage(Messages.InitGame("image.gif"))
    }
  }
}
