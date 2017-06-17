package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.OrderCallType;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.RemoveKeyCacheInterceptor;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.IOrderTimeService;
import com.dbkj.meet.services.inter.ISMTPService;
import com.dbkj.meet.services.inter.RSAKeyService;
import com.dbkj.meet.utils.MailUtil;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.SmtpEmailVO;
import com.jfinal.aop.Before;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by DELL on 2017/05/23.
 */
public class SMTPServiceImpl implements ISMTPService {

    private RSAKeyService rsaKeyService;
    private static final Logger logger= LoggerFactory.getLogger(SMTPServiceImpl.class);

    @Override
    public SmtpEmailVO getByUserId(Long uid) {
        SmtpEmail smtpEmail = SmtpEmail.dao.findByUserId(uid);
        SmtpEmailVO smtpEmailVO=null;
        if(smtpEmail!=null){
            smtpEmailVO=new SmtpEmailVO(smtpEmail.getId(),smtpEmail.getEmail());
        }
        return smtpEmailVO;
    }

    @Before({RemoveKeyCacheInterceptor.class})
    @Override
    public Result save(SmtpEmailVO smtpEmailVO, HttpServletRequest request,String key) {
        Result result=checkSmtpEmailVO(smtpEmailVO,request,key);
        if(result.getResult()){
            Date date=new Date();

            //获取对应邮箱的SMTP的Host
//            String host="smtp."+smtpEmailVO.getEmail().substring(smtpEmailVO.getEmail().lastIndexOf("@")+1);
            //修改
            if(smtpEmailVO.getId()!=null){
                SmtpEmail smtpEmail=SmtpEmail.dao.findById(smtpEmailVO.getId());
                smtpEmail.setUsername(smtpEmailVO.getEmail());
                smtpEmail.setPassword(smtpEmailVO.getPassword());
                smtpEmail.setEmail(smtpEmailVO.getEmail());
                smtpEmail.setHost(smtpEmailVO.getHost());
                smtpEmail.setGmtModified(date);
                result.setResult(smtpEmail.update());
            }else{//添加
                User user= (User) request.getSession().getAttribute(Constant.USER_KEY);
                SmtpEmail smtpEmail=new SmtpEmail();
                smtpEmail.setUid(Integer.parseInt(user.getId().toString()));
                smtpEmail.setUsername(smtpEmailVO.getEmail());
                smtpEmail.setEmail(smtpEmailVO.getEmail());
                smtpEmail.setPassword(smtpEmailVO.getPassword());
                smtpEmail.setHost(smtpEmailVO.getHost());
                smtpEmail.setGmtCreate(date);
                result.setResult(smtpEmail.save());
            }
        }
        return result;
    }

    /**
     * 验证SmtpEmailVO
     * @param smtpEmailVO
     * @param request
     * @return
     */
    private Result checkSmtpEmailVO(SmtpEmailVO smtpEmailVO,HttpServletRequest request,String key){
        Result result=new Result(false);
        if(smtpEmailVO==null){
            return result;
        }
        Res resCn= I18n.use("zh_CN");
        if(StrKit.isBlank(smtpEmailVO.getEmail())){
            result.setMsg(resCn.get("smtp.email.not.empty"));
            return result;
        }else if (!ValidateUtil.validateEmail(smtpEmailVO.getEmail())){
            result.setMsg(resCn.get("smtp.email.format.not.correct"));
            return result;
        }

        if(StrKit.isBlank(smtpEmailVO.getHost())){
            result.setMsg(resCn.get("smtp.host.not.empty"));
            return result;
        }else if (!ValidateUtil.validateDomain(smtpEmailVO.getHost())){
            result.setMsg(resCn.get("smtp.host.format.not.correct"));
            return result;
        }

        rsaKeyService=new RSAKeyServiceImpl();
       String privateKey=rsaKeyService.getPrivateKey(key);

        if(StrKit.isBlank(smtpEmailVO.getEncryptPwd())){
            result.setMsg(resCn.get("smtp.password.not.empty"));
            return result;
        }else{
            String password= null;
            password = RSAUtil2.decryptBase64(smtpEmailVO.getEncryptPwd(),privateKey);
            smtpEmailVO.setPassword(password);
        }

        if(StrKit.isBlank(smtpEmailVO.getEncryptConfirmPwd())){
            result.setMsg(resCn.get("smtp.confirmpwd.not.empty"));
            return result;
        }else{
            String confirmPassword= null;
            confirmPassword = RSAUtil2.decryptBase64(smtpEmailVO.getEncryptConfirmPwd(),privateKey);
            if(!smtpEmailVO.getPassword().equals(confirmPassword)){
                result.setMsg(resCn.get("smtp.confirmpwd.not.equal.password"));
                return result;
            }
            smtpEmailVO.setConfirmPassword(confirmPassword);
        }

        result.setResult(true);
        return result;
    }

