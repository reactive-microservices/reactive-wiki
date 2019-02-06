package com.max.reactive.wiki.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class AddNewPageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void handle(RoutingContext ctx) {

        String pageName = ctx.request().getParam("name");

        String location = "/wiki/" + pageName;

        if (pageName == null || pageName.isEmpty()) {
            location = "/";
        }
        ctx.response().setStatusCode(303);
        ctx.response().putHeader("Location", location);
        ctx.response().end();
    }
}
