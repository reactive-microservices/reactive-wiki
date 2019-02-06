package com.max.reactive.wiki;

public class PageDao {

    public static final String SQL_CREATE_PAGE_TABLE = "create table if not exists PAGE (id integer identity primary key, " +
            "name varchar(255) unique, content clob)";

    public static final String SQL_SELECT_ALL_PAGES = "select * from PAGE";

    public static final String SQL_GET_PAGE = "select id, content from PAGE where name = ?";
    public static final String SQL_CREATE_PAGE = "insert into PAGE values (NULL, ?, ?)";
    public static final String SQL_SAVE_PAGE = "update PAGE set content = ? where id = ?";
    public static final String SQL_ALL_PAGES = "select name from PAGE";
    public static final String SQL_DELETE_PAGE = "delete from PAGE where id = ?";

}
