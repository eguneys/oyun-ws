package oyun.ws
package netty


import com.typesafe.scalalogging.Logger
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.TooLongFrameException
import io.netty.util.AttributeKey
import java.io.IOException
import akka.actor.typed.ActorRef
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.util.concurrent.{ Future => NettyFuture, GenericFutureListener }
import scala.concurrent.{ ExecutionContext, Future, Promise }

final private class ProtocolHandler(
  clients: ActorRef[Clients.Control],
  router: Router,
  ip: IpAddress
)(implicit ec: ExecutionContext)
extends WebSocketServerProtocolHandler(
  "/", // path
  null, // subprotocols ?
  false, // allowExtensions
  2048, // max frame size
  false, // allowMaskMismatch
  true, // checkStartsWith
  true // dropPongFrames
) {

  import ProtocolHandler._
  import Controller.Endpoint

  private val logger = Logger(getClass)

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: java.lang.Object): Unit = {
    evt match {
      case hs: WebSocketServerProtocolHandler.HandshakeComplete =>
        val promise = Promise[Client]
        ctx.channel.attr(key.client).set(promise.future)
        router(
          new util.RequestHeader(hs.requestUri, hs.requestHeaders, ip),
          emitToChannel(ctx.channel)
        ) foreach {
          case Left(status) =>
            terminateConnection(ctx.channel)
            promise failure new Exception(s"Router refused the connection: $status")
          case Right(client) =>
            connectActorToChannel(client, ctx.channel, promise)
        }
      case _ =>
        // logger.info("user event triggered other")
    }
    super.userEventTriggered(ctx, evt)
  }

  private def connectActorToChannel(
    endpoint: Endpoint,
    channel: Channel,
    promise: Promise[Client]
  ): Unit = {
    channel.attr(key.limit).set(endpoint.rateLimit)
    clients ! Clients.Start(endpoint.behavior, promise)
    channel.closeFuture.addListener(new GenericFutureListener[NettyFuture[Void]] {
      def operationComplete(f: NettyFuture[Void]): Unit = 
        Option(channel.attr(key.client).get) match {
          case Some(client) =>
            client foreach { c =>
              clients ! Clients.Stop(c)
            }
          case None =>
        }
    })
  }

  private def emitToChannel(channel: Channel): ClientEmit = in => {
    if (in == ipc.ClientIn.Disconnect) terminateConnection(channel)
    else channel.writeAndFlush(new TextWebSocketFrame(in.write))
  }

  private def terminateConnection(channel: Channel): ChannelFuture =
    channel.writeAndFlush(new CloseWebSocketFrame).addListener(ChannelFutureListener.CLOSE)

  private def sendSimpleErrorResponse(
    channel: Channel,
    status: HttpResponseStatus
  ): ChannelFuture = {
    val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status)
    response.headers.set(HttpHeaderNames.CONNECTION, "close")
    response.headers.set(HttpHeaderNames.CONTENT_LENGTH, "0")
    val f = channel.write(response)
    f.addListener(ChannelFutureListener.CLOSE)
    f
  }


  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = cause match {
    // IO exceptions happen all the time, it usually just means that the client has closed the connection before fully
    // sending/receiving the response.
    case _: IOException =>
      logger.error("wse io")
      //Monitor.websocketError("io")
      ctx.channel.close()
    case _: WebSocketHandshakeException =>
      logger.error("wse handshake")
      //Monitor.websocketError("handshake")
      ctx.channel.close()
    case e: CorruptedWebSocketFrameException
        if Option(e.getMessage).exists(_ startsWith "Max frame length") =>
      logger.error("wse framelength")
      //Monitor.websocketError("frameLength")
    case _: CorruptedWebSocketFrameException =>
      logger.error("wse corrupted")
      //Monitor.websocketError("corrupted")
    case _: TooLongFrameException =>
      logger.error("wse uritoolong")
      //Monitor.websocketError("uriTooLong")
      sendSimpleErrorResponse(ctx.channel, HttpResponseStatus.REQUEST_URI_TOO_LONG)
    case e: IllegalArgumentException
        if Option(e.getMessage).exists(_ contains "Header value contains a prohibited character") =>
      logger.error("wse headerillegal")
      //Monitor.websocketError("headerIllegalChar")
      sendSimpleErrorResponse(ctx.channel, HttpResponseStatus.BAD_REQUEST)
    case _ =>
      logger.error("wse other")
      //Monitor.websocketError("other")
      super.exceptionCaught(ctx, cause)
  }
}

private object ProtocolHandler {
  object key {
    val client = AttributeKey.valueOf[Future[Client]]("client")
    val limit = AttributeKey.valueOf[RateLimit]("limit")
  }
}
