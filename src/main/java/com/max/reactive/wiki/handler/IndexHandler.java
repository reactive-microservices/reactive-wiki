package com.max.reactive.wiki.handler;

import com.max.reactive.wiki.PageDao;
import io.vertx.core.Handler;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import java.util.List;
import java.util.stream.Collectors;

public final class IndexHandler implements Handler<RoutingContext> {

    private final JDBCClient dbClient;
    private final FreeMarkerTemplateEngine templateEngine;

    public IndexHandler(JDBCClient dbClient, FreeMarkerTemplateEngine templateEngine) {
        this.dbClient = dbClient;
        this.templateEngine = templateEngine;
    }

    @Override
    public void handle(RoutingContext ctx) {
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                ctx.fail(ar.cause());
            }
            else {
                SQLConnection conn = ar.result();

                conn.query(PageDao.SQL_ALL_PAGES, resultSet -> {

                    conn.close();

                    if (resultSet.failed()) {
                        ctx.fail(resultSet.cause());
                    }
                    else {
                        List<String> pages = resultSet.result().getResults().stream().
                                map(json -> json.getString(0)).
                                sorted().
                                collect(Collectors.toList());

                        ctx.put("title", "Wiki Home");
                        ctx.put("pages", pages);

                        templateEngine.render(ctx.data(), "templates/pages.ftl", renderResult -> {
                            if (ar.failed()) {
                                ctx.fail(renderResult.cause());
                            }
                            else {
                                ctx.response().putHeader("Content-Type", "text/html");
                                ctx.response().end(renderResult.result());
                            }
                        });

                    }

                });
            }
        });
    }
}
