package com.max.reactive.wiki.handler;

import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class DeletePageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JDBCClient dbClient;

    public DeletePageHandler(JDBCClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void handle(RoutingContext ctx) {

        String pageId = ctx.request().getParam("id");

        dbClient.getConnection(connAr -> {
            if (connAr.failed()) {
                LOG.error("Error obtaining SQL connection", connAr.cause());
                ctx.fail(connAr.cause());
            }
            else {
                SQLConnection conn = connAr.result();

                conn.updateWithParams(PageDao.SQL_DELETE_PAGE, new JsonArray().add(pageId), deleteRes -> {
                    conn.close();

                    if (deleteRes.failed()) {
                        LOG.error("Can't execute delete page SQL statement", deleteRes.cause());
                        ctx.fail(deleteRes.cause());
                    }
                    else {
                        ctx.response().setStatusCode(303);
                        ctx.response().putHeader("Location", "/wiki");
                        ctx.response().end();
                    }
                });
            }
        });
    }
}
