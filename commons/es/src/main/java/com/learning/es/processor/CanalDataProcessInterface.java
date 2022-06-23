package com.learning.es.processor;

import com.learning.es.model.CanalEntryModel;
import com.learning.es.model.elastic.ElasticDocModel;

import java.util.List;

/**
 * 消费端 CanalEntryModel数据转ElasticDocModel数据 方法接口
 */
public interface CanalDataProcessInterface {

    /**
     * 消费canal日志信息，同步数据至es
     *
     * @param entries
     */
    List<ElasticDocModel> dataProcess(List<CanalEntryModel> entries);
}
