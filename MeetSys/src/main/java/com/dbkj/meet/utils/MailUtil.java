package com.dbkj.meet.utils;

import com.jfinal.kit.StrKit;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 邮件发送工具
 * Created by DELL on 2017/05/22.
 */
public class MailUtil {

    private static final Logger logger= LoggerFactory.getLogger(MailUtil.class.getName());

    public static void sendMail(MailBean bean){
        sendMail(bean,false);
    }
    /**
     * 发送邮件
     * @param mailBean 封装邮件信息的bean
     * @param debug 是否显示调试信息
     */
    public static void sendMail(MailBean mailBean,boolean debug){
        Properties props=new Properties();
        //设置发送邮件的邮件服务器
        props.put("mail.smtp.host",mailBean.getHost());
        //需要经过授权，也就是用户名和密码的校验，这样才能通过
        props.put("mail.smtp.auth",true);

        //用刚刚设置好的props对象构建一个session
        Session session=Session.getDefaultInstance(props);

        //是否在发送邮件的过程中在console处显示过程信息，共调试使用
        session.setDebug(debug);

        Transport transport=null;
        if(mailBean.getTo()!=null){
            //用session为参数定义消息对象
            MimeMessage message=new MimeMessage(session);

            try {
                //加载发件人地址
                message.setFrom(new InternetAddress(mailBean.getFrom()));
                //加载发件人地址
                int length = mailBean.getTo().length;
                List<InternetAddress> toAddrs=new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    String email=mailBean.getTo()[i];
                    if(!StrKit.isBlank(email)&&ValidateUtil.validateEmail(email)){
                        if(logger.isInfoEnabled()){
                            logger.info("send email to:{}",email);
                        }
                        toAddrs.add(new InternetAddress(email));
                    }
                }
                //如果收件人地址都为空，则取消发送
                if(toAddrs.size()==0){
                    return;
                }
                //加载收件人地址
                message.addRecipients(Message.RecipientType.TO,toAddrs.toArray(new InternetAddress[toAddrs.size()]));
                //加载标题
                message.setSubject(mailBean.getSubject());

                //向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
                Multipart multipart=new MimeMultipart();

                //设置邮件的文本内容
                BodyPart contentPart=new MimeBodyPart();
                contentPart.setText(mailBean.getContent());
                multipart.addBodyPart(contentPart);
                if(mailBean.getAffix()!=null&&mailBean.getAffix().length()>0){
                    //添加附件
                    BodyPart messageBodyPart=new MimeBodyPart();
                    DataSource source=new FileDataSource(mailBean.getAffix());
                    //添加附件内容
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    //添加附件标题
                    messageBodyPart.setFileName(Base64.encodeBase64String(mailBean.getAffixName().getBytes("UTF-8")));
                    multipart.addBodyPart(messageBodyPart);
                }
                //将multipart对象放到message中
                message.setContent(multipart);
                //保存邮件
                message.saveChanges();
                //发送邮件
                transport=session.getTransport("smtp");
                //链接邮箱服务器
                transport.connect(mailBean.getHost(),mailBean.getUser(),mailBean.getPwd());
                //把邮件发送出去
                transport.sendMessage(message,message.getAllRecipients());
            }catch (MessagingException|UnsupportedEncodingException e){
                logger.error(e.getMessage(),e);
            }finally {
                if(transport!=null){
                    try {
                        transport.close();
                    } catch (MessagingException e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        MailBean mailBean=new MailBean("smtp.mxhichina.com","support@kaihui.info",new String[]{"373413704@qq.com","763201895@qq.com"},
                "support@kaihui.info","Yunhuiyi2017","测试","测试邮件");
        MailUtil.sendMail(mailBean,true);
    }

    /**
     * 封装发送邮件所需要的信息
     */
    public static class MailBean{

        public MailBean(String host, String from, String[] to,String user, String pwd, String subject, String content) {
            this.host = host;
            this.from = from;
            this.to = to;
            this.user=user;
            this.pwd = pwd;
            this.subject = subject;
            this.content = content;
        }

        public MailBean(String host, String from, String[] to, String affix, String affixName, String pwd, String subject, String content) {
            this.host = host;
            this.from = from;
            this.to = to;
            this.affix = affix;
            this.affixName = affixName;
            this.pwd = pwd;
            this.subject = subject;
            this.content = content;
        }

        public MailBean(String host, String from, String[] to, String affix, String affixName, String user, String pwd, String subject, String content) {
            this.host = host;
            this.from = from;
            this.to = to;
            this.affix = affix;
            this.affixName = affixName;
            this.user = user;
            this.pwd = pwd;
            this.subject = subject;
            this.content = content;
        }

        /**
         * smtp服务器
         */
        private String host;
        /**
         * 发件人地址
         */
        private String from;
        /**
         * 收件人地址
         */
        private String[] to;
        /**
         * 附件地址
         */
        private String affix;
        /**
         * 附件名称
         */
        private String affixName;
        /**
         * 用户名
         */
        private String user;
        /**
         * 密码
         */
        private String pwd;
        /**
         * 邮件标题
         */
        private String subject;
        /**
         * 邮件内容
         */
        private String content;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String[] getTo() {
            return to;
        }

        public void setTo(String[] to) {
            this.to = to;
        }

        public String getAffix() {
            return affix;
        }

        public void setAffix(String affix) {
            this.affix = affix;
        }

        public String getAffixName() {
            return affixName;
        }

        public void setAffixName(String affixName) {
            this.affixName = affixName;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
