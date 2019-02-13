package com.max.reactive.wiki.handler;

import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public final class DeletePageHandler implements Handler<RoutingContext> {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PageDao pageDao;

    public DeletePageHandler(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    @Override
    public void handle(RoutingContext ctx) {

        final String pageId = ctx.request().getParam("id");

        pageDao.delete(pageId).setHandler(ar -> {
            if (ar.failed()) {
                LOG.error("Can't execute delete page SQL statement", ar.cause());
                ctx.fail(ar.cause());
            }
            else {
                ctx.response().setStatusCode(303);
                ctx.response().putHeader("Location", "/wiki");
                ctx.response().end();
            }
        });

    }
}
