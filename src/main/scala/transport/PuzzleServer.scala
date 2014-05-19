package transport

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.websocket.servlet._
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}
import akka.actor.{Props, ActorRef, ActorSystem}
import actors.{GamesRegistryActor, Endpoint, PlayerActor}
import actors.GamesRegistryActor.{CreateInvitation, Join}
import model.Id
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import akka.pattern.ask
import concurrent.Await
import concurrent.duration._
import akka.util.Timeout


class PuzzleServer extends Server {

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(1.second)

  val gamesRegistry = system.actorOf(Props[GamesRegistryActor], "games-registry")

  val connector = new ServerConnector(this)
  connector.setPort(8080)
  addConnector(connector)

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  setHandler(context)

  context.addServlet(new ServletHolder("ws-events", new PuzzleServlet), "/ws/game/*")
  context.addServlet(new ServletHolder(new InvitationsServlet), "/games")

  val handler = new ResourceHandler
  handler.setBaseResource(Resource.newClassPathResource("web"))
  handler.setDirectoriesListed(true)
  context.setHandler(handler)

  class InvitationsServlet extends DefaultServlet {

    override def doPost(request: HttpServletRequest, response: HttpServletResponse): Unit = {
      val id = Await.result((gamesRegistry ? CreateInvitation("image.gif")).mapTo[String], 1.second)
      response.sendRedirect(s"game.html?id=$id")
    }
  }

  class PuzzleSocket(gameId: String) extends WebSocketAdapter {

    var actor: ActorRef = _

    override def onWebSocketConnect(session: Session) {
      super.onWebSocketConnect(session)
      actor = system.actorOf(Props(new PlayerActor(Endpoint(session.getRemote))), s"player-${Id.random()}")
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







