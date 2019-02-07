package com.max.reactive.wiki.handler;

import com.max.reactive.wiki.PageDao;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

public final class IndexHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JDBCClient dbClient;
    private final FreeMarkerTemplateEngine templateEngine;

    public IndexHandler(JDBCClient dbClient, FreeMarkerTemplateEngine templateEngine) {
        this.dbClient = dbClient;
        this.templateEngine = templateEngine;
    }

    @Override
    public void handle(RoutingContext ctx) {
        Future<Buffer> allStepsFuture =
                getPagesFromDb().compose(pages -> renderData(ctx, pages));

        allStepsFuture.setHandler(ar -> {
            if (ar.failed()) {
                ctx.fail(ar.cause());
            }
            else {
                ctx.response().putHeader("Content-Type", "text/html");
                ctx.response().end(ar.result());
            }
        });
    }

    private Future<List<String>> getPagesFromDb() {
        Future<List<String>> allPagesFuture = Future.future();

        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOG.error("Error obtaining connection to DB", ar.cause());
                allPagesFuture.fail(ar.cause());
            }
            else {
                SQLConnection conn = ar.result();

                conn.query(PageDao.SQL_ALL_PAGES, resultSet -> {

                    conn.close();

                    if (resultSet.failed()) {
                        LOG.error("Error reading all pages from DB", resultSet.cause());
                        allPagesFuture.fail(resultSet.cause());
                    }
                    else {
                        List<String> pages = resultSet.result().getResults().stream().
                                map(json -> json.getString(0)).
                                sorted().
                                collect(Collectors.toList());

                        allPagesFuture.complete(pages);
                    }
                });
            }
        });

        return allPagesFuture;
    }

    private Future<Buffer> renderData(RoutingContext ctx, List<String> pages) {
        Future<Buffer> templateEngineFuture = Future.future();

        ctx.put("title", "Wiki Home");
        ctx.put("pages", pages);

        templateEngine.render(ctx.data(), "templates/pages.ftl", renderResult -> {
            if (renderResult.failed()) {
                LOG.error("Error rendering template", renderResult.cause());
                templateEngineFuture.fail(renderResult.cause());
            }
            else {
                templateEngineFuture.complete(renderResult.result());
            }
        });


        return templateEngineFuture;
    }

}
