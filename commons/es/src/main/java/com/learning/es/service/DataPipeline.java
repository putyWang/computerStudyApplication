package com.learning.es.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boot.commons.excel.model.header.MainHeader;
import com.boot.commons.utils.StringUtils;
import com.boot.form.query.constants.ElasticConst;
import com.boot.form.query.model.CRFFieldInfo;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * es数据处理类
 *
 * @Author: wangpenghui
 * @Date: 2021/5/26 18:35
 */
@Data
public class DataPipeline {
    // 病例信息基本字段
//    private final static String[] BASE_FIELD = {"patient^pa_name", "patient^pa_gender", "patient^regno"};
    /**
     * 全部脱敏的字段，key为formguid，value为表单字段path及字段脱敏类型
     */
    Map<String, Map<String, Integer>> desensitizeMap;
    /**
     * 随访信息,随访id及随访名称
     */
    Map<Long, String> visitMap;
    /**
     * 需要返回的表单及字段信息,表单formguid、字段path及字段名称
     */
    Map<String, Map<String, String>> formFieldMap;
    /**
     * 表头展示字段信息
     */
    List<MainHeader> mainHeaders;

    public DataPipeline(Map<String, Map<String, Integer>> desensitizeMap,
                        List<CRFFieldInfo> crfFieldInfos,
                        Map<Long, String> visitMap) {
        this.desensitizeMap = desensitizeMap;
        this.visitMap = visitMap;

        build(crfFieldInfos);
    }

    /**
     * 构建参数
     *
     * @param crfFieldInfos
     */
    void build(List<CRFFieldInfo> crfFieldInfos) {
        // 添加病例基本信息字段
        List<String> baseFields = Arrays.asList(ElasticConst.ELASTIC_PAT_FIELD_NAME, ElasticConst.ELASTIC_PAT_FIELD_GENDER, ElasticConst.ELASTIC_FIELD_REGNO);
        Iterator<CRFFieldInfo> iterator = crfFieldInfos.iterator();
        while (iterator.hasNext()){
            CRFFieldInfo crfFieldInfo = iterator.next();
            String formGuid = crfFieldInfo.getFormGuid();
            String path = crfFieldInfo.getPath();
            // 过滤病例基本字段
            if ("patient".equals(formGuid) && baseFields.contains(path)){
                iterator.remove();
            }
        }
        crfFieldInfos.add(0, new CRFFieldInfo(0L, "病例信息", "病例姓名", ElasticConst.ELASTIC_PAT_FIELD_NAME,
                "input", ElasticConst.ELASTIC_PAT_FIELD_NAME, "patient"));
        crfFieldInfos.add(1, new CRFFieldInfo(0L, "病例信息", "病例性别", ElasticConst.ELASTIC_PAT_FIELD_GENDER,
                "input", ElasticConst.ELASTIC_PAT_FIELD_GENDER, "patient"));
        crfFieldInfos.add(2, new CRFFieldInfo(0L, "病例信息", "病例登记号", ElasticConst.ELASTIC_FIELD_REGNO,
                "input", ElasticConst.ELASTIC_FIELD_REGNO, "patient"));

        // 1.处理表单及字段信息
        buildFormFieldMap(crfFieldInfos);
        // 2.生成表头信息
        buildExportHearder();
    }


//    /**
//     * 处理es查询结果，返回导出至excel格式信息
//     *
//     * @param searchResult
//     * @return
//     */
//    public List<Map<String, Object>> processForExport(List<Map<String, Object>> searchResult) {
//        // 处理es查询结果，按病人分组
//        Map<String, List<Map<String, Object>>> searchResultMap = searchResult.stream().filter(map -> {
//            Object empi = map.get(ElasticConst.ELASTIC_FIELD_EMPI);
//            return empi != null && !StringUtils.isEmpty(empi.toString());
//        }).collect(Collectors.groupingBy(map -> map.get(ElasticConst.ELASTIC_FIELD_EMPI).toString()));
//        // 获取病人指定表单字段填写信息
//        List<Map<String, Object>> patientData = getPatientData(searchResultMap);
//        return patientData;
//    }

