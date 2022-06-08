package com.learning.web.util;

import com.alibaba.fastjson.JSONObject;
import com.learning.core.utils.ArrayUtils;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.ObjectUtils;
import com.learning.web.entity.MailData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailUtil {

    public static final Logger log = LoggerFactory.getLogger(MailUtil.class);

    /**
     * 线程池，最多同时3个线程在运行，其他的排队等候
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(3);

    /**
     *  发送邮件（注意发送人的邮箱必须设置开通POP3/SMTP/IMAP，否则无法发送）
     * @param sendUserAccount 发送人email账号（163或者qq邮箱） （必填）
     * @param sendUserPassword 发送人email的授权码（必填）
     * @param sendUserNickName 发送人的昵称
     * @param mailData 接收人 （必填）
     */
    public static void sendMail(String sendUserAccount, String sendUserPassword, String sendUserNickName, MailData mailData){

        MailThread mail = new MailThread(sendUserAccount, sendUserPassword, sendUserNickName, mailData.getReceiveUsers(), mailData.getCopyUsers(), mailData.getDarkUsers(), mailData.getTitle(), mailData.getMessage(),  mailData.getBodyImages(), mailData.getAttachDocs());
        executor.execute(mail);
    }

    private static  class MailThread extends Thread{

        private String sendUserAccount;
        private String sendUserPassword;
        private String sendUserNickName;
        private String[] receiveUsers;
        private String[] copyUsers;
        private String[] darkUsers;
        private String title;
        private String text;
        private File[] bodyImgs;
        private File[] attachDocs;

        private JSONObject params;//参数集合

        public MailThread(String sendUserAccount, String sendUserPassword,
                          String sendUserNickName, String[] receiveUsers,
                          String[] copyUsers, String[] darkUsers, String title,
                          String text, File[] bodyImgs,
                          File[] attachDocs) {
            super();
            this.sendUserAccount = sendUserAccount;
            this.sendUserPassword = sendUserPassword;
            this.sendUserNickName = sendUserNickName;
            this.receiveUsers = receiveUsers;
            this.copyUsers = copyUsers;
            this.darkUsers = darkUsers;
            this.text = text;
            this.title = title;
            this.bodyImgs = bodyImgs;
            this.attachDocs = attachDocs;

            setParams();
        }

        private void setParams(){
            params = new JSONObject();
            params.put("sendUserAccount", sendUserAccount);
            params.put("sendUserPassword", sendUserPassword);
            params.put("sendUserNickName", sendUserNickName);
            params.put("receiveUsers", receiveUsers);
            params.put("copyUsers", copyUsers);
            params.put("darkUsers", darkUsers);
            params.put("title", title);
            params.put("text", text);
            //设置正文图片
            if(! ArrayUtils.isEmpty(bodyImgs)){
                List<String> list = new ArrayList<>();
                for(File f : bodyImgs){
                    list.add(f.getName());
                }
                params.put("bodyImages", list);
            }
            //设置邮件附件
            if(! ArrayUtils.isEmpty(attachDocs)){
                List<String> list = new ArrayList<>();
                for(File f : attachDocs){
                    list.add(f.getName());
                }
                params.put("attachDocs", list);
            }
        }

        public void run(){
            long startTime = System.currentTimeMillis();

            try {
                //获取邮件发送实例
                JavaMailSenderImpl mailSender = getJavaMailSenderImpl(sendUserAccount, sendUserPassword);
                //创建邮件帮助类
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true,"UTF-8");//必须true
                //设置邮件内容
                setMailContent(messageHelper, title, text, bodyImgs, attachDocs);

                //设置发送人
                setSenderUser(messageHelper, sendUserAccount,sendUserNickName);

                //设置接收人
                setReceiveUsers(messageHelper, receiveUsers, copyUsers, darkUsers);
                //发送
                mailSender.send(mimeMessage);

                //邮件发送成功，记录相关日志
                log.info("邮件发送失败，相关信息为：" + params);
            } catch (Exception e) {
                //邮件发送失败，可以将发送失败日志记录到数据库进行相关处理
                log.info("邮件发送失败，相关信息为：" + params + "相关异常为：" + e);
            }

            long endTime = System.currentTimeMillis();
            String t = ((endTime-startTime)/(60*1000))+" 分 " + (((endTime -startTime)/1000.0)%60) + "秒";
            log.info("邮件发送耗时:" + t);
        }

        /**
         * 链接邮件服务器
         * @param sendUserAccount
         * @param sendUserPassword
         * @return
         */
        private  JavaMailSenderImpl  getJavaMailSenderImpl(String sendUserAccount,String sendUserPassword){

            //连接邮件服务器的参数配置
            Properties props = new Properties();
            //开启tls
            props.setProperty("mail.smtp.auth","true");
            props.setProperty("mail.smtp.ssl.enable", "true");
            //    props.setProperty("smtp.starttls.required", "true");

            JavaMailSenderImpl impl = new JavaMailSenderImpl();

            impl.setHost(sendUserAccount.endsWith("163.com") ? "smtp.163.com":"smtp.qq.com");
            impl.setUsername(sendUserAccount);
            impl.setPassword(sendUserPassword);
            impl.setPort(465);
            impl.setDefaultEncoding("UTF-8");
            impl.setProtocol("smtp");
            impl.setJavaMailProperties(props);

            return impl;
        }

        /**
         * 设置邮件内容
         * @param help
         * @param title
         * @param text
         * @param bodyImgs
         * @param attachDocs
         * @throws MessagingException
         * @throws UnsupportedEncodingException
         */
        private void setMailContent(MimeMessageHelper help,String title,String text,File[] bodyImgs,File[] attachDocs)
                throws MessagingException, UnsupportedEncodingException {
            //设置标题
            help.setSubject(title);
            //设置文本内容
            StringBuffer s = new StringBuffer("<html><body>");
            s.append(text);
            //设置正文图片格式
            if(! ArrayUtils.isEmpty(bodyImgs)){
                for(int i = 0;i<bodyImgs.length;i++){
                    s.append("<img src='cid:pic").append(i).append("' />");
                }
            }
            s.append("</body></html>");
            //设置文本格式为html
            help.setText(s.toString(), true);
            //展示在正文的图片
            if(! ArrayUtils.isEmpty(bodyImgs)){
                for(int i = 0;i<bodyImgs.length;i++){
                    help.addInline("pic"+i, bodyImgs[i]);
                }
            }
            //添加附件
            if(! ArrayUtils.isEmpty(attachDocs)){
                for(File file : attachDocs){
                    //解决附件中文乱码
                    help.addAttachment(MimeUtility.encodeWord(file.getName()), file);
                }
            }
        }

        /**
         * 设置接收人,抄送人，暗送人
         * @param help
         * @param receiveUsers 接收人
         * @param copyUsers  抄送人
         * @param darkUsers  暗送人
         * @throws MessagingException
         */
        private void setReceiveUsers(MimeMessageHelper help,String[] receiveUsers,String[] copyUsers,String[] darkUsers) throws MessagingException{
            if (! ObjectUtils.isEmpty(receiveUsers)) {
                help.setTo(receiveUsers);
            }

            if (! ArrayUtils.isEmpty(copyUsers)) {
                help.setCc(copyUsers);
            }

            if (! ArrayUtils.isEmpty(darkUsers)) {
                help.setBcc(darkUsers);
            }
        }

        /**
         * 设置发送人
         * @param help
         * @param senderAccount 邮箱账号
         * @param userName  昵称
         * @throws MessagingException
         * @throws UnsupportedEncodingException
         */
        private void setSenderUser(MimeMessageHelper help,String senderAccount,String userName) throws MessagingException, UnsupportedEncodingException{
            if (! ObjectUtils.isEmpty(userName)) {
                help.setFrom(senderAccount,userName);
            }else{
                help.setFrom(senderAccount);
            }
        }
    }
}
