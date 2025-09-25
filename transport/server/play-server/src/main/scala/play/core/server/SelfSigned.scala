/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.core.server

import java.security.cert.X509Certificate
import java.security.KeyStore
import javax.net.ssl._

import org.apache.pekko.annotation.ApiMayChange
import org.slf4j.LoggerFactory
import play.core.server.ssl.FakeKeyStore
import play.core.server.ssl.FakeSSLTools
import play.core.ApplicationProvider
import play.server.api.SSLEngineProvider

/** Contains a statically initialized self-signed certificate. */
// public only for testing purposes
@ApiMayChange object SelfSigned {

  /** The SSLContext and TrustManager associated with the self-signed certificate. */
  lazy val (sslContext, trustManager): (SSLContext, X509TrustManager) = {
    val keyStore: KeyStore = FakeKeyStore.generateKeyStore
    FakeSSLTools.buildContextAndTrust(keyStore)
  }
}

/** An SSLEngineProvider which simply references the values in the SelfSigned object. */
// public only for testing purposes
@ApiMayChange final class SelfSignedSSLEngineProvider(
    serverConfig: ServerConfig,
    appProvider: ApplicationProvider
) extends SSLEngineProvider {
  override def createSSLEngine: SSLEngine = sslContext.createSSLEngine()
  override def sslContext: SSLContext     = SelfSigned.sslContext
}

private[play] object LoggingTrustManager extends X509TrustManager {
  private val logger = LoggerFactory.getLogger("LoggingTrustManager")

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    logger.debug(s"checkClientTrusted for chain = $chain and authType = $authType")
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    logger.debug(s"checkServerTrusted for chain = $chain and authType = $authType")
  }

  override def getAcceptedIssuers: Array[X509Certificate] = {
    logger.debug(s"calling getAcceptedIssuers")
    Array.empty
  }
}
