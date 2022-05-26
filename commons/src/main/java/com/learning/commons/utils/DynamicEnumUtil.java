package com.learning.commons.utils;

import com.learning.commons.enums.CodeInfoEnum;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DynamicEnumUtil {
    private static ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

    public DynamicEnumUtil() {
    }

    private static void setFailsafeFieldValue(Field field, Object target, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);
        modifiers &= -17;
        modifiersField.setInt(field, modifiers);
        FieldAccessor fa = reflectionFactory.newFieldAccessor(field, false);
        fa.set(target, value);
    }

    private static void blankField(Class<?> enumClass, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field[] var2 = Class.class.getDeclaredFields();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Field field = var2[var4];
            if (field.getName().contains(fieldName)) {
                AccessibleObject.setAccessible(new Field[]{field}, true);
                setFailsafeFieldValue(field, enumClass, (Object)null);
                break;
            }
        }

    }

    private static void cleanEnumCache(Class<?> enumClass) throws NoSuchFieldException, IllegalAccessException {
        blankField(enumClass, "enumConstantDirectory");
        blankField(enumClass, "enumConstants");
    }

    private static ConstructorAccessor getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes) throws NoSuchMethodException {
        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = Integer.TYPE;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        return reflectionFactory.newConstructorAccessor(enumClass.getDeclaredConstructor(parameterTypes));
    }

    private static Object makeEnum(Class<?> enumClass, String value, int ordinal, Class<?>[] additionalTypes, Object[] additionalValues) throws Exception {
        Object[] parms = new Object[additionalValues.length + 2];
        parms[0] = value;
        parms[1] = ordinal;
        System.arraycopy(additionalValues, 0, parms, 2, additionalValues.length);
        return enumClass.cast(getConstructorAccessor(enumClass, additionalTypes).newInstance(parms));
    }

    public static <T extends Enum<?>> void addEnum(Class<T> enumType, String enumName, Class<?>[] paramClass, Object[] paramValue) {
        if (!Enum.class.isAssignableFrom(enumType)) {
            throw new RuntimeException("class " + enumType + " is not an instance of Enum");
        } else {
            Field valuesField = null;
            Field[] fields = CodeInfoEnum.class.getDeclaredFields();
            Field[] var6 = fields;
            int var7 = fields.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                Field field = var6[var8];
                if (field.getName().contains("$VALUES")) {
                    valuesField = field;
                    break;
                }
            }

            AccessibleObject.setAccessible(new Field[]{valuesField}, true);

            try {
                T[] previousValues = (T[]) valuesField.get(enumType);
                List<T> values = new ArrayList(Arrays.asList(previousValues));
                T newValue = (T) makeEnum(enumType, enumName, values.size(), paramClass, paramValue);
                values.add(newValue);
                Object object = values.toArray((Enum[])((Enum[]) Array.newInstance(enumType, 0)));
                setFailsafeFieldValue(valuesField, (Object)null, object);
                cleanEnumCache(enumType);
            } catch (Exception var10) {
                var10.printStackTrace();
                throw new RuntimeException(var10.getMessage(), var10);
            }
        }
    }

    public static void main(String[] args) {
        Class var1 = CodeInfoEnum.class;
        synchronized(CodeInfoEnum.class) {
            addEnum(CodeInfoEnum.class, "3", new Class[]{Long.class, Long.class, String.class, String.class}, new Object[]{2L, 3L, "ActiveStatus", "Active"});
            addEnum(CodeInfoEnum.class, "4", new Class[]{Long.class, Long.class, String.class, String.class}, new Object[]{2L, 4L, "ActiveStatus", "Inactive"});
            addEnum(CodeInfoEnum.class, "5", new Class[]{Long.class, Long.class, String.class, String.class}, new Object[]{3L, 5L, "Optype", "OP1"});
            addEnum(CodeInfoEnum.class, "6", new Class[]{Long.class, Long.class, String.class, String.class}, new Object[]{3L, 6L, "Optype", "OP2"});
            addEnum(CodeInfoEnum.class, "7", new Class[]{Long.class, Long.class, String.class, String.class}, new Object[]{3L, 7L, "Optype", "OP3"});
            addEnum(CodeInfoEnum.class, "8", new Class[]{Long.class, Long.class, String.class, String.class}, new Object[]{3L, 8L, "Optype", "OP4"});
        }

        CodeInfoEnum codeInfoEnum = CodeInfoEnum.valueOf("5");
        System.out.println(codeInfoEnum);
        System.out.println(Arrays.deepToString(CodeInfoEnum.values()));
        System.out.println("============================打印所有枚举（包括固定的和动态的），可以将数据库中保存的CIC以枚举的形式加载到JVM");
        CodeInfoEnum[] var2 = CodeInfoEnum.values();
        int var3 = var2.length;

        CodeInfoEnum toGetActiveStatus_miss;
        for(int var4 = 0; var4 < var3; ++var4) {
            toGetActiveStatus_miss = var2[var4];
            System.out.println(toGetActiveStatus_miss.toString());
        }

        System.out.println("============================通过codeId找到的枚举，用于PO转VO的处理");
        CodeInfoEnum activeStatus_Active = CodeInfoEnum.getByInfoId(3L);
        System.out.println(activeStatus_Active);
        System.out.println("============================通过ClassId找到的枚举列表");
        List<CodeInfoEnum> activeStatusEnumList = CodeInfoEnum.getByClassId(3L);
        Iterator var10 = activeStatusEnumList.iterator();

        while(var10.hasNext()) {
            toGetActiveStatus_miss = (CodeInfoEnum)var10.next();
            System.out.println(toGetActiveStatus_miss);
        }

        System.out.println("============================通过ClassCode和InfoCode获取枚举，用于导入验证CIC合法性");
        CodeInfoEnum toGetActiveStatus_Active = CodeInfoEnum.getByClassCodeAndInfoCode("ActiveStatus", "Active");
        System.out.println(toGetActiveStatus_Active);
        System.out.println("============================通过ClassCode和InfoCode获取枚举，输入不存在的Code，则返回NULL");
        toGetActiveStatus_miss = CodeInfoEnum.getByClassCodeAndInfoCode("ActiveStatus", "MISS");
        System.out.println(toGetActiveStatus_miss);
    }
}
