package com.learning.commons.utils;

import java.lang.reflect.Array;
import java.util.*;

public class ArrayUtils {
    public static final int INDEX_NOT_FOUND = -1;

    public ArrayUtils() {
    }

    /**
     * 判断数组是否为空
     * @param array
     * @param <T>
     * @return
     */
    public static <T> boolean isEmpty(T[] array) {
        return null == array || array.length == 0;
    }

    /**
     * 判断对象是否为空数组
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (null != obj && isArray(obj)) {
            return 0 == Array.getLength(obj);
        } else {
            return true;
        }
    }

    /**
     * 判断对象是否为数组
     * @param obj
     * @return
     */
    public static boolean isArray(Object obj) {
        return null != obj && obj.getClass().isArray();
    }

    /**
     * 若数组为空，设为默认数组
     * @param array
     * @param defaultArray
     * @param <T>
     * @return
     */
    public static <T> T[] defaultIfEmpty(T[] array, T[] defaultArray) {
        return isEmpty(array) ? defaultArray : array;
    }

    /**
     * 判断两数组长度是否相等
     * @param array1
     * @param array2
     * @return
     */
    public static boolean isArraySameLength(Object[] array1, Object[] array2) {
        return (isEmpty(array1) ? 0 : Array.getLength(array1)) == (isEmpty(array2) ? 0 : Array.getLength(array2));
    }

    /**
     * 判断数组是否不为空
     * @param array
     * @param <T>
     * @return
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    /**
     * 旋转数组
     * @param array
     * @param <T>
     */
    public static <T> void arrayReverse(T[] array) {
        if (!isEmpty(array)) {
            int i = 0;

            for(int j = array.length - 1; j > i; --j) {
                T temp = array[j];
                array[j] = array[i];
                array[i] = temp;
                ++i;
            }

        }
    }

    /**
     * 判断数组是否含有空函数
     * @param array
     * @param <T>
     * @return
     */
    public static <T> boolean hashNull(T... array) {
        if (isNotEmpty(array)) {

            for (T element : array) {
                if (null == element) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 寻找数组中第一个非空元素
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T fistNonNullEl(T... array) {
        if (isNotEmpty(array)) {

            for (T element : array) {
                if (null != element) {
                    return element;
                }
            }
        }

        return null;
    }

    /**
     * 判断数组是否拥有某元素
     * @param array
     * @param value
     * @param <T>
     * @return
     */
    public static <T> boolean contains(T[] array, T value) {
        return arrayIndexOf(array, value, 0) > -1;
    }

    /**
     *
     * @param array
     * @param value
     * @return
     */
    public static boolean containsIgnoreCase(CharSequence[] array, CharSequence value) {
        return arrayIndexOfIgnoreCase(array, value) > -1;
    }

    /**
     * 判断values是否与array相交
     * @param array
     * @param values
     * @param <T>
     * @return
     */
    public static <T> boolean containsAny(T[] array, T... values) {

        for (T value : array) {

            if (contains(array, value)) {
                return true;
            }
        }

        return false;
    }

    public static <T> int arrayIndexOf(T[] array, Object value) {
        return arrayIndexOf(array, value, 0);
    }

    public static <T> int arrayIndexOf(T[] array, Object value, int startIndex) {
        if (isEmpty(array)) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        } else if (startIndex > array.length) {
            return -1;
        }

        int i;
        if (value == null) {
            for(i = 0; i < array.length; ++i) {
                if (null == array[i]) {
                    return i;
                }
            }
        } else {
            for(i = startIndex; i < array.length; ++i) {
                if (ObjectUtils.equal(array[i], value)) {
                    return i;
                }
            }
        }

        return -1;

    }

    public static int arrayIndexOfIgnoreCase(CharSequence[] array, CharSequence value) {
        return arrayIndexOfIgnoreCase(array, value, 0);
    }

    public static int arrayIndexOfIgnoreCase(CharSequence[] array, CharSequence value, int startIndex) {
        if (isEmpty((Object[])array)) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            } else if (startIndex > array.length) {
                return -1;
            }

            int i;
            if (value == null) {
                for(i = 0; i < array.length; ++i) {
                    if (null == array[i]) {
                        return i;
                    }
                }
            } else {
                for(i = startIndex; i < array.length; ++i) {
                    if (StringUtils.equals(value, array[i], true)) {
                        return i;
                    }
                }
            }

            return -1;
        }
    }

