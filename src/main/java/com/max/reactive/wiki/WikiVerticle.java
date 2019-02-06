package com.max.reactive.wiki;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class WikiVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final int PORT = 8080;

    @Override
    public void start() {
        
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end("Hello wiki."))
                .listen(PORT);

        LOG.info("MainVerticle started at port {}", PORT);
    }
}