    @Override
    public void sendMail(MailUtil.MailBean bean) {
        MailUtil.sendMail(bean);
    }

    @Override
    public void sendMail(String from, String[] to, String password,String host, String subject, String content) {
        MailUtil.MailBean mailBean=new MailUtil.MailBean(host,from,to,from,password,subject,content);
        sendMail(mailBean);
    }

    @Override
    public void sendMail(long uid, String[] to, long rid) {
        SmtpEmail smtpEmail=SmtpEmail.dao.findByUserId(uid);
        if(smtpEmail==null){//如果用户没有设置SMTP邮箱，则用默认的SMTP邮箱
            smtpEmail=SmtpEmail.dao.findByUserId(0);
        }
        //smtp不为空，才可以发送邮件
        if(smtpEmail!=null){
            Record record=Record.dao.findById(rid);
            String subject="公司电话会议邀请函";
            //邮件内容
            StringBuilder content=new StringBuilder(250);
            content.append("\t尊敬的电话会议客户，您好！");
            content.append(record.getHostName());
            content.append("邀请您于");
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            content.append(simpleDateFormat.format(new Date()));
            content.append("参加");
            content.append(record.getSubject());
            content.append(",请拨打");
            Company company=Company.dao.findById(User.dao.findById(record.getBelong()).getCid());
            content.append(AccessNum.dao.findById(company.getCallNum()).getNum());
            content.append("并输入");
            content.append(record.getHostPwd());
            content.append("参加会议。");

            sendMail(smtpEmail.getEmail(),to,smtpEmail.getPassword(),smtpEmail.getHost(),subject,content.toString());
        }
    }

    @Override
    public void sendMail(long uid, String[] to, OrderMeet orderMeet) {
        if(to==null||to.length==0){
            return;
        }
        SmtpEmail smtpEmail=SmtpEmail.dao.findByUserId(uid);
        if(smtpEmail==null){//如果用户没有设置SMTP邮箱，则用默认的SMTP邮箱
            smtpEmail=SmtpEmail.dao.findByUserId(0);
        }
        //smtp不为空，才可以发送邮件
        if(smtpEmail!=null){
            String subject="公司电话会议邀请函";
            String content=getMailContent(orderMeet);

            sendMail(smtpEmail.getEmail(),to,smtpEmail.getPassword(),smtpEmail.getHost(),subject,content);
        }
    }

    private String getMailContent(OrderMeet orderMeet){
        //邮件内容
        StringBuilder content=new StringBuilder(250);
        content.append("\t尊敬的电话会议客户，您好！");
        content.append(orderMeet.getHostName());
        content.append("邀请您于");
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        content.append(simpleDateFormat.format(new Date()));
        content.append("参加");
        content.append(orderMeet.getSubject());
        content.append(",");

        Company company=Company.dao.findById(User.dao.findById(orderMeet.getBelong()).getCid());
        String callNum=AccessNum.dao.findById(company.getCallNum()).getNum();
        if(orderMeet.getIsCallInitiative()==Integer.parseInt(Constant.YES)){
            content.append("请注意接听");
            content.append(callNum);
            content.append("的来电");
        }else{
            content.append("请拨打"+callNum);
            content.append("并输入"+orderMeet.getHostPwd()+"参加会议");
        }
        return content.toString();
    }
}
