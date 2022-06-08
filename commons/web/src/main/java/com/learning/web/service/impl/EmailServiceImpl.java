package com.learning.web.service.impl;

import com.learning.web.entity.MailData;
import com.learning.web.service.EmailService;
import com.learning.web.util.MailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private MailProperties mailProperties;

    @Value("${spring.mail.sendName}")
    private String name;

    @Override
    public void send(String email, MailData mailData) {

            MailUtil.sendMail(mailProperties.getUsername(), mailProperties.getPassword(), name, mailData);
    }
}
