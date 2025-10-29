/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.core

import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.Future

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.serialization.jackson.JacksonObjectMapperProvider
import play.api.inject._
import play.libs.Json

/**
 * Module that injects an object mapper to the JSON library on start and on stop.
 *
 * This solves the issue of the ObjectMapper cache from holding references to the application class loader between
 * reloads.
 */
class ObjectMapperModule
    extends SimpleModule(
      bind[ObjectMapper].toProvider[ObjectMapperProvider].eagerly()
    )

object ObjectMapperProvider {
  val BINDING_NAME = "play"
}
@Singleton
class ObjectMapperProvider @Inject() (lifecycle: ApplicationLifecycle, actorSystem: ActorSystem)
    extends Provider[ObjectMapper] {

  private val staticObjectMapperInitialized = new AtomicBoolean(false)

  lazy val get: ObjectMapper = {
    val mapper =
      JacksonObjectMapperProvider
        .get(actorSystem)
        .getOrCreate(ObjectMapperProvider.BINDING_NAME, Option.empty)
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY)
    if (staticObjectMapperInitialized.compareAndSet(false, true)) {
      Json.setObjectMapper(mapper)

      lifecycle.addStopHook { () => Future.successful(Json.setObjectMapper(null)) }
    }
    mapper
  }
}

/**
 * Components for Jackson ObjectMapper and Play's Json.
 */
trait ObjectMapperComponents {
  def actorSystem: ActorSystem
  def applicationLifecycle: ApplicationLifecycle

  lazy val objectMapper: ObjectMapper = new ObjectMapperProvider(applicationLifecycle, actorSystem).get
}
