package com.max.reactive.wiki.handler;

import com.github.rjeschke.txtmark.Processor;
import com.max.reactive.wiki.dao.PageDao;
import com.max.reactive.wiki.dao.PageData;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public final class GetPageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PageDao pageDao;
    private final FreeMarkerTemplateEngine templateEngine;

    public GetPageHandler(PageDao pageDao, FreeMarkerTemplateEngine templateEngine) {
        this.pageDao = pageDao;
        this.templateEngine = templateEngine;
    }

    @Override
    public void handle(RoutingContext ctx) {

        String pageName = ctx.request().getParam("pageName");

        Future<Buffer> getPageFuture = pageDao.getSinglePageByName(pageName).compose(this::renderTemplate);

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

    private Future<Buffer> renderTemplate(PageData pageDto) {

        Future<Buffer> renderTemplateFuture = Future.future();

        Map<String, Object> data = new HashMap<>();
        data.put("title", pageDto.getPageName());
        data.put("id", pageDto.getPageId());
        data.put("newPage", pageDto.getNewPage());
        data.put("rawContent", pageDto.getContent());
        data.put("content", Processor.process(pageDto.getContent()));
        data.put("timestamp", pageDto.getTimestamp());

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
}
