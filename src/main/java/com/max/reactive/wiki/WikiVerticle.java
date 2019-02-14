package com.max.reactive.wiki;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.max.reactive.wiki.dao.PageDao;
import com.max.reactive.wiki.handler.CreateNewPageHandler;
import com.max.reactive.wiki.handler.DeletePageHandler;
import com.max.reactive.wiki.handler.GetPageHandler;
import com.max.reactive.wiki.handler.HealthHandler;
import com.max.reactive.wiki.handler.IndexHandler;
import com.max.reactive.wiki.handler.SavePageHandler;
import com.max.reactive.wiki.handler.api.AddNewPageApi;
import com.max.reactive.wiki.handler.api.GetAllPagesApi;
import com.max.reactive.wiki.handler.api.GetPageApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class WikiVerticle extends AbstractVerticle {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String HTTP_PORT_KEY = "http.server.port";

    private PageDao pageDao;

    private FreeMarkerTemplateEngine templateEngine;

    @Inject
    public void setPageDao(PageDao pageDao) {
        this.pageDao = pageDao;
    }

    @Inject
    public void setTemplateEngine(FreeMarkerTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void start(Future<Void> startFuture) {

        Guice.createInjector(new WikiModule(vertx)).injectMembers(this);

        pageDao.createTableIfNotExist().
                compose(v -> startHttpServer()).
                setHandler(startFuture.completer());
    }

    private Future<Void> startHttpServer() {

        Router router = Router.router(vertx);

        //API GET
        Router apiRouter = Router.router(vertx);
        apiRouter.get("/pages").handler(new GetAllPagesApi(pageDao));
        apiRouter.get("/pages/:id").handler(new GetPageApi(pageDao));

        // API post
        apiRouter.post().handler(BodyHandler.create());
        apiRouter.post("/pages").handler(new AddNewPageApi(pageDao));

        // GET
        router.get("/health").handler(new HealthHandler());
        router.get("/wiki").handler(new IndexHandler(pageDao, templateEngine));
        router.get("/wiki/:pageName").handler(new GetPageHandler(pageDao, templateEngine));

        // POST
        router.post().handler(BodyHandler.create());
        router.post("/wiki/create").handler(new CreateNewPageHandler());
        router.post("/wiki/save").handler(new SavePageHandler(pageDao));
        router.post("/wiki/delete").handler(new DeletePageHandler(pageDao));

        router.mountSubRouter("/api", apiRouter);

        Future<Void> httpServerFuture = Future.future();

        final int portNumber = config().getInteger(HTTP_PORT_KEY, 8080);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(portNumber, ar -> {
                    if (ar.failed()) {
                        LOG.error("Can't start HTTP", ar.cause());
                        httpServerFuture.fail(ar.cause());
                    }
                    else {
                        LOG.info("HTTP server successfully started at port {}", portNumber);
                        httpServerFuture.complete();
                    }
                });

        return httpServerFuture;
    }


}
