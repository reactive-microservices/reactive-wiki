package com.max.reactive.wiki.handler;

import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class SavePageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JDBCClient dbClient;

    public SavePageHandler(JDBCClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void handle(RoutingContext ctx) {

        String id = ctx.request().getParam("id");
        String title = ctx.request().getParam("title");
        String markdown = ctx.request().getParam("markdown");

        String sql = isNewPage(ctx) ? PageDao.SQL_INSERT_PAGE : PageDao.SQL_UPDATE_PAGE;
        JsonArray params = new JsonArray();

        if (isNewPage(ctx)) {
            params.add(title).add(markdown);
        }
        else {
            params.add(markdown).add(id);
        }

        dbClient.updateWithParams(sql, params, resultSet -> {
            if (resultSet.failed()) {
                LOG.error("Error", resultSet.cause());
                ctx.fail(resultSet.cause());
            }
            else {
                ctx.response().setStatusCode(303);
                ctx.response().putHeader("Location", "/wiki/" + title);
                ctx.response().end();
            }
        });
    }

    private static boolean isNewPage(RoutingContext ctx) {
        return "yes".equals(ctx.request().getParam("newPage"));
    }
}
