package com.learning.es.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 查询参数
 *
 * @author ：wangpenghui
 * @date ：Created in 2021/3/19 16:23
 */
@Accessors(chain = true)
@Data
public class QueryParam {
    /**
     * 查询条件
     */
    QueryJson queryJson;
    /**
     * 需要返回的表单字段信息
     */
    List<CRFFieldInfo> crfFieldInfos;
    /**
     * 导出文件名称
     */
    String fileName;
}
