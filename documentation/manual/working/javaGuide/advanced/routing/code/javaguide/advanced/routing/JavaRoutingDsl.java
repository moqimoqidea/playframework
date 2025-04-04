/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package javaguide.advanced.routing;

import org.junit.Before;
import org.junit.Test;

// #imports
import jakarta.inject.Inject;

import play.api.mvc.AnyContent;
import play.api.mvc.BodyParser;
import play.api.mvc.PlayBodyParsers;
import play.mvc.Http;
import play.routing.Router;
import play.routing.RoutingDsl;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Controller.*;
// #imports

import play.mvc.Result;
import play.test.WithApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.*;

public class JavaRoutingDsl extends WithApplication {

  private RoutingDsl routingDsl;

  @Before
  public void initializeRoutingDsl() {
    this.routingDsl = app.injector().instanceOf(RoutingDsl.class);
  }

  @Test
  public void simple() {
    // #simple
    Router router =
        routingDsl.GET("/hello/:to").routingTo((request, to) -> ok("Hello " + to)).build();
    // #simple

    assertThat(makeRequest(router, "GET", "/hello/world")).isEqualTo("Hello world");
  }

  @Test
  public void fullPath() {
    // #full-path
    Router router =
        routingDsl.GET("/assets/*file").routingTo((request, file) -> ok("Serving " + file)).build();
    // #full-path

    assertThat(makeRequest(router, "GET", "/assets/javascripts/main.js"))
        .isEqualTo("Serving javascripts/main.js");
  }

  @Test
  public void regexp() {
    // #regexp
    Router router =
        routingDsl
            .GET("/api/items/$id<[0-9]+>")
            .routingTo((request, id) -> ok("Getting item " + id))
            .build();
    // #regexp

    assertThat(makeRequest(router, "GET", "/api/items/23")).isEqualTo("Getting item 23");
  }

  @Test
  public void integer() {
    // #integer
    Router router =
        routingDsl
            .GET("/api/items/:id")
            .routingTo((Http.Request request, Integer id) -> ok("Getting item " + id))
            .build();
    // #integer

    assertThat(makeRequest(router, "GET", "/api/items/23")).isEqualTo("Getting item 23");
  }

  @Test
  public void async() {
    // #async
    Router router =
        routingDsl
            .GET("/api/items/:id")
            .routingAsync(
                (Http.Request request, Integer id) ->
                    CompletableFuture.completedFuture(ok("Getting item " + id)))
            .build();
    // #async

    assertThat(makeRequest(router, "GET", "/api/items/23")).isEqualTo("Getting item 23");
  }

  private String makeRequest(Router router, String method, String path) {
    Result result = routeAndCall(app, router, fakeRequest(method, path));
    if (result == null) {
      return null;
    } else {
      return contentAsString(result);
    }
  }

  // #inject
  public class MyComponent {

    private final RoutingDsl routingDsl;

    @Inject
    public MyComponent(RoutingDsl routing) {
      this.routingDsl = routing;
    }
  }
  // #inject

  @Test
  public void createNewRoutingDsl() {
    play.mvc.BodyParser.Default bodyParser =
        app.injector().instanceOf(play.mvc.BodyParser.Default.class);

    // #new-routing-dsl
    RoutingDsl routingDsl = new RoutingDsl(bodyParser);
    // #new-routing-dsl
    Router router =
        routingDsl.GET("/hello/:to").routingTo((request, to) -> ok("Hello " + to)).build();

    assertThat(makeRequest(router, "GET", "/hello/world")).isEqualTo("Hello world");
  }
}
