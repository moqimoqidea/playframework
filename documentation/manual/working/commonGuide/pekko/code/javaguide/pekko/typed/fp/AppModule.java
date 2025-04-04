/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package javaguide.pekko.typed.fp;

// #fp-app-module
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import jakarta.inject.Inject;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;

public class AppModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(new TypeLiteral<ActorRef<HelloActor.SayHello>>() {})
        .toProvider(HelloActorProvider.class)
        .asEagerSingleton();
    bind(new TypeLiteral<ActorRef<ConfiguredActor.GetConfig>>() {})
        .toProvider(ConfiguredActorProvider.class)
        .asEagerSingleton();
  }

  public static class HelloActorProvider implements Provider<ActorRef<HelloActor.SayHello>> {
    private final ActorSystem actorSystem;

    @Inject
    public HelloActorProvider(ActorSystem actorSystem) {
      this.actorSystem = actorSystem;
    }

    @Override
    public ActorRef<HelloActor.SayHello> get() {
      return Adapter.spawn(actorSystem, HelloActor.create(), "hello-actor");
    }
  }

  public static class ConfiguredActorProvider
      implements Provider<ActorRef<ConfiguredActor.GetConfig>> {

    private final ActorSystem actorSystem;
    private final Config config;

    @Inject
    public ConfiguredActorProvider(ActorSystem actorSystem, Config config) {
      this.actorSystem = actorSystem;
      this.config = config;
    }

    @Override
    public ActorRef<ConfiguredActor.GetConfig> get() {
      return Adapter.spawn(actorSystem, ConfiguredActor.create(config), "configured-actor");
    }
  }
}
// #fp-app-module
