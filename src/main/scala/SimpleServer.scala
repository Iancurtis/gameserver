import akka.actor.{ ActorSystem, Actor, Props, ActorLogging, ActorRef, ActorRefFactory }
import akka.io.{Tcp, IO}
import akka.io.Tcp.Received
import akka.util.ByteString
import spray.can.Http
import spray.can.server.UHttp
import spray.can.server.UHttp.Upgraded
import spray.can.websocket
import spray.can.websocket.frame.{ BinaryFrame, TextFrame }
import spray.http.HttpRequest
import spray.can.websocket.{UpgradedToWebSocket, FrameCommandFailed}
import spray.routing.HttpServiceActor

/**
 * Created by woko on 2015/10/7.
 */
object  SimpleServer extends App   {
  final case class Push(msg : String)
  final case class PushToChildren(msg: String)

  object WebSocketServer {
    def props() = Props(classOf[WebSocketServer])
  }

  class WebSocketServer extends Actor with ActorLogging{
    def receive = {
      case Http.Connected(remoteAddress, localAddress) =>
        val serverConnection = sender()
        val conn = context.actorOf(WebSocketWorker.props(serverConnection))
        serverConnection ! Http.Register(conn)
        log.info("new connection")

      case PushToChildren(msg: String) =>
        val children = context.children
        println("pushing to all children : " + msg + " " + children.size)
        children.foreach(ref => ref ! Push(msg))
    }
  }

  object WebSocketWorker {
    def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
  }
  class WebSocketWorker(val serverConnection : ActorRef) extends HttpServiceActor with ActorLogging with websocket.WebSocketServerWorker   {
    override def receive =  handshaking orElse businessLogicNoUpgrade orElse closeLogic

    def businessLogic : Receive = {
      case x @ UpgradedToWebSocket =>
        log.info("start ---")

      case x @ ( text : TextFrame) =>  //_ : BinaryFrame |

        log.info("@@@@@@@@@@@" + text.payload)
        sender() ! x

      case Push(msg) =>
        log.info("send data " + msg)
        send(TextFrame(msg))

      case x @ Tcp.Closed =>
        log.info("close...")

      case x : FrameCommandFailed =>
        log.error("frame command failed", x)

      case x:HttpRequest =>
    }

    def businessLogicNoUpgrade: Receive = {
      implicit val refFactory: ActorRefFactory = context
      runRoute {
        getFromResourceDirectory("webapp")
      }
    }
  }

  def doMain(): Unit = {
    implicit val system = ActorSystem()
    import system.dispatcher

    val server = system.actorOf(WebSocketServer.props(), "websocket")
    IO(UHttp) ! Http.Bind(server, "localhost", 8080)
    while (true) {
      var msg = readLine("Give a message and hit ENTER to push message to the children ...\n")
      server ! PushToChildren(msg)
//      server ! msg
    }

    system.shutdown()
    system.awaitTermination()
  }

  doMain()

}
