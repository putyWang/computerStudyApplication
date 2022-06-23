package com.learning.es;

import com.learning.es.service.FormClusterService;
import com.learning.es.service.FormDocumentService;
import com.learning.es.service.FormIndexService;
import com.learning.es.service.FormQueryService;
import com.learning.es.service.impl.FormClusterServiceImpl;
import com.learning.es.service.impl.FormDocumentServiceImpl;
import com.learning.es.service.impl.FormIndexServiceImpl;
import com.learning.es.service.impl.FormQueryServiceImpl;

/**
 * 表单ES服务管理
 *
 * @author ：wangpenghui
 * @date ：Created in 2021/3/15 14:43
 */
public final class FormElasticManager {
    private static FormElasticManager instance = null;

    private FormQueryService formQueryService;

    private FormIndexService formIndexService;

    private FormDocumentService formDocumentService;

    private FormClusterService formClusterService;

    public static FormElasticManager getInstance() {
        if (instance == null) {
            synchronized (FormElasticManager.class) {
                if (instance == null) {
                    instance = new FormElasticManager();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public static FormQueryService query() {
        return getInstance().formQueryService;
    }

    public static FormIndexService index() {
        return getInstance().formIndexService;
    }

    public static FormDocumentService document() {
        return getInstance().formDocumentService;
    }

    public static FormClusterService cluster() {
        return getInstance().formClusterService;
    }

    private FormElasticManager() {
        formQueryService = new FormQueryServiceImpl();
        formIndexService = new FormIndexServiceImpl();
        formDocumentService = new FormDocumentServiceImpl();
        formClusterService = new FormClusterServiceImpl();
    }

}
