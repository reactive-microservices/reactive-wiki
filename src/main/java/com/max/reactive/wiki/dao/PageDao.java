package com.max.reactive.wiki.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PageDao {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String EMPTY_PAGE_MARKDOWN =
            "# A new page\n" +
                    "\n" +
                    "Feel-free to write in Markdown!\n";

    private JDBCClient dbClient;

    @Inject
    public void setDbClient(JDBCClient dbClient) {
        this.dbClient = dbClient;
    }


    public Future<List<PageDto>> getAllPages() {
        Future<List<PageDto>> resFuture = Future.future();

        dbClient.query("SELECT id, name from PAGE", res -> {
            if (res.failed()) {
                LOG.error("Can't read PAGE data from DB");
                resFuture.fail(res.cause());
            }
            else {
                List<PageDto> allPages = res.result().getResults().stream().
                        map(json -> new PageDto(json.getString(1), json.getInteger(0), "", "", "")).collect(Collectors.toList());

                resFuture.complete(allPages);
            }
        });

        return resFuture;
    }

    public Future<Void> createTableIfNotExist() {
        Future<Void> databaseFuture = Future.future();

        dbClient.update("create table if not exists PAGE (id integer identity primary key, " +
                                "name varchar(255) unique, content clob)", ar -> {
            if (ar.failed()) {
                LOG.error("Can't create PAGE table", ar.cause());
                databaseFuture.fail(ar.cause());
            }
            else {
                LOG.info("Connection to DB successfully created.");
                databaseFuture.complete();
            }
        });

        return databaseFuture;
    }

    public Future<List<String>> getAllPagesNames() {
        Future<List<String>> allPagesFuture = Future.future();

        dbClient.query("select name from PAGE", resultSet -> {
            if (resultSet.failed()) {
                LOG.error("Error reading all pages from DB", resultSet.cause());
                allPagesFuture.fail(resultSet.cause());
            }
            else {
                List<String> pages = resultSet.result().getResults().stream().
                        map(json -> json.getString(0)).
                        sorted().
                        collect(Collectors.toList());

                allPagesFuture.complete(pages);
            }
        });

        return allPagesFuture;
    }

    public Future<PageDto> getSinglePageByName(String pageName) {

        Future<PageDto> readPageFuture = Future.future();

        dbClient.queryWithParams("select id, content from PAGE where name = ?", new JsonArray().add(pageName), resultSet -> {
            if (resultSet.failed()) {
                LOG.error("Error reading page data from database", resultSet.cause());
                readPageFuture.fail(resultSet.cause());
            }
            else {
                JsonArray row = resultSet.result().getResults().
                        stream().
                        findFirst().
                        orElseGet(() -> new JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN));

                Integer id = row.getInteger(0);
                String newPage = resultSet.result().getResults().isEmpty() ? "yes" : "no";
                String content = row.getString(1);

                PageDto pageDto = new PageDto(pageName, id, newPage, content, new Date().toString());

                readPageFuture.complete(pageDto);
            }
        });

        return readPageFuture;
    }

    public Future<Void> save(String title, String content) {

        Future<Void> resultFuture = Future.future();
        JsonArray params = new JsonArray().add(title).add(content);

        dbClient.updateWithParams("insert into PAGE values (NULL, ?, ?)", params, ar -> {
            if (ar.failed()) {
                resultFuture.fail(ar.cause());
                LOG.error("Can't update page from DB", ar.cause());
            }
            else {
                resultFuture.complete();
            }
        });

        return resultFuture;
    }

    public Future<Void> update(String id, String content) {

        Future<Void> resultFuture = Future.future();
        JsonArray params = new JsonArray().add(content).add(id);

        dbClient.updateWithParams("update PAGE set content = ? where id = ?", params, ar -> {
            if (ar.failed()) {
                resultFuture.fail(ar.cause());
                LOG.error("Can't update page from DB", ar.cause());
            }
            else {
                resultFuture.complete();
            }
        });

        return resultFuture;

    }

    public Future<Void> delete(String id) {

        Future<Void> resFuture = Future.future();

        dbClient.updateWithParams("delete from PAGE where id = ?", new JsonArray().add(id), deleteRes -> {
            if (deleteRes.failed()) {
                LOG.error("Can't execute delete page SQL statement", deleteRes.cause());
                resFuture.fail(deleteRes.cause());
            }
            else {
                resFuture.complete();
            }
        });

        return resFuture;
    }

    public Future<PageDto> getSinglePageById(String id) {
        Future<PageDto> dbFuture = Future.future();

        dbClient.queryWithParams("SELECT id, name, content from PAGE where id = ?", new JsonArray().add(id), ar -> {
            if (ar.failed()) {
                LOG.error("Can't get page with id " + id + " from DB", ar.cause());
                dbFuture.fail(ar.cause());
            }
            else {
                JsonArray row = ar.result().getResults().
                        stream().
                        findFirst().
                        orElse(new JsonArray());

                PageDto dto = new PageDto(row.getString(1), row.getInteger(0), "false",
                                          row.getString(2), new Date().toString());
                dbFuture.complete(dto);
            }
        });

        return dbFuture;
    }
}
