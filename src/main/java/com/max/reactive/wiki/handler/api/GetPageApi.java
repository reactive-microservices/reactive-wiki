package com.max.reactive.wiki.handler.api;

import com.max.reactive.wiki.dao.PageDao;
import com.max.reactive.wiki.dao.PageDto;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class GetPageApi implements Handler<RoutingContext> {

    private final PageDao pageDao;

    public GetPageApi(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    @Override
    public void handle(RoutingContext ctx) {

        final String pageId = ctx.request().getParam("id");

        Future<PageDto> pageFromDbFuture = pageDao.getSinglePageById(pageId);

        pageFromDbFuture.setHandler(ar -> {
            if (ar.failed()) {
                JsonObject errorData = new JsonObject();
                errorData.put("error", "Can't get page for id: " + pageId);
                errorData.put("details", ar.cause().getMessage());
                ctx.response().setStatusCode(500).end(errorData.encodePrettily());
            }
            else {
                PageDto dto = ar.result();

                JsonObject singlePageJson = new JsonObject();
                singlePageJson.put("id", dto.getPageId());
                singlePageJson.put("name", dto.getPageName());
                singlePageJson.put("content", dto.getContent());

                ctx.response().putHeader("Content-Type", "application/json").end(singlePageJson.encodePrettily());
            }
        });

    }

}
