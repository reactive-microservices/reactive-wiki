package com.max.reactive.wiki.handler.api;

import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AddNewPageApi implements Handler<RoutingContext> {

    private final PageDao pageDao;

    public AddNewPageApi(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    @Override
    public void handle(RoutingContext ctx) {

        final String title = ctx.request().getParam("title");
        final String content = ctx.request().getParam("content");

        pageDao.save(title, content).
                setHandler(ar -> {
                    if (ar.failed()) {
                        JsonObject errorData = new JsonObject();
                        errorData.put("error", "ERROR_ADDING_NEW_PAGE");
                        errorData.put("details", "Can't add new page with title " + title);
                        ctx.response().setStatusCode(500).end(errorData.encodePrettily());
                    }
                    else {
                        ctx.response().
                                setStatusCode(201).
                                putHeader("Location", "/api/pages/" + ar.result()).
                                end();
                    }
                });
    }
}
