package com.tcooper8.net

import unfiltered.netty.{ReceivedMessage, ServerErrorResponse, Http}
import unfiltered.netty.async.Plan
import unfiltered.request._
import unfiltered.response._
import org.slf4j.LoggerFactory
import org.jboss.netty.channel.ChannelHandler

object GetTime extends Plan
	with ServerErrorResponse {

	private[this] val log = LoggerFactory.getLogger(this.getClass())
	private[this] val httpFunction = "/getTime"

	private[this] def debug(o: String) { log.debug(o) }
	private[this] def debug[A](o: A) { log.debug(s"$o") }
	private[this] def error(o: String) { log.error(o) }
	private[this] def error[A](o: A) { log.error(s"$o") }
	private[this] def info(o: String) { log.info(o) }
	private[this] def info[A](o: A) { log.info(s"$o") }

	private[this] def getResponse() =
		Ok ~> ResponseString(s"Hello, the time is ${System.currentTimeMillis()}")

	def intent = {
		case req @ GET(Path(httpfunction)) =>
			req.respond(getResponse())

		case req =>
			info("Got bad request")
			req.respond(BadRequest)
	}
}

case class WebServer(port: Int, handler: ChannelHandler) {
	private[this] val log = LoggerFactory.getLogger(this.getClass())

	def start() = {
		Http(port)
			.handler(handler)
			.run { s =>
					log.info(s"Starting unfiltered app on localhost:${s.port}")
		}
	}
}

object WebServer {
	private[this] val defaultPort = 8080

	def apply() = new WebServer(defaultPort, GetTime)
	def apply(port: Int) = new WebServer(port, GetTime)
}

object WebServerMain {
	def main(argv: Array[String]) {
		val server = WebServer()
		server.start()

		dispatch.Http.shutdown()
	}
}