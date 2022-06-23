package com.learning.es.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 病例基本信息信息
 * @author ：wangpenghui
 * @date ：Created in 2021/3/18 14:35
 */
@Data
public class PatBasicInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 患者唯一标识
     */
    private String empi;

    /**
     * 病案号 非必填
     */
    private String recordId;

    private String name;

    /**
     * 患者登记号 必填 登记号可以重复
     */
    private String regno;

    private Integer gender;

    private Date birthday;

    private String idCard;

    private String tel;

    private Integer source;

    /**
     * 入组时间
     */
    private Date intoDate;
}
