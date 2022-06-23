package com.learning.es.service;

import org.elasticsearch.client.Request;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface IDSLService {
    Map<String, Object> dslExecute(String var1, String var2, String var3, String var4) throws UnsupportedEncodingException;

    Map<String, Object> doDsl(Request var1, String var2);
}