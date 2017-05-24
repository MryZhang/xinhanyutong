package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.SmtpEmail;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.RSAServiceImpl;
import com.dbkj.meet.services.SMTPServiceImpl;
import com.dbkj.meet.services.inter.IRSAService;
import com.dbkj.meet.services.inter.ISMTPService;
import com.dbkj.meet.vo.SmtpEmailVO;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import org.omg.PortableServer.POA;

import java.security.Key;
import java.util.Map;

/**
 * 设置smtp邮箱的controller
 * Created by DELL on 2017/05/23.
 */
public class SMTPController extends Controller {

    private final ISMTPService smtpService=new SMTPServiceImpl();
    private final IRSAService rsaService=new RSAServiceImpl();

    public void index(){
        User user=getSessionAttr(Constant.USER_KEY);
        SmtpEmailVO smtpEmail=smtpService.getByUserId(user.getId());
        setAttr("smtp",smtpEmail);
        Map<String,Key> keyMap=rsaService.generateKeys();
        setSessionAttr(Constant.KEY_MAP,keyMap);
        setAttr("publicKey",rsaService.getPublicKey(keyMap));
        render("index.html");
    }

    @Before({POST.class})
    public void save(){
        SmtpEmailVO smtpEmailVO=getBean(SmtpEmailVO.class,"smtp");
        Result result=smtpService.save(smtpEmailVO,getRequest());
        renderJson(result);
    }
}
