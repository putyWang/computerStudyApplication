package com.learning.es.model.elastic;

import com.learning.es.enums.AnalyzerEnum;

public class TextFieldMetadata extends BaseFieldMetadata {
    private final static String N = "text";

    /**
     * 属性名称
     */
    private String name;
    /**
     * 索引分析器，默认ik_max_word
     */
    private String analyzer;

    /**
     * 是否创建关键词
     */
    private boolean createKeyword;

    /**
     * 搜索时分词器，如果不设置，默认使用analyzer
     */
    private String searchAnalyzer;

    public TextFieldMetadata(String name) {
        super(N);
        this.name = name;
        this.analyzer = AnalyzerEnum.IK_MAX_WORD.getCode();
        this.searchAnalyzer = AnalyzerEnum.IK_MAX_WORD.getCode();
        this.createKeyword = false;
    }

    public String getName() {
        return name;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getSearchAnalyzer() {
        return searchAnalyzer;
    }

    public void setSearchAnalyzer(String searchAnalyzer) {
        this.searchAnalyzer = searchAnalyzer;
    }

    public boolean getCreateKeyword() {
        return createKeyword;
    }

    public void setCreateKeyword(boolean isCreate) {
        createKeyword = isCreate;
    }
}
