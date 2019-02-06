package com.max.reactive.wiki;

import io.vertx.core.Handler;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

final class PagesHandler implements Handler<RoutingContext> {

    private final JDBCClient dbClient;

    public PagesHandler(JDBCClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void handle(RoutingContext ctx) {
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                ctx.response().setStatusCode(500).end("Error: " + ar.cause().getMessage());
            }
            else {
                SQLConnection conn = ar.result();
                conn.query("SELECT * FROM PAGE", selectRes -> {
                    conn.close();

                    if (selectRes.succeeded()) {
                        ctx.response().setStatusCode(200).end(selectRes.result().toJson().encodePrettily());
                    }
                    else {
                        ctx.response().setStatusCode(500).end("Error: " + selectRes.cause().getMessage());
                    }

                });
            }
        });
    }
}