    public static <T> int arrayLastIndexOf(T[] array, Object value) {
        return arrayLastIndexOf(array, value, 2147483647);
    }

    public static <T> int arrayLastIndexOf(T[] array, Object value, int startIndex) {
        if (isEmpty(array)) {
            return -1;
        } else if (startIndex < 0) {
            return -1;
        } else {
            if (startIndex >= array.length) {
                startIndex = array.length - 1;
            }

            int i;
            if (value == null) {
                for(i = array.length - 1; i >= startIndex; --i) {
                    if (array[i] == null) {
                        return i;
                    }
                }
            } else {
                for(i = array.length - 1; i >= startIndex; --i) {
                    if (ObjectUtils.equal(array[i], value)) {
                        return i;
                    }
                }
            }

            return -1;
        }
    }

    public static <T> T get(Object[] array, int index) {
        if (null == array) {
            return null;
        } else {
            if (index < 0) {
                index = 0;
            } else if (index >= array.length) {
                index = array.length - 1;
            }

            return (T) Array.get(array, index);
        }
    }

    public static <T> T[] get(Object[] array, int... indexes) {
        if (null == array) {
            return null;
        } else {
            T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), indexes.length);

            for(int i = 0; i < indexes.length; ++i) {
                result[i] = get(array, indexes[i]);
            }

            return result;
        }
    }

    public static <T> T[] remove(T[] array, int index) {
        return (T[])(remove((Object)array, index));
    }

    public static Object remove(Object array, int index) {
        if (!isArray(array)) {
            return array;
        } else {
            int len = Array.getLength(array);
            if (index >= 0 && index < len) {
                Object result = Array.newInstance(array.getClass().getComponentType(), len - 1);
                System.arraycopy(array, 0, result, 0, index);
                if (index < len - 1) {
                    System.arraycopy(array, index + 1, result, index, len - index - 1);
                }

                return result;
            } else {
                return array;
            }
        }
    }

    public static <T> T[] removeEl(T[] array, T element) {
        return remove(array, arrayIndexOf(array, element));
    }

    public static <T> T[] distinct(T[] array) {
        if (isEmpty(array)) {
            return array;
        } else {
            Set<T> set = new LinkedHashSet(array.length, 1.0F);
            Collections.addAll(set, array);
            return set.toArray(newArray(array.getClass().getComponentType(), 0));
        }
    }

    public static <T> T[] newArray(Class<?> componentType, int newSize) {
        return (T[]) Array.newInstance(componentType, newSize);
    }

    public static <T> T[] newArray(Collection<? extends T> col, Class<T> componentType) {
        return (T[]) col.toArray((Object[])(Array.newInstance(componentType, 0)));
    }

    public static void swap(Object[] array, int i, int j) {
        Object temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static String toString(Object value) {
        if (null == value) {
            return null;
        } else if (value instanceof byte[]) {
            return Arrays.toString((byte[])((byte[])value));
        } else if (value instanceof short[]) {
            return Arrays.toString((short[])((short[])value));
        } else if (value instanceof int[]) {
            return Arrays.toString((int[])((int[])value));
        } else if (value instanceof char[]) {
            return Arrays.toString((char[])((char[])value));
        } else if (value instanceof long[]) {
            return Arrays.toString((long[])((long[])value));
        } else if (value instanceof boolean[]) {
            return Arrays.toString((boolean[])((boolean[])value));
        } else if (value instanceof float[]) {
            return Arrays.toString((float[])((float[])value));
        } else if (value instanceof double[]) {
            return Arrays.toString((double[])((double[])value));
        } else {
            return isArray(value) ? Arrays.deepToString((Object[])((Object[])value)) : value.toString();
        }
    }
}

