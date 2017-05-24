package com.dbkj.meet.controller;

import com.dbkj.meet.services.MessageServiceImpl;
import com.dbkj.meet.services.inter.MessageService;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/03/23.
 */
public class MessageController extends Controller {

    MessageService messageService=new MessageServiceImpl();

    public void send(){
        Long id=getParaToLong();
        messageService.sendMsg(getRequest());
        renderNull();
    }
}