    /**
     * 处理es查询结果，返回表单字段填写信息
     *
     * @param searchResult
     * @return
     */
    public List<Map<String, Object>> processData(List<Map<String, Object>> searchResult) {
        if (CollectionUtils.isEmpty(searchResult)) {
            return null;
        }
        // 处理es查询结果，按病人分组
        Map<String, List<Map<String, Object>>> searchResultMap = searchResult.stream().filter(map -> {
            Object empi = map.get(ElasticConst.ELASTIC_FIELD_EMPI);
            return empi != null && !StringUtils.isEmpty(empi.toString());
        }).collect(Collectors.groupingBy(map -> map.get(ElasticConst.ELASTIC_FIELD_EMPI).toString()));

        // 获取指定表单字段填写信息
        return getPatientData(searchResultMap);
    }

    /**
     * 获取所有附件路径信息
     * @param searchResult
     * @return 返回指定empi下附件路径信息及原始名称信息
     */
    public Map<String, Map<String, String>> processForAttach(List<Map<String, Object>> searchResult) {
        if (CollectionUtils.isEmpty(searchResult)) {
            return null;
        }

        String empi;
        String curjoinField = "";
        String joinField = ElasticConst.ELASTIC_FIELD_JION_FIELD;
        String attachmentTable = ElasticConst.ELASTIC_FIELD_ATTACHMENT;
        String empiField = ElasticConst.ELASTIC_FIELD_EMPI;
        String attachFilePathField = ElasticConst.ELASTIC_ATTACH_FIELD_FILE_PATH;
        String attachFileNameField = ElasticConst.ELASTIC_ATTACH_FIELD_FILE_NAME;
        Map<String, Map<String, String>> attachFilePathMap = new HashMap<>();

        // 处理es查询结果，按病人分组
        for (Map<String, Object> objectMap : searchResult) {
            String attachDataStr = objectMap.get(attachmentTable) == null ?
                    "" : JSONObject.toJSONString(objectMap.get(attachmentTable));
            // 获取join_field字段值
            if (!(objectMap.get(joinField) instanceof String)) {
                Map<String, Object> joinFieldMap = (Map<String, Object>) objectMap.get(joinField);
                curjoinField = joinFieldMap.get("name") == null ? "" : joinFieldMap.get("name").toString();
            }else {
                curjoinField = "";
            }

            if (attachmentTable.equals(curjoinField)){
                // 解析附件数据
                Map<String, Object> attachMap = JSONObject.parseObject(attachDataStr, Map.class);
                if (!CollectionUtils.isEmpty(attachMap)){
                    String filePath = attachMap.get(attachFilePathField) == null ? "" : attachMap.get(attachFilePathField).toString();
                    String fileName = attachMap.get(attachFileNameField) == null ? "" : attachMap.get(attachFileNameField).toString();
                    if (!StringUtils.isEmpty(filePath)){
                        empi = objectMap.get(empiField) == null
                                ? "" : objectMap.get(empiField).toString();
                        Map<String, String> pathMap = attachFilePathMap.get(empi);
                        if (pathMap == null){
                            pathMap = new HashMap<>();
                        }
                        pathMap.put(filePath, fileName);
                        attachFilePathMap.put(empi, pathMap);
                    }
                }
            }
        }

        return attachFilePathMap;
    }

    /**
     * 处理病人信息成excel填写数据格式返回
     *
     * @param patientData
     * @return
     */
    public List<List<Object>> getExcelWriteData(List<Map<String, Object>> patientData) {
        List<List<Object>> excelWriteData = new ArrayList<>();
        if (CollectionUtils.isEmpty(patientData)) {
            return excelWriteData;
        }

        // 遍历病人信息
        for (Map<String, Object> curPatientData : patientData) {
            List<Object> rowData = new ArrayList<>();
            // 遍历表头信息，获取列信息
            for (MainHeader mainHeader : mainHeaders) {
                String key = mainHeader.getKeyName();
                Object value = curPatientData.get(key) == null ?
                        "" : curPatientData.get(key);
                rowData.add(value);
            }
            excelWriteData.add(rowData);
        }

        return excelWriteData;
    }


