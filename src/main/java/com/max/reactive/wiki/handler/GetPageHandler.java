package com.max.reactive.wiki.handler;

import com.github.rjeschke.txtmark.Processor;
import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        Future<Buffer> getPageFuture = readPageFromDb(pageName).compose(this::renderTemplate);

        getPageFuture.setHandler(ar -> {
            if (ar.failed()) {
                LOG.error("Error displaying page content", ar.cause());
                ctx.fail(ar.cause());
            }
            else {
                ctx.response().putHeader("Content-Type", "text/html");
                ctx.response().end(ar.result());
            }
        });
    }

    private Future<PageDto> readPageFromDb(String pageName) {

        Future<PageDto> readPageFuture = Future.future();

        dbClient.getConnection(car -> {
            if (car.failed()) {
                LOG.error("Error obtaining connection to DB", car.cause());
                readPageFuture.fail(car.cause());
            }
            else {

                SQLConnection conn = car.result();

                conn.queryWithParams(PageDao.SQL_GET_PAGE, new JsonArray().add(pageName), resultSet -> {
                    conn.close();

                    if (resultSet.failed()) {
                        LOG.error("Error reading page data from database", resultSet.cause());
                        readPageFuture.fail(resultSet.cause());
                    }
                    else {
                        JsonArray row = resultSet.result().getResults().
                                stream().
                                findFirst().
                                orElseGet(() -> new JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN));

                        Integer id = row.getInteger(0);
                        String newPage = resultSet.result().getResults().isEmpty() ? "yes" : "no";
                        String content = row.getString(1);

                        PageDto pageDto = new PageDto(pageName, id, newPage, content, new Date().toString());

                        readPageFuture.complete(pageDto);
                    }
                });
            }
        });

        return readPageFuture;
    }

    private Future<Buffer> renderTemplate(PageDto pageDto) {

        Future<Buffer> renderTemplateFuture = Future.future();

        Map<String, Object> data = new HashMap<>();
        data.put("title", pageDto.pageName);
        data.put("id", pageDto.pageId);
        data.put("newPage", pageDto.newPage);
        data.put("rawContent", pageDto.content);
        data.put("content", Processor.process(pageDto.content));
        data.put("timestamp", pageDto.timestamp);

        templateEngine.render(data, "templates/single_page.ftl", renderedData -> {
            if (renderedData.failed()) {
                renderTemplateFuture.fail(renderedData.cause());
                LOG.error("Error rendering template", renderedData.cause());
            }
            else {
                renderTemplateFuture.complete(renderedData.result());
            }
        });

        return renderTemplateFuture;
    }

    @Value
    private static final class PageDto {
        private final String pageName;
        private final Integer pageId;
        private final String newPage;
        private final String content;
        private final String timestamp;
    }
}
