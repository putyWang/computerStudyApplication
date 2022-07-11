package com.learning.es.enums;

import com.learning.core.utils.StringUtils;

public enum DSLEnum {
    DSL_GET_SEARCH("GET", "_search", "{\n\t\"query\": {\n\t\t\"match_all\": {}\n\t}\n}"),
    DSL_GET_COUNT("GET", "_count", "{\n\t\"query\": {\n\t\t\"match_all\": {}\n\t}\n}"),
    DSL_GET_SETTINGS("GET", "_settings", ""),
    DSL_PUT_SETTINGS("PUT", "_settings", "{\n\t\"settings\": {\n\t\t\"field_name\": \"value\"\n\t}\n}"),
    DSL_GET_MAPPING("GET", "_mapping", ""),
    DSL_PUT_MAPPING("PUT", "_mapping", "{\n\t\t\"properties\": {\n\t\t\t\"field\": {\n\t\t\t\t\"type\": \"string\"\n\t\t\t}\n\t}\n}"),
    DSL_POST_ANALYZE("POST", "_analyze", "{\n  \"analyzer\": \"whitespace\",\n  \"text\":     \"The quick brown fox.\"\n}\n"),
    DSL_DELETE_BY_ID("DELETE", "_delete", "{\n    \"id\" : \"value\",\n    \"routing\" : \"value\"\n}"),
    DSL_DELETE_BY_QUERY("POST", "_delete_by_query", "{\n\t\"query\": {\n\t\t\"term\": {\n\t\t\t\"field_name\": \"value\"\n\t\t}\n\t}\n}"),
    DSL_UPDATE_BY_ID("PUT", "_update", "{\n\t\"id\": \"value\",\n\t\"routing\": \"value\",\n\t\"doc\": {\n\t\t\"key1\": \"key1\",\n\t\t\"key2\": \"key2\"\n\t}\n}"),
    DSL_UPDATE_BY_QUERY("POST", "_update_by_query", "{\n\t\t\t\"script\": {\n\t\t\t\t\"source\": \"ctx._source['key']='value'\" \n\t\t\t\t  },\n\t\t\t\t  \"query\":{\"match_all\":{}}\n\t\t\t}");

    private String requestType;
    private String dslMethod;
    private String dslTemp;

    private DSLEnum(String requestType, String dslMethod, String dslTemp) {
        this.requestType = requestType;
        this.dslMethod = dslMethod;
        this.dslTemp = dslTemp;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public String getDslMethod() {
        return this.dslMethod;
    }

    public String getDslTemp() {
        return this.dslTemp;
    }

    public static DSLEnum getByQuery(String requestType, String dslMethod) {
        if (! StringUtils.isEmpty(requestType) && ! StringUtils.isEmpty(dslMethod)) {
            DSLEnum[] var2 = values();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                DSLEnum dslEnum = var2[var4];
                if (dslEnum.getRequestType().equals(requestType) && dslEnum.getDslMethod().equals(dslMethod)) {
                    return dslEnum;
                }
            }
        }

        return null;
    }
}
