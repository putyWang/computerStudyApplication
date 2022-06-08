package com.learning.web.entity;

import lombok.Data;

import java.io.File;

@Data
public class MailData {

    /**
     * 发送邮件文字内容
     */
    private String message;

    /**
     * 接收人
     */
    private String[] receiveUsers;

    /**
     * 抄送人
     */
    private String[] copyUsers;

    /**
     * 暗送人
     */
    private String[] darkUsers;

    /**
     * 邮件标题
     */
    private String title;

    /**
     * 正文附带图片路径集合
     */
    private File[] bodyImages;

    /**
     * 邮件附件
     */
    private File[] attachDocs;

}
