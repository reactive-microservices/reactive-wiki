package com.max.reactive.wiki;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.max.reactive.wiki.dao.PageDao;
import com.max.reactive.wiki.handler.CreateNewPageHandler;
import com.max.reactive.wiki.handler.DeletePageHandler;
import com.max.reactive.wiki.handler.GetPageHandler;
import com.max.reactive.wiki.handler.HealthHandler;
import com.max.reactive.wiki.handler.IndexHandler;
import com.max.reactive.wiki.handler.SavePageHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class WikiVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final int PORT = 8080;

    private Injector injector;

    @Override
    public void start(Future<Void> startFuture) {

        injector = Guice.createInjector(new WikiModule(vertx));

        Future<Void> initSteps = prepareDatabase().compose(v -> startHttpServer());
        initSteps.setHandler(startFuture.completer());
    }

    private Future<Void> prepareDatabase() {
        Future<Void> databaseFuture = Future.future();

        JDBCClient dbClient = injector.getInstance(JDBCClient.class);

        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                databaseFuture.fail(ar.cause());
            }
            else {

                SQLConnection conn = ar.result();

                conn.execute(PageDao.SQL_CREATE_PAGE_TABLE, createResult -> {
                    conn.close();

                    if (createResult.failed()) {
                        LOG.error("Can't create PAGE table", createResult.cause());
                        databaseFuture.fail(createResult.cause());
                    }
                    else {
                        LOG.info("Connection to DB successfully created.");
                        databaseFuture.complete();
                    }
                });

            }
        });

        return databaseFuture;
    }

    private Future<Void> startHttpServer() {

        JDBCClient dbClient = injector.getInstance(JDBCClient.class);
        FreeMarkerTemplateEngine templateEngine = injector.getInstance(FreeMarkerTemplateEngine.class);

        Router router = Router.router(vertx);

        // GET
        router.get("/health").handler(new HealthHandler());
        router.get("/wiki").handler(new IndexHandler(dbClient, templateEngine));
        router.get("/wiki/:pageName").handler(new GetPageHandler(dbClient, templateEngine));

        // POST
        router.post().handler(BodyHandler.create());
        router.post("/wiki/create").handler(new CreateNewPageHandler());
        router.post("/wiki/save").handler(new SavePageHandler(dbClient));
        router.post("/wiki/delete").handler(new DeletePageHandler(dbClient));

        Future<Void> httpServerFuture = Future.future();

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(PORT, ar -> {
                    if (ar.failed()) {
                        LOG.error("Can't start HTTP", ar.cause());
                        httpServerFuture.fail(ar.cause());
                    }
                    else {
                        LOG.info("HTTP server successfully started at port {}", PORT);
                        httpServerFuture.complete();
                    }
                });

        return httpServerFuture;
    }


}