    /**
     * 获取所有病人表单字段填写信息
     *
     * @param searchResultMap es查询结果
     * @return
     */
    private List<Map<String, Object>> getPatientData(Map<String, List<Map<String, Object>>> searchResultMap) {
        String joinField = ElasticConst.ELASTIC_FIELD_JION_FIELD;
        String visitIdField = ElasticConst.ELASTIC_FIELD_VISIT_ID;
        String formGuidField = ElasticConst.ELASTIC_FIELD_FORM_GUID;
        String fillDataField = ElasticConst.ELASTIC_FIELD_FILL_DATA;
        String attachmentField = ElasticConst.ELASTIC_FIELD_ATTACHMENT;

        List<Map<String, Object>> result = new ArrayList<>();
        String curjoinField;

        // 遍历病人信息
        for (Map.Entry<String, List<Map<String, Object>>> entry : searchResultMap.entrySet()) {
            String regNo = entry.getKey();
            List<Map<String, Object>> curList = entry.getValue();
            Map<String, Object> patientMap = new LinkedHashMap<>();
            if (!CollectionUtils.isEmpty(curList)) {

                // 遍历查询到的ES数据
                for (Map<String, Object> curMap : curList) {
                    Long visitId = curMap.get(visitIdField) == null ?
                            0L : Long.parseLong(curMap.get(visitIdField).toString());
                    String formGuid = curMap.get(formGuidField) == null ?
                            "" : curMap.get(formGuidField).toString();
                    String fillDataStr = curMap.get(fillDataField) == null ?
                            "" : JSONObject.toJSONString(curMap.get(fillDataField));
                    String attachDataStr = curMap.get(attachmentField) == null ?
                            "" : JSONObject.toJSONString(curMap.get(attachmentField));

                    // 获取join_field字段值
                    if (curMap.get(joinField) instanceof String) {
                        // 获取病人表数据
                        curjoinField = curMap.get(joinField).toString();
                    } else {
                        Map<String, Object> joinFieldMap = (Map<String, Object>) curMap.get(joinField);
                        curjoinField = joinFieldMap.get("name") == null ? "" : joinFieldMap.get("name").toString();
                    }

                    if (ElasticConst.ELASTIC_FIELD_PATIENT.equals(curjoinField)){
                        // 获取病人基本信息
                        processPatientData(curMap, patientMap);
                    }else if (ElasticConst.ELASTIC_FIELD_CRF.equals(curjoinField)){
                        // 获取指定表单的字段填写数据,添加到病人所有信息中
                        processFillData(fillDataStr, formGuid, visitId, patientMap);
                    }else if (ElasticConst.ELASTIC_FIELD_ATTACHMENT.equals(curjoinField)){
                        // 获取附件相关数据
                        processAttachData(attachDataStr, attachmentField, patientMap);
                    }
                }

                result.add(patientMap);
            }
        }

        return result;
    }

    private void processPatientData(Map<String, Object> curMap, Map<String, Object> patientMap) {
        // 获取病例指定字段数据
        String patientField = ElasticConst.ELASTIC_FIELD_PATIENT;
        Map<String, String> fieldMap = formFieldMap.get(patientField);
        // 设置病例基本信息
        if (!CollectionUtils.isEmpty(fieldMap)){
            for (String key : fieldMap.keySet()) {
                patientMap.put(patientField + "^" + key, curMap.getOrDefault(key, ""));
            }
        }
    }

    private void processAttachData(String attachDataStr, String formGuid, Map<String, Object> patientMap) {
        if (StringUtils.isEmpty(attachDataStr)) {
            return;
        }else {

            // 获取附件查询字段
            Map<String, String> fieldMap = formFieldMap.get(formGuid);
            if (!CollectionUtils.isEmpty(fieldMap)){
                List<String> fields = new ArrayList<>(fieldMap.keySet());
                // 解析附件数据
                Map<String, Object> attachMap = JSONObject.parseObject(attachDataStr, Map.class);
                if (!CollectionUtils.isEmpty(attachMap)) {
                    // 遍历字段信息
                    for (String field : fields) {
                        String value = attachMap.get(field) == null ? "" : attachMap.get(field).toString();
                        // field中的"."替换为"^",因为"."在前端是特殊字符
                        String key = formGuid + "^" + field.replace(".", "^");
                        String oldValue = patientMap.get(key) == null ? "" : patientMap.get(key).toString();
                        String newValue = StringUtils.isEmpty(oldValue) ?
                                value : oldValue + "^" + value;
                        patientMap.put(key, newValue);
                    }
                }
            }
        }

    }


