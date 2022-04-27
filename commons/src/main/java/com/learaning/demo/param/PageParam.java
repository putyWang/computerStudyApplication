package com.learaning.demo.param;

import lombok.Data;

/**
 *
 */
@Data
public class PageParam {

    //当前页
    private int current;

    //当前页面数据数
    private int pageSize;

    //关键字
    private String[] keywords;
}
