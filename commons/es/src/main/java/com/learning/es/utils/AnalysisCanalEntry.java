package com.learning.es.utils;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.learning.es.model.CanalEntryModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author wangpenghui
 * @createTime 2021年08月17日 10:50:00
 */
@Slf4j
public class AnalysisCanalEntry {

    /**
     * 解析Entry为model类
     *
     * @param entry
     */
    public static CanalEntryModel parseEntryToModel(CanalEntry.Entry entry) throws InvalidProtocolBufferException {
        // 解析日志数据
        if (entry != null) {
            if (CanalEntry.EntryType.ROWDATA == entry.getEntryType()) {
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                CanalEntry.EventType eventType = rowChange.getEventType();
                List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                // 数据库信息
                String schemaName = entry.getHeader().getSchemaName();
                // 获取表名称
                String tableName = entry.getHeader().getTableName();
                // 过滤表名称
                // 解析数据为map结构
                List<Map<String, Object>> maps = parseColumnsToMap(rowDatasList);

                if (!CollectionUtils.isEmpty(maps)) {
                    log.info("解析成map结构数据： " + maps.toString());
                    // 生成entry model类
                    CanalEntryModel model = new CanalEntryModel();
                    model.setSchemaName(schemaName);
                    model.setTableName(tableName);
                    model.setEventTypeEnum(eventType);
                    model.setDatas(maps);

                    return model;
                }
            }
        }

        return null;
    }

    /**
     * 解析每条sql数据为map结构
     *
     * @param rowDatasList
     * @return
     */
    public static List<Map<String, Object>> parseColumnsToMap(List<CanalEntry.RowData> rowDatasList) {
        if (!CollectionUtils.isEmpty(rowDatasList)) {
            List<Map<String, Object>> result = new ArrayList<>();
            Iterator<CanalEntry.RowData> iterator = rowDatasList.iterator();
            Map<String, Object> jsonMap;

            // 遍历所有sql数据
            while (iterator.hasNext()) {
                CanalEntry.RowData rowData = iterator.next();
                List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                if (CollectionUtils.isEmpty(columns)){
                    // 如果是删除操作，则是获取删除前的数据
                    columns = rowData.getBeforeColumnsList();
                }

                jsonMap = new HashMap<>();
                // 遍历所有字段信息
                Iterator<CanalEntry.Column> columnIterator = columns.iterator();
                while (columnIterator.hasNext()) {
                    CanalEntry.Column column = columnIterator.next();
                    if (column == null) {
                        continue;
                    }
                    jsonMap.put(column.getName(), column.getValue());
                }

                result.add(jsonMap);
            }

            return result;
        } else {
            return null;
        }

    }
}
