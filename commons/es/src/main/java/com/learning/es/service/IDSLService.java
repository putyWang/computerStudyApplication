package com.learning.es.service;

import org.elasticsearch.client.Request;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 表达式查询
 */
public interface IDSLService {

    /**
     * 利用es查询字符串进行查询
     * @param method 请求方法
     * @param type es查询类型
     * @param dslIndex 索引
     * @param dsl es查询json字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    Map<String, Object> dslExecute(String method, String type, String dslIndex, String dsl) throws UnsupportedEncodingException;

    /**
     * 根据请求类型执行es查询json字符串
     * @param request 请求
     * @param dsl es查询json字符串
     * @return
     */
    Map<String, Object> doDsl(Request request, String dsl);
}