    /**
     * 获取指定表单的字段填写数据,添加到病人所有信息中
     *
     * @param fillDataStr 表单填写数据json字符串
     * @param formGuid    表单formGuid
     * @param visitId     随访id
     * @param patientMap  返回的病人数据
     */
    private void processFillData(String fillDataStr, String formGuid, Long visitId, Map<String, Object> patientMap) {
        if (StringUtils.isEmpty(fillDataStr)) {
            return;
        }else {
            // 获取指定表单指定字段填写数据
            Map<String, String> fieldMap = formFieldMap.get(formGuid);
            if (!CollectionUtils.isEmpty(fieldMap)){
                List<String> fields = new ArrayList<>(fieldMap.keySet());
                // 解析表单填写数据
                Map<String, String> fillDataMap = null;
                try{
                    fillDataMap = getCRFFieldValues(fillDataStr, fields);
                }catch (Exception e){
                    e.printStackTrace();
                }

                Object oldValueObject;
                if (!CollectionUtils.isEmpty(fillDataMap)) {
                    // 遍历字段信息
                    for (String field : fields) {
                        String value = fillDataMap.get(field) == null ? "" : fillDataMap.get(field);
                        // 返回key为随访id拼上表单formGuid，再拼上表单字段路径
                        // TODO 和随访关联起来
//            String key = visitId + "^" + formGuid + "^" + field;
                        // field中的"."替换为"^",因为"."在前端是特殊字符
                        String key = formGuid + "^" + field.replace(".", "^");
                        oldValueObject = patientMap.get(key);
                        String oldValue = oldValueObject == null ? "" : oldValueObject.toString();
                        // 不同表单填写数据用^分割
                        String newValue = oldValueObject == null
                                ? value : oldValue + "^" + value;
                        patientMap.put(key, newValue);
                    }
                }
            }
        }

    }


    /**
     * 获取指定表单指定字段填写数据
     *
     * @param fillDataStr 表单填写数据json字符串，es中的数据结构
     * @param fields      表单字段路径信息
     */
    public static Map<String, String> getCRFFieldValues(String fillDataStr, List<String> fields) {
        Map<String, String> fillDataMap = new HashMap<>();
        if (org.apache.commons.lang.StringUtils.isEmpty(fillDataStr)) {
            return fillDataMap;
        }
        JSONObject fillDataJson = JSONObject.parseObject(fillDataStr);
        for (String field : fields) {
            StringBuilder stringBuilder = new StringBuilder();
            getCRFFieldValue(stringBuilder, field, fillDataJson);
//            String curValue = "";
//            String[] paths = field.split("\\.");
//            if (paths.length == 1) {
//                curValue = fillDataJson.getString(paths[0]) == null ?
//                        "" : fillDataJson.getString(paths[0]);
//
//            } else if (paths.length > 1) {
//                // 如果crf表单字段为数组、对象、单选、多选、下拉框, 或者动态分组的基础字段类型
//                Object o = fillDataJson.get(paths[0]);
//                curValue = getInnerFieldValues(paths[1], o);
//
//            }else if (paths.length == 3) {
//                // 如果是动态分组，则表单填写数据有两层或三层结构
//                StringBuilder stringBuilder = new StringBuilder();
//                JSONArray jsonArray = fillDataJson.getJSONArray(paths[0]);
//                for (Object o : jsonArray) {
//                    if (o == null){
//                        continue;
//                    }
//                    JSONObject jsonObject = (JSONObject)o;
//                    curValue = getInnerFieldValues(paths[2], jsonObject.get(paths[1]));
//                    if (stringBuilder.length() > 0) {
//                        // 同一条表单填写数据中多个数据，如动态分组用|分割
//                        stringBuilder.append("|");
//                    }
//                    stringBuilder.append(curValue);
//                }
//
//                curValue = stringBuilder.toString();
//            }

            if (stringBuilder.length() > 0){
                fillDataMap.put(field, stringBuilder.toString());
            }
        }
        if (!CollectionUtils.isEmpty(fillDataMap)){
            // 逆向映射处理，从path映射到其他key值,这里是直接去掉最后的label字符串
            fillDataMap = reverseMapProcess(fillDataMap);
        }

        return fillDataMap;
    }

    /**
     * 获取单个crf表单字段填写值
     * @param stringBuilder
     * @param field 字段path
     * @param fillDataJson
     */
    private static void getCRFFieldValue(StringBuilder stringBuilder, String field, JSONObject fillDataJson) {
        String[] paths = field.split("\\.");
        if (paths.length == 1) {
            String curValue = fillDataJson.getString(paths[0]) == null
                    ? "" : fillDataJson.getString(paths[0]);
            stringBuilder.append(curValue);
        }else if (paths.length > 1){
            String firstField = field.substring(0, field.indexOf("."));
            String lastField = field.substring(field.indexOf(".") + 1);
            Object o = fillDataJson.get(firstField);
            if (o == null){
                return;
            }
            if (o instanceof JSONArray){
                JSONArray jsonArray = (JSONArray)o;
                // 外层数组全部用"[]"框起来
                stringBuilder.append("[");
                if (jsonArray != null && jsonArray.size() > 0){
                    for (Object curObject : jsonArray) {
                        getCRFFieldValue(stringBuilder, lastField, (JSONObject)curObject);
                        if (lastField.split("\\.").length == 1){
                            // 最内层数组用"&"拼接
                            stringBuilder.append("&");
                        }
                    }
                    // 如果是最内层数组拼接完，需要去除多余的"&"符号
                    if (lastField.split("\\.").length == 1){
                        // 去掉最后多余的一个"&"
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }

                }
                stringBuilder.append("]");
            }else if (o instanceof JSONObject){
                JSONObject curJsonObject = (JSONObject)o;
                stringBuilder.append("{");
                getCRFFieldValue(stringBuilder, lastField, curJsonObject);
                stringBuilder.append("}");
            }
        }

    }

