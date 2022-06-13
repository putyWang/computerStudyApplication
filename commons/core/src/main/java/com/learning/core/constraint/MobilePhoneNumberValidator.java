package com.learning.core.constraint;

import com.learning.core.annotion.MobilePhoneNumber;
import com.learning.core.utils.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 手机号码验证类
 */
public class MobilePhoneNumberValidator
        implements ConstraintValidator<MobilePhoneNumber, String> {

    /**
     * 存储相关属性值
     */
    private Map<String, String> valueMap;


    /**
     * 验证参数初始化
     * @param mobilePhoneNumber 手机验证注解
     */
    @Override
    public void initialize(MobilePhoneNumber mobilePhoneNumber) {

        valueMap = new HashMap<>();

        //获取相关验证参数
        String regex = mobilePhoneNumber.regex();
        int size = mobilePhoneNumber.size();

        //保存相关验证参数
        if (! StringUtils.isEmpty(regex)) {
            valueMap.put("regex", regex);
        }

        if (size > 0) {
            valueMap.put("size", Integer.toString(size));
        }else {
            valueMap.put("size", "11");
        }
    }

    /**
     * 验证手机号码合法性逻辑
     * @param s 手机号码值
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        //验证手机号码大小
        if (StringUtils.isEmpty(s) || s.length() != Integer.parseInt(valueMap.get("size"))) {

            return false;
        }

        //验证正则表达式
        return Pattern.matches(valueMap.get("regex"), s);
    }
}
