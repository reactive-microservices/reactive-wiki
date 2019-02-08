package com.max.reactive.wiki;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class WikiVerticleIntTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext tc) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(WikiVerticle.class.getName(), tc.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.close(tc.asyncAssertSuccess());
    }

    @Test
    public void serverStartedSuccessfully(TestContext tc) {
        Async async = tc.async();
        vertx.createHttpClient().getNow(8080, "localhost", "/health", response -> {
            tc.assertEquals(response.statusCode(), 200);
            response.bodyHandler(body -> {
                tc.assertTrue(body.length() > 0);
                tc.assertEquals("{\n" +
                                        "  \"http server\" : \"OK\",\n" +
                                        "  \"db\" : \"OK\"\n" +
                                        "}", body.toString());
                async.complete();
            });
        });
    }

}
