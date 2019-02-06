package com.max.reactive.wiki;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

final class HealthHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        JsonObject statusObj = new JsonObject();
        statusObj.put("http server", "OK");
        statusObj.put("db", "OK");

        ctx.response().
                putHeader("Content-Type", "application/json").
                end(statusObj.encodePrettily());
    }
}
