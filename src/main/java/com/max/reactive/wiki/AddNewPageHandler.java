package com.max.reactive.wiki;

import io.vertx.core.Handler;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;

final class AddNewPageHandler implements Handler<RoutingContext> {

    private final JDBCClient dbClient;

    AddNewPageHandler(JDBCClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String wikiPageId = ctx.pathParam("pageId");

        ctx.response().end("page id: " + wikiPageId);
    }
}
