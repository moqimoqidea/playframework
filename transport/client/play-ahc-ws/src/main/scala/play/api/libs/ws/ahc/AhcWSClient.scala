/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.libs.ws.ahc

import org.apache.pekko.stream.Materializer
import play.api.libs.ws.ahc.cache.AhcHttpCache
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSRequest

/**
 * Async WS Client backed by AsyncHttpClient.
 *
 * See https://www.playframework.com/documentation/latest/ScalaWS for documentation.
 */
class AhcWSClient(underlyingClient: StandaloneAhcWSClient) extends WSClient {

  /**
   * The underlying implementation of the client, if any.  You must cast explicitly to the type you want.
   *
   * @tparam T the type you are expecting (i.e. isInstanceOf)
   * @return the backing class.
   */
  override def underlying[T]: T = underlyingClient.underlying[T]

  /**
   * Generates a WS Request.  This is a builder that can be used to build up an
   * HTTP request, finally calling a termination method like `get()` or `execute()`
   * which returns a `Future[WSResponse]`.
   *
   * @param url The base URL to make HTTP requests to.
   * @return a request
   * @throws java.lang.IllegalArgumentException if the URL is invalid.
   */
  @throws[IllegalArgumentException]
  override def url(url: String): WSRequest = {
    AhcWSRequest(underlyingClient.url(url).asInstanceOf[StandaloneAhcWSRequest])
  }

  /** Closes this client, and releases underlying resources. */
  override def close(): Unit = underlyingClient.close()

  /** Return the implementation interface of StandaloneAhcWSClient. */
  def standaloneWSClient: StandaloneWSClient = this.underlyingClient
}

object AhcWSClient {

  /**
   * Convenient factory method that uses a play.api.libs.ws.WSClientConfig value for configuration instead of
   * an [[https://www.javadoc.io/doc/org.asynchttpclient/async-http-client/2.12.3/org/asynchttpclient/AsyncHttpClientConfig.html org.asynchttpclient.AsyncHttpClientConfig]].
   *
   * Typical usage:
   *
   * {{{
   *   implicit val materializer = ...
   *   val client = AhcWSClient()
   *   val request = client.url(someUrl).get()
   *   request.foreach { response =>
   *     doSomething(response)
   *     client.close()
   *   }
   * }}}
   *
   * @param config configuration settings, AhcWSClientConfig() by default
   * @param cache enables HTTP cache-control, None by default
   */
  def apply(config: AhcWSClientConfig = AhcWSClientConfig(), cache: Option[AhcHttpCache] = None)(
      implicit materializer: Materializer
  ): AhcWSClient = {
    new AhcWSClient(StandaloneAhcWSClient(config, cache))
  }
}
