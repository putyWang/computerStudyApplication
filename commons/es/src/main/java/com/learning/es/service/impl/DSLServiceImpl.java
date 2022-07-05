package com.learning.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.service.EsServiceImpl;
import com.learning.es.service.IDSLService;
import com.learning.es.service.esServiceImpl;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DSLServiceImpl
        extends EsServiceImpl
        implements IDSLService {

    public DSLServiceImpl(RestClientFactory restClientFactory) {

        super(restClientFactory);
    }

    public Map<String, Object> dslExecute(String method, String type, String dslIndex, String dsl) throws UnsupportedEncodingException {
        boolean ret = true;
        String endpoint = "";
        String msg = "";
        Map<String, Object> map = new HashMap<>();
        DSLEnum dslEnum = DSLEnum.getByQuery(method, type);
        if (dslEnum == null) {
            msg = "不支持" + method + "和" + type + "的组合类型，请重新选择.";
            map.put("status", false);
            map.put("msg", msg);
            return map;
        } else {
            switch(dslEnum) {
                case DSL_GET_SEARCH:
                    endpoint = "/" + dslIndex + "/_search?format=json&pretty";
                    break;
                case DSL_GET_COUNT:
                    endpoint = "/" + dslIndex + "/_count";
                    break;
                case DSL_GET_MAPPING:
                    endpoint = "/" + dslIndex + "//_mapping?format=json&pretty";
                    break;
                case DSL_PUT_MAPPING:
                    endpoint = "/" + dslIndex + "/" + "doc" + "/_mapping";
                    break;
                case DSL_GET_SETTINGS:
                    endpoint = "/" + dslIndex + "/_settings?format=json&pretty";
                    break;
                case DSL_PUT_SETTINGS:
                    endpoint = "/" + dslIndex + "/_settings";
                    break;
                case DSL_POST_ANALYZE:
                    endpoint = "/" + dslIndex + "/_analyze?format=json&pretty";
                    break;
                case DSL_DELETE_BY_ID:
                case DSL_UPDATE_BY_ID:
                    if (StringUtil.isNotEmpty(dsl)) {
                        JSONObject jsonData = JSONObject.parseObject(dsl);
                        String id = jsonData.getString("id");
                        String routing = jsonData.getString("routing");
                        dsl = jsonData.getString("doc");
                        if (StringUtil.isNotEmpty(id) && StringUtil.isNotEmpty(routing)) {
                            id = URLEncoder.encode(id, "UTF-8");
                            endpoint = "/" + dslIndex + "/" + "doc" + "/" + id + "?routing=" + routing;
                        }
                    }
                    break;
                case DSL_DELETE_BY_QUERY:
                    endpoint = "/" + dslIndex + "/_delete_by_query?format=json&pretty";
                    break;
                case DSL_UPDATE_BY_QUERY:
                    endpoint = "/" + dslIndex + "/" + "doc" + "/_update_by_query?conflicts=proceed";
                    break;
                default:
                    ret = false;
                    msg = "不支持" + method + "和" + type + "的组合类型，请重新选择.";
            }

            Request request = new Request(method, endpoint);

            try {
                if (StringUtil.isNotEmpty(dsl)) {
                    request.setEntity(new NStringEntity(dsl, ContentType.APPLICATION_JSON));
                }

                Response response = this.restClient.performRequest(request);
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(responseBody);
                map.put("result", jsonObject);
            } catch (IOException var17) {
                this.log.error("Elasticsearch 执行错误，", var17);
                ret = false;
                msg = "执行DSL出错.";
            }

            map.put("status", ret);
            map.put("msg", msg);
            return map;
        }
    }

    public Map<String, Object> doDsl(Request request, String dsl) {
        HashMap map = new HashMap();

        try {
            if (StringUtil.isNotEmpty(dsl)) {
                request.setEntity(new NStringEntity(dsl, ContentType.APPLICATION_JSON));
            }

            Response response = this.restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(responseBody);
            map.put("status", true);
            map.put("result", jsonObject);
        } catch (IOException var7) {
            this.log.error("Elasticsearch 执行错误，", var7);
            map.put("status", false);
            map.put("result", (Object)null);
        }

        return map;
    }
}
