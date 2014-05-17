package transport

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.websocket.servlet._
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}
import akka.actor.{Props, ActorRef, ActorSystem}
import actors.{GamesRegistryActor, Endpoint, PlayerActor}
import actors.GamesRegistryActor.Join

class PuzzleServer extends Server {
  implicit val system = ActorSystem()

  val gamesRegistry = system.actorOf(Props[GamesRegistryActor])

  val connector = new ServerConnector(this)
  connector.setPort(8080)
  addConnector(connector)

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  setHandler(context)

  val holderEvents = new ServletHolder("ws-events", new PuzzleServlet)
  context.addServlet(holderEvents, "/ws/game/*")

  val handler = new ResourceHandler
  handler.setBaseResource(Resource.newClassPathResource("web"))
  handler.setDirectoriesListed(true)
  context.setHandler(handler)

  class PuzzleSocket(gameId: String) extends WebSocketAdapter {

    var actor: ActorRef = _

    override def onWebSocketConnect(session: Session) {
      super.onWebSocketConnect(session)
      println(gameId)
      actor = system.actorOf(Props(new PlayerActor(Endpoint(session.getRemote))))
      gamesRegistry ! Join(gameId, actor)
    }

    override def onWebSocketText(message: String) {
      actor ! message
    }

    override def onWebSocketClose(statusCode: Int, reason: String) {
      super.onWebSocketClose(statusCode, reason)
      System.out.println("Socket Closed: [" + statusCode + "] " + reason)
    }

    override def onWebSocketError(cause: Throwable) {
      super.onWebSocketError(cause)
      cause.printStackTrace(System.err)
    }
  }

  class PuzzleServlet extends WebSocketServlet {

    def configure(factory: WebSocketServletFactory) {
      factory.setCreator(new WebSocketCreator {
        def createWebSocket(req: ServletUpgradeRequest, resp: ServletUpgradeResponse): AnyRef = {
          new PuzzleSocket(req.getRequestPath.split('/').last)
        }
      })
    }
  }
}

object PuzzleServer extends App {

  val server = new PuzzleServer

  try {
    server.start()
    server.join()
  } catch {
    case t: Throwable =>
      t.printStackTrace(System.err)
  }
}







