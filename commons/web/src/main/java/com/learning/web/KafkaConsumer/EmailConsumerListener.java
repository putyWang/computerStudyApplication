package com.learning.web.KafkaConsumer;

import com.alibaba.fastjson.JSON;
import com.learning.core.bean.SuccessRegistryMessage;
import com.learning.core.constants.KafkaConstants;
import com.learning.core.constants.MailConstants;
import com.learning.core.utils.ObjectUtils;
import com.learning.web.entity.MailData;
import com.learning.web.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class EmailConsumerListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = KafkaConstants.REGISTRY_SUCCESS_KEY)
    public void consumeMessage(ConsumerRecord<String, String> record){
        Optional<String> kafkaMessage = Optional.ofNullable(record.value());

        if (kafkaMessage.isPresent()){
            String message = kafkaMessage.get();
            log.info("----------------- record =" + record);

            SuccessRegistryMessage successRegistryMessage = JSON.parseObject(message, SuccessRegistryMessage.class);
            if (! ObjectUtils.isEmpty(successRegistryMessage)) {
                //设置邮件正文相关内容
                MailData mailData = new MailData();
                mailData.setMessage(successRegistryMessage.getUsername());
                mailData.setReceiveUsers(new String[]{
                    successRegistryMessage.getEmailAddress()
                });
                mailData.setTitle(MailConstants.REGISTRY_SUCCESS_MAIL_TITLE);
                emailService.send(successRegistryMessage.getEmailAddress(), mailData);
            }else
                log.info("消费者json字符串转换失败" + message);
        }

    }
}
