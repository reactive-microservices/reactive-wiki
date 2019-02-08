package com.max.reactive.wiki;

import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public final class WikiModule extends AbstractModule {

    private final Vertx vertx;

    WikiModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        bind(JDBCClient.class)
                .toInstance(JDBCClient.createShared(vertx,
                                                    new JsonObject().
                                                            put("url", "jdbc:hsqldb:file:db/wiki").
                                                            put("driver_class", "org.hsqldb.jdbcDriver").
                                                            put("max_pool_size", 30)));

        bind(FreeMarkerTemplateEngine.class).toInstance(FreeMarkerTemplateEngine.create(vertx));
    }
}
