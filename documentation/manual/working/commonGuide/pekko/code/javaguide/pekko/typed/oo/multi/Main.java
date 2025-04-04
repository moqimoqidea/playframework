/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package javaguide.pekko.typed.oo.multi;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import javaguide.pekko.typed.oo.*;
import org.apache.pekko.actor.typed.ActorRef;

@Singleton
public class Main {
  public final ActorRef<HelloActor.SayHello> helloActor1;
  public final ActorRef<HelloActor.SayHello> helloActor2;
  public final ActorRef<ConfiguredActor.GetConfig> configuredActor1;
  public final ActorRef<ConfiguredActor.GetConfig> configuredActor2;

  @Inject
  public Main(
      @Named("hello-actor1") ActorRef<HelloActor.SayHello> helloActor1,
      @Named("hello-actor2") ActorRef<HelloActor.SayHello> helloActor2,
      @Named("configured-actor1") ActorRef<ConfiguredActor.GetConfig> configuredActor1,
      @Named("configured-actor2") ActorRef<ConfiguredActor.GetConfig> configuredActor2) {
    this.helloActor1 = helloActor1;
    this.helloActor2 = helloActor2;
    this.configuredActor1 = configuredActor1;
    this.configuredActor2 = configuredActor2;
  }
}
