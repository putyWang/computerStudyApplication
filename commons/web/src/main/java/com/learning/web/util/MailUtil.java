package com.learning.web.util;

import com.alibaba.fastjson.JSONObject;
import com.learning.core.utils.ArrayUtils;
import com.learning.core.utils.ObjectUtils;
import com.learning.web.entity.MailData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送邮件工具类
 */
public final class MailUtil {

    public static final Logger log = LoggerFactory.getLogger(MailUtil.class);

    /**
     * 批量处理数据的大小
     */
    private final static int batchSize = 5000;

    /**
     * 线程池线程数量
     */
    private final static int poolSize = 5;

    /**
     * 插入数据的时间间隔，每间隔intervalTime的时间自动插入一次
     */
    private final static long intervalTime = 1000*60*5;

    private static MailUtil mailUtil;

    /**
     * 线程池，最多同时3个线程在运行，其他的排队等候
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(3);

    /*
     * 当前激活的线程数量计数器
     */
    private static AtomicInteger activeThreadCount = new AtomicInteger(0);
    /**
     *
     * 初始化队列， 作为数据缓存池。
     * 缓存池的大小为：batchSize * (poolSize + 1)
     */
    private static BlockingQueue<MailData> blockingQueue = new LinkedBlockingQueue<>(batchSize * (poolSize + 20));
    /**
     * 可重用固定个数的线程池
     * 可控制线程最大并发数，超出的线程会在队列中等待,当处理完一个马上就会去接着处理排队中的任务
     */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolSize);

    public MailUtil() {
    }

    /**
     * 获取单例实例
     * @return ElasticBulkProcessor
     */
    public static MailUtil getInstance() {
        if (null == mailUtil) {
            // 多线程同步
            synchronized (MailUtil.class) {
                if (null == mailUtil) {
                    mailUtil = new MailUtil();
                }
            }
        }

        return mailUtil;
    }

    /**
     * 同步执行add,往队列中添加一条数据
     * @param mailData 邮件数据
     */
    public synchronized void add(MailData mailData) {
        try {
            if (! ObjectUtils.isEmpty(mailData)){
                log.info("向canal消息队列插入一条数据：" + mailData.toString());
                // 将指定元素插入此队列中，将等待可用的空间.当>maxSize 时候，阻塞，直到能够有空间插入元素
                blockingQueue.put(mailData);
                // 执行线程
                execute();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 线程池执行
     */
    private void execute() {
        // 获取当前活动的线程数
        int curActiveCount = activeThreadCount.get();
        Future<Long> future;

        // 如果激活的线程池为0，创建一个新的线程
        if (curActiveCount == 0) {
            ExecuteClass executeClass = new ExecuteClass();
            // 开启一个线程，和execute区别为有返回值
            future = fixedThreadPool.submit(executeClass);
            activeThreadCount.incrementAndGet();
        }
    }

    /**
     * 实现Callable可以返回现线程执行结果
     * 返回结果为执行成功的数量
     */
    class ExecuteClass
            implements Callable<Long> {

        @Override
        public Long call() throws Exception {
            log.info("start thread -" + Thread.currentThread().getName());
            // 空闲时间
            long freeTime = 0;
            long sleep = 100;
            long longSleep = 1000*60;
            List<MailData> entries;

            // 无限循环从blockQueue中取数据
            while (true) {
                try{
                    // 只要消息队列有数据就立即执行
                    if (blockingQueue != null && blockingQueue.size() >= 1) {
                        freeTime = 0;
                        entries = new ArrayList<>();
                        //取出邮件数据对象进行消费
                        MailData mailData = blockingQueue.poll();
                        if (!CollectionUtils.isEmpty(entries)) {
                            // 将数据插入es
                            consume(mailData);
                        }
                    } else {
                        // 等待100ms
                        Thread.sleep(sleep);
                        freeTime += sleep;
                    }

                    // 如果总空闲时间超过5分钟， 结束当前线程
                    if (freeTime >= intervalTime) {
                        log.info("stop Thread-" + Thread.currentThread().getName());
                        activeThreadCount.decrementAndGet();
                        break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    activeThreadCount.decrementAndGet();
                    break;
                }
            }

            return null;
        }

    }

    /**
     * 发送邮件（注意发送人的邮箱必须设置开通POP3/SMTP/IMAP，否则无法发送）
     * @param mailData
     */
    private void consume(MailData mailData){
        MailSender mail = new MailSender(mailData.getSendUserAccount(), mailData.getSendUserPassword(), mailData.getSendUserNickName(), mailData.getReceiveUsers(), mailData.getCopyUsers(), mailData.getDarkUsers(), mailData.getTitle(), mailData.getMessage(),  mailData.getBodyImages(), mailData.getAttachDocs());
        mail.sendMail();
    }

    /**
     * 邮件发送对象
     */
    private static class MailSender{

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

        public MailSender(String sendUserAccount, String sendUserPassword,
                          String sendUserNickName, String[] receiveUsers,
                          String[] copyUsers, String[] darkUsers, String title,
                          String text, File[] bodyImgs,
                          File[] attachDocs) {

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

        public void sendMail(){
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
