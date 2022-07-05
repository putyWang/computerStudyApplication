package com.learning.es.service;

import com.learning.es.bean.AdvancedSearchResult;
import com.learning.es.bean.FulltextSearchResult;
import com.learning.es.bean.SearchResult;
import com.learning.es.constants.ElasticMethodInterface;
import com.learning.es.model.condition.ConditionBuilder;
import com.learning.es.model.QuickConditionBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Map;

public interface SearchService  {
    /**
     *
     * @param conditionBuilder
     * @param page
     * @param size
     * @param indices
     * @return
     */
    FulltextSearchResult quickSearch(QuickConditionBuilder conditionBuilder, int page, int size, String... indices);

    AdvancedSearchResult advancedSearch(ConditionBuilder var1, int var2, int var3, String... var4) throws Exception;

    long getPatientCount(ConditionBuilder var1, ConditionBuilder var2, QueryBuilder var3, String... var4);

    List<String> getPatientRegNos(ConditionBuilder var1, ConditionBuilder var2, String... var3);

    List<String> getPatientRegNos(ConditionBuilder var1, ConditionBuilder var2, QueryBuilder var3, String... var4);

    List<String> getMedicalRegNos(ConditionBuilder var1, ConditionBuilder var2, QueryBuilder var3, String... var4);

    SearchResult getAdmNos(ConditionBuilder var1, ConditionBuilder var2, Integer var3, Integer var4, String... var5);

    SearchResult getAdmNos(ConditionBuilder var1, ConditionBuilder var2, String... var3);

    SearchResult getAdmNos(ConditionBuilder var1, ConditionBuilder var2, String var3, Integer var4, Integer var5, Integer var6, String... var7);

    SearchResult getAdmNos(ConditionBuilder var1, ConditionBuilder var2, String var3, Integer var4, String... var5);

    SearchResult getAdmNos(QuickConditionBuilder var1, Integer var2, Integer var3, String... var4);

    SearchResult getAdmNos(QuickConditionBuilder var1, String... var2);

    List<Map<String, Object>> getAdmByRegNos(List<String> var1, String... var2);

    void scrollSearchPatient(ElasticMethodInterface var1, ConditionBuilder var2, ConditionBuilder var3, QueryBuilder var4, String... var5);

    void scrollSearchAdm(ElasticMethodInterface var1, ConditionBuilder var2, ConditionBuilder var3, QueryBuilder var4, String... var5);
}
