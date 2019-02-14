package com.max.reactive.wiki.dao;

import lombok.Value;

@Value
public class PageDto {

    private final String pageName;
    private final Integer pageId;
    private final String newPage;
    private final String content;
    private final String timestamp;

    public static final PageDto EMPTY = new PageDto("", -1, "", "", "");

}
