package com.max.reactive.wiki.handler;

import com.github.rjeschke.txtmark.Processor;
import com.max.reactive.wiki.PageDao;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;

public final class GetPageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String EMPTY_PAGE_MARKDOWN =
            "# A new page\n" +
                    "\n" +
                    "Feel-free to write in Markdown!\n";

    private final JDBCClient dbClient;
    private final FreeMarkerTemplateEngine templateEngine;

    public GetPageHandler(JDBCClient dbClient, FreeMarkerTemplateEngine templateEngine) {
        this.dbClient = dbClient;
        this.templateEngine = templateEngine;
    }

    @Override
    public void handle(RoutingContext ctx) {

        String pageName = ctx.request().getParam("pageName");

        dbClient.getConnection(car -> {
            if (car.failed()) {
                ctx.fail(car.cause());
                LOG.error("Error", car.cause());
            }
            else {

                SQLConnection conn = car.result();

                conn.queryWithParams(PageDao.SQL_GET_PAGE, new JsonArray().add(pageName), resultSet -> {
                    conn.close();

                    if (resultSet.failed()) {
                        ctx.fail(resultSet.cause());
                        LOG.error("Error", resultSet.cause());
                    }
                    else {
                        JsonArray row = resultSet.result().getResults().
                                stream().
                                findFirst().
                                orElseGet(() -> new JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN));

                        Integer id = row.getInteger(0);
                        String content = row.getString(1);

                        ctx.put("title", pageName);
                        ctx.put("id", id);
                        ctx.put("newPage", resultSet.result().getResults().size() == 0 ? "yes" : "no");
                        ctx.put("rawContent", content);
                        ctx.put("content", Processor.process(content));
                        ctx.put("timestamp", new Date().toString());

                        templateEngine.render(ctx.data(), "templates/single_page.ftl", renderedData -> {
                            if (renderedData.failed()) {
                                ctx.fail(renderedData.cause());
                                LOG.error("Error", renderedData.cause());
                            }
                            else {
                                ctx.response().putHeader("Content-Type", "text/html");
                                ctx.response().end(renderedData.result());
                            }
                        });
                    }
                });

            }
        });


    }
}
