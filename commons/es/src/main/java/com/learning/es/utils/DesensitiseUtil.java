package com.learning.es.utils;

import com.learning.es.enums.TuoMinEnum;
import org.apache.commons.lang3.StringUtils;

public final class DesensitiseUtil {
    public DesensitiseUtil() {
    }

    public static String chineseName(String fullName) {
        return chineseName(fullName, true);
    }

    public static String chineseName(String fullName, boolean highLevel) {
        if (StringUtils.isBlank(fullName)) {
            return "";
        } else {
            String name = highLevel ? "" : StringUtils.left(fullName, 1);
            return StringUtils.rightPad(name, fullName.length(), "*");
        }
    }

    public static String idCard(String idStr) {
        return idCard(idStr, true);
    }

    public static String idCard(String idStr, boolean highLevel) {
        if (StringUtils.isBlank(idStr)) {
            return "";
        } else if (highLevel) {
            return StringUtils.rightPad("", idStr.length(), "*");
        } else {
            String idLeft = StringUtils.left(idStr, 6);
            String idRight = StringUtils.right(idStr, 4);
            return StringUtils.rightPad(idLeft, 14, "*").concat(idRight);
        }
    }

    public static String mobilePhone(String phoneNum) {
        return mobilePhone(phoneNum, true);
    }

    public static String mobilePhone(String phoneNum, boolean highLevel) {
        if (StringUtils.isBlank(phoneNum)) {
            return "";
        } else if (highLevel) {
            return StringUtils.rightPad("", phoneNum.length(), "*");
        } else {
            String phoneLeft = StringUtils.left(phoneNum, 3);
            String phoneRight = StringUtils.right(phoneNum, 4);
            return StringUtils.rightPad(phoneLeft, 7, "*").concat(phoneRight);
        }
    }

    public static String address(String address) {
        if (StringUtils.isBlank(address)) {
            return "";
        } else {
            String addressLeft = StringUtils.left(address, address.indexOf("区") + 1);
            return addressLeft.concat("****");
        }
    }

    public static String email(String email) {
        if (StringUtils.isBlank(email)) {
            return "";
        } else {
            int idx = email.indexOf("@");
            if (idx <= 1) {
                return email;
            } else {
                String emailLeft = StringUtils.left(email, 1);
                return StringUtils.rightPad(emailLeft, idx, "*").concat(email.substring(idx));
            }
        }
    }

    public static String bankCard(String bankCardNum) {
        return bankCard(bankCardNum, true);
    }

    public static String bankCard(String bankCardNum, boolean highLevel) {
        if (StringUtils.isBlank(bankCardNum)) {
            return "";
        } else {
            String bankLeft = StringUtils.left(bankCardNum, 6);
            String bankRight = StringUtils.right(bankCardNum, 4);
            return StringUtils.rightPad(bankLeft, bankCardNum.length() - 4, "*").concat(bankRight);
        }
    }

    public static String account(String account) {
        if (StringUtils.isBlank(account)) {
            return "";
        } else {
            String accountLeft = StringUtils.left(account, 1);
            return StringUtils.rightPad(accountLeft, account.length(), "*");
        }
    }

    public static String password(String passwd) {
        return StringUtils.isBlank(passwd) ? "" : StringUtils.rightPad("", passwd.length(), "*");
    }

    public static String tuoMin(String tuoMinCode, String sourceStr) {
        return tuoMin(tuoMinCode, sourceStr, true);
    }

    public static String tuoMin(String tuoMinCode, String sourceStr, boolean highLevel) {
        TuoMinEnum tuoMinEnum = TuoMinEnum.enumMap.get(tuoMinCode);
        if (tuoMinEnum == null) {
            System.out.println("未找到相应的脱敏类型");
            return sourceStr;
        } else {
            return adapterTuoMin(tuoMinEnum, sourceStr, highLevel);
        }
    }

    public static String tuoMin(Integer type, String sourceStr) {
        return tuoMin(type, sourceStr, true);
    }

    public static String tuoMin(Integer type, String sourceStr, boolean highLevel) {
        TuoMinEnum tuoMinEnum = (TuoMinEnum)TuoMinEnum.enumTypeMap.get(type);
        if (tuoMinEnum == null) {
            System.out.println("未找到相应的脱敏类型");
            return sourceStr;
        } else {
            return adapterTuoMin(tuoMinEnum, sourceStr, highLevel);
        }
    }

    private static String adapterTuoMin(TuoMinEnum tuoMinEnum, String sourceStr, boolean highLevel) {
        String targetStr = "";
        switch(tuoMinEnum) {
            case NOT_TM:
                targetStr = sourceStr;
                break;
            case NAME_TM:
                targetStr = chineseName(sourceStr, highLevel);
                break;
            case PHONE_TM:
                targetStr = mobilePhone(sourceStr, highLevel);
                break;
            case REGNO_TM:
                targetStr = mobilePhone(sourceStr);
                break;
            case ID_CARD_TM:
                targetStr = idCard(sourceStr, highLevel);
                break;
            case EMAIL_TM:
                targetStr = email(sourceStr);
                break;
            case ADDRESS_TM:
                targetStr = address(sourceStr);
                break;
            default:
                targetStr = sourceStr;
        }

        return targetStr;
    }

    public static void main(String[] args) {
        System.out.println("张三：" + tuoMin("name_tm", "张三"));
        System.out.println("王小红：" + tuoMin("name_tm", "王小红", false));
        System.out.println("18834310787：" + tuoMin("phone_tm", "18834310787"));
        System.out.println("144121199209090013：" + tuoMin("id_card_tm", "144121199209090013"));
        System.out.println(tuoMin("address_tm", "北京市海淀区北清路156号龙芯1号楼"));
    }
}
