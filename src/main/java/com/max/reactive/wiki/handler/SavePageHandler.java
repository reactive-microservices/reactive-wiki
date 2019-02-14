package com.max.reactive.wiki.handler;

import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class SavePageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PageDao pageDao;

    public SavePageHandler(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    @Override
    public void handle(RoutingContext ctx) {

        String id = ctx.request().getParam("id");
        String title = ctx.request().getParam("title");
        String markdown = ctx.request().getParam("markdown");

        Future<?> updateDbFuture;

        if (isNewPage(ctx)) {
            updateDbFuture = pageDao.save(title, markdown);
        }
        else {
            updateDbFuture = pageDao.update(id, markdown);
        }

        updateDbFuture.setHandler(asyncResult -> {
            if (asyncResult.failed()) {
                LOG.error("Error", asyncResult.cause());
                ctx.fail(asyncResult.cause());
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
