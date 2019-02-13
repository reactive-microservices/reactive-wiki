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

    public static final String SQL_CREATE_PAGE_TABLE = "create table if not exists PAGE (id integer identity primary key, " +
            "name varchar(255) unique, content clob)";

    public static final String SQL_INSERT_PAGE = "insert into PAGE values (NULL, ?, ?)";
    public static final String SQL_UPDATE_PAGE = "update PAGE set content = ? where id = ?";

    public static final String SQL_DELETE_PAGE = "delete from PAGE where id = ?";

    private JDBCClient dbClient;

    @Inject
    public void setDbClient(JDBCClient dbClient) {
        this.dbClient = dbClient;
    }

    public Future<List<String>> getAllPages() {
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

    private static final String EMPTY_PAGE_MARKDOWN =
            "# A new page\n" +
                    "\n" +
                    "Feel-free to write in Markdown!\n";

    public Future<PageDto> getSinglePage(String pageName) {

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

}
