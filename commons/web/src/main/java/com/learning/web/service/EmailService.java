package com.learning.web.service;

import com.learning.web.entity.MailData;

/**
 * email相关操作类
 */
public interface EmailService {

    void send(String email, MailData mailData);
}
