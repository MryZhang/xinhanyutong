package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.SmtpEmail;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.ISMTPService;
import com.dbkj.meet.utils.MailUtil;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.SmtpEmailVO;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Created by DELL on 2017/05/23.
 */
public class SMTPServiceImpl implements ISMTPService {

    @Override
    public SmtpEmailVO getByUserId(Long uid) {
        SmtpEmail smtpEmail = SmtpEmail.dao.findByUserId(uid);
        SmtpEmailVO smtpEmailVO=null;
        if(smtpEmail!=null){
            smtpEmailVO=new SmtpEmailVO(smtpEmail.getId(),smtpEmail.getEmail());
        }
        return smtpEmailVO;
    }

    @Override
    public Result save(SmtpEmailVO smtpEmailVO, HttpServletRequest request) {
        Result result=checkSmtpEmailVO(smtpEmailVO,request);
        if(result.getResult()){
            Date date=new Date();

            //获取对应邮箱的SMTP的Host
            String host="smtp."+smtpEmailVO.getEmail().substring(smtpEmailVO.getEmail().lastIndexOf("@"));
            //修改
            if(smtpEmailVO.getId()!=null){
                SmtpEmail smtpEmail=SmtpEmail.dao.findById(smtpEmailVO.getId());
                smtpEmail.setUsername(smtpEmail.getEmail());
                smtpEmail.setPassword(smtpEmail.getPassword());
                smtpEmail.setEmail(smtpEmail.getEmail());
                smtpEmail.setHost(host);
                smtpEmail.setGmtModified(date);
                result.setResult(smtpEmail.update());
            }else{//添加
                User user= (User) request.getSession().getAttribute(Constant.USER_KEY);
                SmtpEmail smtpEmail=new SmtpEmail();
                smtpEmail.setUid(Integer.parseInt(user.getId().toString()));
                smtpEmail.setUsername(smtpEmailVO.getEmail());
                smtpEmail.setEmail(smtpEmailVO.getEmail());
                smtpEmail.setPassword(smtpEmailVO.getPassword());
                smtpEmail.setHost(host);
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
    private Result checkSmtpEmailVO(SmtpEmailVO smtpEmailVO,HttpServletRequest request){
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

        Map<String,Key> keyMap= (Map<String, Key>) request.getSession().getAttribute(Constant.KEY_MAP);
        Key privateKey=keyMap.get(RSAUtil2.PRIVATE_KEY);

        if(StrKit.isBlank(smtpEmailVO.getEncryptPwd())){
            result.setMsg(resCn.get("smtp.password.not.empty"));
            return result;
        }else{
            String password= RSAUtil2.decryptBase64(smtpEmailVO.getEncryptPwd(),privateKey);
            smtpEmailVO.setPassword(password);
        }

        if(StrKit.isBlank(smtpEmailVO.getEncryptConfirmPwd())){
            result.setMsg(resCn.get("smtp.confirmpwd.not.empty"));
            return result;
        }else{
            String confirmPassword=RSAUtil2.decryptBase64(smtpEmailVO.getEncryptConfirmPwd(),privateKey);
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

}
