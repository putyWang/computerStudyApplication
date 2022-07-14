package com.learning.core.bean.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.*;

/**
 * /创建的监听器必须继承 AnalysisEventListener，泛型为刚刚具体的Bean的实体类
 *
 * @author felix
 */
@Log4j2
public class ReadExcelListener<T> extends AnalysisEventListener<T> {

    //由于数据是按行读取，所以此处需要创建两个静态变量来实现批量插入
    private static int vipCount;
    private static int BATCH = 2000;

    List<T> list = new ArrayList<T>();
    Map<Integer, String> head = new HashMap<>();
    Class classz;

    public ReadExcelListener(Class c) {
        classz = c;
    }


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        list.add(data);
        vipCount++;
        //得到当前操作表格的所有行数，由于行数包含了表头，所以需要减一为所有数据的条目数
        Integer a = context.getTotalCount() - 1;
        //读取的行数到行尾时，将剩下的数据全部插入到数据库中
        if (a == vipCount) {
//            addVipsService.addVip(list);
        }

        //每当list中存储的条目数达到2000条时，批量插入到数据库中，并清空当前list的数据
        if (list.size() % BATCH == 0) {
//            addVipsService.addVip(list);
            list.clear();
        }
    }

    /**
     * 重写invokeHeadMap 方法 验证表头
     *
     * @param headMap
     * @param context
     */
    @SneakyThrows
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {

        if (null == head || head.size() <= 0) {
            head = getIndexNameMap(classz);
        }
        if (CollectionUtils.isEmpty(head)) {
            return;
        }
        Set<Integer> keySet = head.keySet();
        for (Integer key : keySet) {
            //headMap 是从0开始
            if (StringUtils.isEmpty(headMap.get(key))) {
                throw new ExcelAnalysisException("解析excel出错，请传入正确格式的excel");
            }
            if (!headMap.get(key).equals(head.get(key))) {
                log.info(headMap.get(key));
                log.info(head.get(key));
                throw new ExcelAnalysisException("解析excel出错，请传入正确格式的excel");
            }
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //清除数据
        log.info("所有数据解析完成！");
    }

    public List<T> getDatas() {
        return list;
    }

    /**
     * 通过反射来 判断excel 的格式和数据库格式是否一致
     *
     * @param clazz
     * @return
     * @throws NoSuchFieldException
     */
    public Map<Integer, String> getIndexNameMap(Class clazz) throws NoSuchFieldException {
        Map<Integer, String> result = new HashMap<>();
        Field field;
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            field = clazz.getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            //在实体类上加上这个注解  即可去除多余的字段 也可以获取表结构
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                int index = excelProperty.index();
                String[] values = excelProperty.value();
                StringBuilder value = new StringBuilder();
                for (String v : values) {
                    value.append(v);
                }
                result.put(index, value.toString());
            }
        }
        return result;
    }

}
