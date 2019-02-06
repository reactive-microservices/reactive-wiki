package com.max.reactive.wiki;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

final class HealthHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().end(new JsonObject().
                put("status", "OK").
                encodePrettily());
    }
}
