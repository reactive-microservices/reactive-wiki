package com.max.reactive.wiki.handler.api;

import com.max.reactive.wiki.dao.PageDao;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

public class GetAllPagesApi implements Handler<RoutingContext> {

    private final PageDao pageDao;

    public GetAllPagesApi(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    @Override
    public void handle(RoutingContext ctx) {

        pageDao.getAllPages().setHandler(ar -> {
            if (ar.failed()) {
                JsonObject errorData = new JsonObject();
                errorData.put("error", "Can't get all pages");
                errorData.put("details", ar.cause().getMessage());
                ctx.response().setStatusCode(500).end(errorData.encodePrettily());
            }
            else {
                List<JsonObject> allPagesJson = ar.result().stream().map(pageDto -> {
                    return new JsonObject().put("id", pageDto.getPageId()).put("name", pageDto.getPageName());
                }).collect(Collectors.toList());

                JsonObject allPages = new JsonObject().put("pages", new JsonArray(allPagesJson));

                ctx.response().putHeader("Content-Type", "application/json").end(allPages.encodePrettily());
            }
        });
    }


}
