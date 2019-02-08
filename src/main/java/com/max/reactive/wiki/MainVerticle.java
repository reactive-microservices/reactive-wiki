package com.max.reactive.wiki;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MainVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void start(Future<Void> startFuture) {

        final int wikiVerticlesCount = Runtime.getRuntime().availableProcessors();

        LOG.info("Deploying {} WikiVerticles", wikiVerticlesCount);

        vertx.deployVerticle(WikiVerticle.class,
                             new DeploymentOptions().setInstances(wikiVerticlesCount),
                             ar -> {
                                 if (ar.failed()) {
                                     LOG.error("Can't properly deploy few WikiVerticle from MainVerticle", ar.cause());
                                     startFuture.fail(ar.cause());
                                 }
                                 else {
                                     startFuture.complete();
                                 }
                             });
    }
}
