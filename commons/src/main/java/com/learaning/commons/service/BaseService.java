package com.learaning.commons.service;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.learaning.commons.Annotion.Query;
import com.learaning.commons.Annotion.UnionUnique;
import com.learaning.commons.Annotion.UnionUniqueCode;
import com.learaning.commons.Annotion.Unique;
import com.learaning.commons.dto.BaseDto;
import com.learaning.commons.entity.BaseEntity;
import com.learaning.commons.enums.ApiCode;
import com.learaning.commons.enums.BaseOperationEnum;
import com.learaning.commons.exception.ExceptionBuilder;
import com.learaning.commons.param.PageParam;
import com.learaning.commons.param.SortPageParam;
import com.learaning.commons.utils.CollectionUtils;
import com.learaning.commons.utils.ReflectionUtils;
import com.learaning.commons.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public interface
BaseService<D extends BaseDto, T extends BaseEntity>
        extends IService <T> {

    /**
     * 分页函数
     * @param page
     * @return
     */
    default IPage<T> page(PageParam page, Wrapper<T> queryWrapper) {

        Page<T> iPage = new Page<>();
        iPage.setCurrent(page.getCurrent());
        iPage.setSize(page.getPageSize());

        //当分页中需要排序时
        if (page instanceof SortPageParam) {
            SortPageParam sortPage = (SortPageParam) page;
            List<String> sorts = sortPage.getSorts();
            List<String> acSs = sortPage.getACSs();

            if (CollectionUtils.isNotEmpty(sorts)) {
                OrderItem[] orderItems = new OrderItem[sorts.size()];
                //设置排序规则
                if (CollectionUtils.isEmpty(acSs)) {
                    for (int i = 0; i < sorts.size(); i++) {
                        orderItems[i] = build(sorts.get(i), "ASC");
                    }
                }else if (acSs.size() < sorts.size()) {
                    for (int i = 0; i < sorts.size(); i++) {
                        orderItems[i] = build(sorts.get(i), acSs.get(0));
                    }
                }else {
                    for (int i = 0; i < sorts.size(); i++) {
                        orderItems[i] = build(sorts.get(i), acSs.get(i));
                    }
                }

                iPage.addOrder(orderItems);
            }
        }

        //当设置了查询条件时直接查询
        if (queryWrapper != null) {

            return this.getBaseMapper().selectPage(iPage, queryWrapper);
        }

        //未设置查询条件时，根据page提供策略对wrapper进行重构
        Wrapper<T> wrapper = getWrapper(page);

        return this.getBaseMapper().selectPage(iPage, extensionWrapper(page, wrapper));
    }

    default Wrapper<T> extensionWrapper(PageParam page, Wrapper<T> wrapper) {

        return wrapper;
    }

    /**
     * 单条插入
     * @param t
     * @return
     */
    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean insert(T t) {
        if (t == null) {

            return true;
        }

        processBeforeOperation(t, BaseOperationEnum.INSERT);
        checkUniqueField(t, false);
        boolean save = save(t);

        if (!save) {
            throw ExceptionBuilder.build("插入失败");
        } else {
            this.processAfterOperation(t, BaseOperationEnum.INSERT);
            this.clearBusinessCache(t, BaseOperationEnum.INSERT);
            return true;
        }
    }

    /**
     * 批量插入
     * @param list
     * @return
     */
    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean insertBatch(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {

            return true;
        }else {
            this.processBeforeBatchOperation(list, BaseOperationEnum.BATCH_INSERT);
            boolean result = this.saveBatch(list);
            list.forEach((entity) -> {
                this.clearBusinessCache(entity, BaseOperationEnum.INSERT);
            });
            if (!result) {
                throw ExceptionBuilder.build("插入失败");
            } else {
                this.processAfterBatchOperation(list, BaseOperationEnum.BATCH_INSERT);
                return true;
            }
        }

    }

    /**
     * 单条更新
     * @param t
     * @return
     */
    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean update(T t) {
        if (t == null) {

            return true;
        }

        processBeforeOperation(t, BaseOperationEnum.UPDATE);
        checkUniqueField(t, true);

        if (t instanceof BaseEntity) {
            this.clearBusinessCache(t, BaseOperationEnum.DELETE);
        }

        boolean update = updateById(t);

        if (!update) {
            throw ExceptionBuilder.build("更新失败");
        } else {
            this.processAfterOperation(t, BaseOperationEnum.UPDATE);
            this.clearBusinessCache(t, BaseOperationEnum.UPDATE);
            return true;
        }
    }

    /**
     * 单条删除
     * @param id
     * @return
     */
    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean delete(Serializable id) {
        T t = this.getById(id);

        if (t == null) {
            throw ExceptionBuilder.build("未查询到该对象");
        }

        processBeforeOperation(t, BaseOperationEnum.DELETE);
        boolean delete = removeById(id);

        if (! delete) {
            throw ExceptionBuilder.build("删除失败");
        } else {
            clearBusinessCache(t, BaseOperationEnum.DELETE);
            processAfterOperation(t, BaseOperationEnum.DELETE);

            return true;
        }

    }

    /**
     * 批量删除数据
     * @param ids
     * @return
     */
    default boolean deleteBatch(List<Serializable> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            return true;
        }

        List<T> ts = this.getBaseMapper().selectBatchIds(ids);

        if (CollectionUtils.isEmpty(ts)) {
            throw ExceptionBuilder.build("该对象不存在");
        }

        processBeforeBatchOperation (ts, BaseOperationEnum.BATCH_DELETE);
        boolean result = SqlHelper.retBool(this.getBaseMapper().deleteBatchIds(ids));

        if (result) {
            ts.stream().forEach((t) -> {
                clearBusinessCache(t, BaseOperationEnum.BATCH_DELETE);
            });
            processAfterBatchOperation(ts, BaseOperationEnum.BATCH_DELETE);

            return true;
        } else
            throw ExceptionBuilder.build("删除失败");

    }

    /**
     * 前部钩子函数
     * @param t
     * @param baseOperationEnum
     */
    default void processBeforeOperation (T t, BaseOperationEnum baseOperationEnum) {}

    /**
     * 处理后钩子函数
     * @param t
     * @param baseOperationEnum
     */
    default void processAfterOperation (T t, BaseOperationEnum baseOperationEnum) {}

    /**
     * 批量前部钩子函数
     * @param list
     * @param baseOperationEnum
     */
    default void processBeforeBatchOperation (List<T> list, BaseOperationEnum baseOperationEnum) {}

    /**
     * 处理后钩子函数
     * @param list
     * @param baseOperationEnum
     */
    default void processAfterBatchOperation (List<T> list, BaseOperationEnum baseOperationEnum) {}

    /**
     * 进行非重验证
     * @param entity
     * @param isUpdate
     */
    default void checkUniqueField(T entity, boolean isUpdate) {
        Field[] allFields = ReflectionUtils.getAllFieldsArr(entity);

        //获取相应的表id字段
        Optional<Field> idFiledOptional = Arrays.stream(allFields)
                .filter((field) -> {
            return field.isAnnotationPresent(TableId.class);
        }).findFirst();

        //验证id是否不重复
        if (idFiledOptional.isPresent()) {
            Field idField = idFiledOptional.get();
            idField.setAccessible(true);

            for(int i = 0; i < allFields.length; ++ i) {
                Field field = allFields[i];

                if (field.isAnnotationPresent(Unique.class)) {
                    Unique unique = field.getDeclaredAnnotation(Unique.class);
                    QueryWrapper wrapper = Wrappers.query();

                    try {
                        Object value = this.getFieldValue(entity, field);
                        String column;
                        if (StringUtils.isBlank(unique.column())) {
                            column = StringUtils.camelToUnderline(field.getName());
                        } else {
                            column = unique.column();
                        }

                        wrapper.eq(column, value);
                        if (isUpdate) {
                            wrapper.ne((idField.getAnnotation(TableId.class)).value(), idField.get(entity));
                        }
                    } catch (Exception E) {
                        continue;
                    }

                    if (this.getBaseMapper().selectCount(wrapper) > 0) {
                        String errorMeg = unique.code();
                        if (StringUtils.isBlank(errorMeg)) {
                            errorMeg = unique.apiCode().getMessage();
                        }

                        throw ExceptionBuilder.build(ApiCode.DAO_EXCEPTION.getCode(), errorMeg, new Object[]{field.getName()});
                    }
                }
            }

            // 存储组名称
            Map<String, QueryWrapper<T>> unionUniqueMap = new HashMap();

            //为相应组设置相应查询语句
            for(int i = 0; i < allFields.length; ++ i) {
                Field field = allFields[i];
                if (field.isAnnotationPresent(UnionUnique.class)) {
                    try {
                        UnionUnique[] unionUniques = field.getDeclaredAnnotationsByType(UnionUnique.class);

                        for(int j = 0; j < unionUniques.length; ++ j) {
                            UnionUnique unionUnique = unionUniques[j];
                            String group = unionUnique.group();
                            Object value = this.getFieldValue(entity, field);
                            String column;

                            if (StringUtils.isBlank(unionUnique.column())) {
                                column = StringUtils.camelToUnderline(field.getName());
                            } else {
                                column = unionUnique.column();
                            }

                            QueryWrapper unionWrapper;
                            if (unionUniqueMap.containsKey(group)) {
                                unionWrapper = unionUniqueMap.get(group);
                                unionWrapper.eq(column, value);
                            } else {
                                unionWrapper = Wrappers.query();
                                unionWrapper.eq(column, value);
                                unionUniqueMap.put(group, unionWrapper);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }


            Set<Map.Entry<String, QueryWrapper<T>>> entries = unionUniqueMap.entrySet();
            Iterator iterator = entries.iterator();

            while(true) {
                Map.Entry entry;
                Integer result;
                do {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    entry = (Map.Entry)iterator.next();
                    QueryWrapper<T> queryWrapper = (QueryWrapper)entry.getValue();
                    if (isUpdate) {
                        try {
                            queryWrapper.ne((idField.getAnnotation(TableId.class)).value(), idField.get(entity));
                        } catch (Exception e) {
                            return;
                        }
                    }

                    result = this.getBaseMapper().selectCount(queryWrapper);
                } while(result <= 0);

                String group = (String)entry.getKey();
                Class<? extends BaseEntity> aClass = entity.getClass();
                UnionUniqueCode[] unionUniqueCodes = aClass.getAnnotationsByType(UnionUniqueCode.class);

                for(int i = 0; i < unionUniqueCodes.length; ++ i) {
                    UnionUniqueCode unionUniqueCode = unionUniqueCodes[i];
                    if (StringUtils.equals(unionUniqueCode.group(), group)) {
                        throw ExceptionBuilder.build(unionUniqueCode.code());
                    }
                }
            }
        }
    }

    /**
     * 清除缓存
     * @param entity
     * @param baseOperationEnum
     */
    default void clearBusinessCache(T entity, BaseOperationEnum baseOperationEnum) {}

    /**
     * 将page中的搜索策略加入到warpper中
     * @param page
     * @return
     */
    default Wrapper<T> getWrapper(PageParam page) {

        QueryWrapper<T> queryWrapper = new QueryWrapper();
        //获取所有字段
        Field[] declaredFields = page.getClass().getDeclaredFields();

        Arrays.stream(declaredFields).filter((field) -> {
            if (field.isAnnotationPresent(Query.class)) {
                Query query = field.getAnnotation(Query.class);
                return query.where();
            }else
                return false;
        }).forEach((field) -> {
            try {
                field.setAccessible(true);
                String column;

                //设置字段名
                if (field.isAnnotationPresent(Query.class)
                        && ! StringUtils.isBlank(field.getAnnotation(Query.class).column())) {
                    column = field.getAnnotation(Query.class).column();
                }else {
                    column = StringUtils.camelToUnderline(field.getName());
                }

                //判断字段值是否为字符串，或者是否为list
                if (!(field.get(page) instanceof String)
                        && ! StringUtils.isNoneBlank(new CharSequence[]{(String)field.get(page)})) {
                    return;
                }

                //设置查询条件
                if (field.isAnnotationPresent(Query.class)) {
                    switch (field.getAnnotation(Query.class).value()) {
                        case LIKE:
                            String valueLike = String.valueOf(field.get(page));
                            if (valueLike.contains("%")) {
                                valueLike = valueLike.replace("%", "\\%");
                            }else if (valueLike.contains("_")) {
                                valueLike = valueLike.replace("_", "\\_");
                            }

                            queryWrapper.like(column, valueLike);
                            break;
                        case IN:
                            Object value = field.get(page);

                            if (value instanceof List) {
                                queryWrapper.in(column, value);
                            }else if (value instanceof String) {
                                String[] split = ((String)value).split(",");
                                List<String> list = Arrays.asList(split);
                                queryWrapper.in(column, list);
                            }

                            break;
                        case GT:
                            queryWrapper.gt(column,field.get(page));
                            break;
                        case GE:
                            queryWrapper.ge(column,field.get(page));
                            break;
                        case LT:
                            queryWrapper.lt(column,field.get(page));
                            break;
                        case LE:
                            queryWrapper.le(column, field.get(page));
                            break;
                        case BWT:
                            String[] split = field.get(page).toString().split(",");
                            if (split.length == 2) {
                                queryWrapper.between(column, split[0], split[1]);
                            } else if (split.length == 1) {
                                queryWrapper.ge(column, split[0]);
                            }
                            break;
                        default:
                            queryWrapper.eq(column, field.get(page));
                    }
                } else {
                    queryWrapper.eq(column, field.get(page));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        return queryWrapper;
    }

    /**
     * 建立
     * @param sort
     * @param order
     * @return
     */
    default OrderItem build(String sort, String order){

        //将sort转化为表字段的形式
        String column = StringUtils.camelToUnderline(sort);

        if("ASC" == order) {

            return OrderItem.asc(column);
        }else

            return OrderItem.desc(column);
    }

    /**
     * 获取相应字段值
     * @param entity
     * @param field
     * @return
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    default Object getFieldValue(T entity, Field field)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        PropertyDescriptor propertyDescriptor =
                new PropertyDescriptor(field.getName(), entity.getClass());

        Method readMethod = propertyDescriptor.getReadMethod();

        return readMethod.invoke(entity);
    }

}
