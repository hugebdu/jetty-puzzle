package transport

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.websocket.servlet.{WebSocketServletFactory, WebSocketServlet}
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}
import model.{Size, Board}
import org.slf4j.LoggerFactory

class PuzzleServer extends Server {
  val connector = new ServerConnector(this)
  connector.setPort(8080)
  addConnector(connector)

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  setHandler(context)

  val holderEvents = new ServletHolder("ws-events", classOf[PuzzleServlet])
  context.addServlet(holderEvents, "/ws/*")

  val handler = new ResourceHandler
  handler.setBaseResource(Resource.newClassPathResource("web"))
  handler.setDirectoriesListed(true)
  context.setHandler(handler)
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

class PuzzleSocket extends WebSocketAdapter {

  implicit val size = Size(4)

  val board = Board.create()

  override def onWebSocketConnect(session: Session) {
    super.onWebSocketConnect(session)
    System.out.println("Socket Connected: " + session)
  }

  override def onWebSocketText(message: String) {
    super.onWebSocketText(message)
    System.out.println("Received TEXT message: " + message)
    LoggerFactory.getLogger(getClass).info("Received TEXT message: " + message)
//    getRemote.sendString(board.prettyString)
    getRemote.sendString("world")
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
    factory.register(classOf[PuzzleSocket])
  }
}





