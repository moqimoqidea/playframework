/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package javaguide.pekko.typed.oo.multi;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javaguide.pekko.typed.oo.*;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import play.libs.pekko.PekkoGuiceSupport;

public class AppModule extends AbstractModule implements PekkoGuiceSupport {
  @Override
  protected void configure() {
    bindHelloActor("hello-actor1");
    bindHelloActor("hello-actor2");
    bindConfiguredActor("configured-actor1");
    bindConfiguredActor("configured-actor2");
  }

  private void bindHelloActor(String name) {
    bind(new TypeLiteral<ActorRef<HelloActor.SayHello>>() {})
        .annotatedWith(Names.named(name))
        .toProvider(
            new Provider<ActorRef<HelloActor.SayHello>>() {
              @Inject ActorSystem actorSystem;

              @Override
              public ActorRef<HelloActor.SayHello> get() {
                return Adapter.spawn(actorSystem, HelloActor.create(), name);
              }
            })
        .asEagerSingleton();
  }

  private void bindConfiguredActor(String name) {
    bind(new TypeLiteral<ActorRef<ConfiguredActor.GetConfig>>() {})
        .annotatedWith(Names.named(name))
        .toProvider(
            new Provider<ActorRef<ConfiguredActor.GetConfig>>() {
              @Inject ActorSystem actorSystem;
              @Inject Config config;

              @Override
              public ActorRef<ConfiguredActor.GetConfig> get() {
                return Adapter.spawn(actorSystem, ConfiguredActor.create(config), name);
              }
            })
        .asEagerSingleton();
  }
}
