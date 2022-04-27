package com.learaning.demo.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.learaning.demo.Annotion.Query;
import com.learaning.demo.dto.BaseDto;
import com.learaning.demo.entity.BaseEntity;
import com.learaning.demo.param.PageParam;
import com.learaning.demo.param.SortPageParam;
import com.learaning.demo.utils.CollectionUtils;
import com.learaning.demo.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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
}
