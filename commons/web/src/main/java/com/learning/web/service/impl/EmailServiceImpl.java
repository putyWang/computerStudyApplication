package com.learning.web.service.impl;

import com.learning.web.entity.MailData;
import com.learning.web.service.EmailService;
import com.learning.web.util.MailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 邮件发送服务类
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private MailProperties mailProperties;

    @Value("${spring.mail.sendName}")
    private String name;

    @Override
    public void send(String email, MailData mailData) {

        mailData.setSendUserNickName(name);
        mailData.setSendUserAccount(mailProperties.getUsername());
        mailData.setSendUserPassword(mailProperties.getPassword());

        /**
         * 将邮件发送对象添加到邮件发送队列中
         */
        MailUtil.getInstance().add(mailData);
    }
}
