package com.learning.core.utils;

import java.util.*;
import java.util.stream.Collectors;

public class ListUtils {
    public ListUtils() {
    }

    /**
     * 创建相应的列表
     * @param isLinked
     * @param <T>
     * @return
     */
    public static <T> List<T> list(boolean isLinked) {
        return isLinked ? new LinkedList<>() : new ArrayList<>();
    }

    /**
     * 将Collection转换为相应的列表
     * @param isLinked
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> List<T> list(boolean isLinked, Collection<? extends T> collection) {
        if (null == collection) {
            return list(isLinked);
        } else {
            return isLinked ? new LinkedList<>(collection) : new ArrayList<>(collection);
        }
    }

    /**
     * 将T数组转化为List<T>
     * @param isLinked
     * @param values
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> List<T> list(boolean isLinked, T... values) {
        if (ArrayUtils.isEmpty(values)) {
            return list(isLinked);
        } else {
            List<T> list = isLinked ? new LinkedList<>() : new ArrayList<>(values.length);
            Collections.addAll(list, values);
            return list;
        }
    }

    /**
     * 将迭代器中剩下元素放在新链表中
     * @param isLinked
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> List<T> list(boolean isLinked, Iterable<? extends T> iterable) {
        return null == iterable ? list(isLinked) : list(isLinked, iterable.iterator());
    }

    /**
     * 将迭代器中剩下元素放在新List中
     * @param isLinked
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> List<T> list(boolean isLinked, Iterator<? extends T> iter) {
        List<T> list = list(isLinked);
        if (null != iter) {
            while(iter.hasNext()) {
                list.add(iter.next());
            }
        }

        return list;
    }

    /**
     * 将集合转化为ArrayList
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> toArrayList(Collection<? extends T> collection) {
        return (ArrayList<T>) list(false, collection);
    }

    /**
     * 将iterable转化为ArrayList
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> toArrayList(Iterable<? extends T> iterable) {
        return (ArrayList<T>)list(false, iterable);
    }

    /**
     * 将iterator转化为ArrayList
     * @param iterator
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> toArrayList(Iterator<? extends T> iterator) {
        return (ArrayList<T>)list(false, iterator);
    }

    /**
     * 将数组转化为ArrayList
     * @param values
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> ArrayList<T> toArrayList(T... values) {
        return (ArrayList<T>)list(false, values);
    }

    /**
     * 将数组转化为LinkedList
     * @param values
     * @param <T>
     * @return
     */
    @SafeVarargs
    public static <T> LinkedList<T> toLinkedList(T... values) {
        return (LinkedList)list(true, values);
    }

    /**
     * 将列表排序
     * @param values
     * @param <T>
     * @return
     */
    public static <T> List<T> sort(List<T> list, Comparator<? super T> c) {
        list.sort(c);
        return list;
    }

    /**
     * 将列表进行旋转
     * @param values
     * @param <T>
     * @return
     */
    public static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

    /**
     * 对List去重
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> distinct(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        } else {
            return list.stream().distinct().collect(Collectors.toList());
        }
    }

    /**
     * 清空列表
     * @param <T>
     * @return
     */
    public static <T> List<T> empty() {
        return Collections.emptyList();
    }
}
