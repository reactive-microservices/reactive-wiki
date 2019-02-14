package com.max.reactive.wiki.dao;

import lombok.Value;

@Value
public class PageData {

    public static final PageData EMPTY = new PageData("", -1, "", "", "");

    private final String pageName;
    private final Integer pageId;
    private final String newPage;
    private final String content;
    private final String timestamp;
}
