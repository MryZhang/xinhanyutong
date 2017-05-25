package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.model.SmtpEmail;
import com.dbkj.meet.model.User;
import com.dbkj.meet.utils.MailUtil;
import com.dbkj.meet.vo.SmtpEmailVO;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by DELL on 2017/05/23.
 */
public interface ISMTPService {

    /**
     * 获取用户的Smtp邮箱
     * @param uid
     * @return
     */
    SmtpEmailVO getByUserId(Long uid);

    Result save(SmtpEmailVO smtpEmailVO, HttpServletRequest request);

    /**
     * 发送邮件
     * @param bean
     */
    void sendMail(MailUtil.MailBean bean);

    /**
     * 发送邮件
     * @param from 发送邮箱
     * @param to 收件邮箱
     * @param password 密码
     * @param host smtp邮箱服务器
     * @param subject 主题
     * @param content 邮件内容
     */
    void sendMail(String from,String[] to,String password,String host,String subject,String content);

    /**
     * 发送邮件
     * @param uid 要送邮件的用户id
     * @param to 收件邮箱
     * @param rid 会议记录Record ID
     */
    void sendMail(long uid,String[] to,long rid);

    /**
     * 发送邮件
     * @param uid 要送邮件的用户id
     * @param to 收件邮箱
     * @param orderMeet 预约会议记录
     */
    void sendMail(long uid, String[] to, OrderMeet orderMeet);
}