    /**
     * 逆向映射处理，从path映射到其他key值,这里是直接去掉最后的label字符串
     * @param fillDataMap
     * @return
     */
    private static Map<String, String> reverseMapProcess(Map<String, String> fillDataMap) {
        if (!CollectionUtils.isEmpty(fillDataMap)){
            String newKey;
            Map<String, String> newFillDataMap = new HashMap<>();
            Iterator<Map.Entry<String, String>> iterator = fillDataMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.endsWith("label")){
                    newKey = key.substring(0, key.lastIndexOf("."));
                }else {
                    newKey = key;
                }

                if (!StringUtils.isEmpty(value)){
                    newFillDataMap.put(key, value);
                }
            }

            return newFillDataMap;
        }

        return null;
    }


    /**
     * 获取表单及字段 map信息
     *
     * @return
     */
    private void buildFormFieldMap(List<CRFFieldInfo> crfFieldInfos) {
        if (formFieldMap == null) {
            formFieldMap = new LinkedHashMap<>();
        }

        if (CollectionUtils.isEmpty(crfFieldInfos)) {
            return;
        }

        for (CRFFieldInfo crfFieldInfo : crfFieldInfos) {
            String formGuid = crfFieldInfo.getFormGuid();
            String path = crfFieldInfo.getPath();
            String name = crfFieldInfo.getName();
            String formName = crfFieldInfo.getFormName();

            if (StringUtils.isEmpty(formGuid) || StringUtils.isEmpty(path) || StringUtils.isEmpty(name)) {
                continue;
            }
            if (!formFieldMap.containsKey(formGuid)) {
                Map<String, String> fieldMap = new LinkedHashMap<>();
                formFieldMap.put(formGuid, fieldMap);
            }

            Map<String, String> fieldMap = formFieldMap.get(formGuid);
            // 每个字段对应的名称为表单名称拼上字段名
            fieldMap.put(path, formName + "_" + name);
            formFieldMap.put(formGuid, fieldMap);
        }
    }

    /**
     * 生成导出表头信息
     */
    private void buildExportHearder() {
        if (mainHeaders == null) {
            mainHeaders = new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(formFieldMap)) {
            return;
        }

        // TODO 和随访关联起来
//        Map<String, Object> curMap = new HashMap<>();
//        for (String key : curMap.keySet()) {
//            if (StringUtils.isEmpty(key)) {
//                continue;
//            }
//            String[] split = key.split("\\^");
//            if (split != null && split.length > 2) {
//                Long visitId = Long.parseLong(split[0]);
//                String formGuid = split[1];
//                String path = split[2];
//                String visitName = visitMap.get(visitId) == null ? "" : visitMap.get(visitId);
//                String fieldName = formFieldMap.get(formGuid).get(path) == null ? "" : formFieldMap.get(formGuid).get(path);
//                // 返回病人信息key值为随访名称拼上表单字段名称
//                String keyDesc = visitName + "_" + fieldName;
//                mainHeaders.add(new MainHeader(keyDesc, key));
//            }
//        }

        for (Map.Entry<String, Map<String, String>> entry : formFieldMap.entrySet()) {
            String formGuid = entry.getKey();
            Map<String, String> value = entry.getValue();
            for (Map.Entry<String, String> curEntry : value.entrySet()) {
                String path = curEntry.getKey();
                // path中的"."替换为"^",因为"."在前端是特殊字符
                String key = StringUtils.isEmpty(formGuid) ? path.replace(".", "^") :
                        formGuid + "^" + path.replace(".", "^");
                String fieldName = curEntry.getValue();
                String keyDesc = fieldName;
                mainHeaders.add(new MainHeader(keyDesc, key));
            }
        }
    }

}
