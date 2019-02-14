package com.max.reactive.wiki;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MainVerticle extends io.vertx.reactivex.core.AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    @SuppressWarnings("CheckReturnValue")
    public void start(Future<Void> startFuture) {

        final int wikiVerticlesCount = Runtime.getRuntime().availableProcessors();

        LOG.info("Deploying {} WikiVerticles", wikiVerticlesCount);

        vertx.rxDeployVerticle(WikiVerticle.class.getCanonicalName(),
                               new DeploymentOptions().setInstances(wikiVerticlesCount)
        ).subscribe(ar -> startFuture.complete(),
                    error -> {
                        LOG.error("Can't properly deploy few WikiVerticle from MainVerticle", error);
                        startFuture.fail(error);
                    });
    }
}
