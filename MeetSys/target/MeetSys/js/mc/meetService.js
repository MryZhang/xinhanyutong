/**
 * Created by MrQin on 2016/11/29.
 */
//防止脚本出现未知错误影响正常使用
window.onError=function (msg) {
    console.log(msg);
    return true;
}
//角色
var RoleType={
    HOST:1,//主持人
    ATTENDEE:2//参会人
}
var meetService={
    isFirst:true,//是否是初次呼叫
    isFirstStartRecord:true,//是否是首次开始录音
    remainSec:10,
    isShow:false,
    serviceUrl:"ws://60.190.236.54:21280/meet1/meet_api",
    //初始化页面数据
    initPage:function () {
        //加载个人通信录
        $.ajax({
            type:"get",
            url:common.getContextPath()+"/personalcontacts/getContacts",
            dataType:"json",
            success:function(data){
                $("body").data("personalContacts",data);
                meetService.loadContacts(data, ["name","phone"], "personl_contacts");
            },error:function(err){
                console.log(err.responseText);
            }
        })
        //加载公共通信录
        var publicContacts;
        $.ajax({
            type:"get",
            url:common.getContextPath()+"/publiccontacts/getContacts",
            dataType:"json",
            success:function(data){
                publicContacts=data;
            },error:function(err){
                console.log(err.responseText);
            }
        }).done(function(){
            $("body").data("publicContacts",publicContacts);
            meetService.loadContacts(publicContacts, ["name","phone","position"], "public_contacts");
            //加载会议中的数据
            var inmeetData=$("#inmeetData").text();
            if(inmeetData){
                inmeetData=JSON.parse(inmeetData);
                $("body").data("inmeet",inmeetData);
                meetService.loadInMeet(inmeetData);
            }
            //加载会议邀请人数据
            var inviteData=$("#inviteData").text();
            if(inviteData){
                inviteData=JSON.parse(inviteData);
                $("body").data("invite",inviteData);//将数据存入缓存
                meetService.loadInvite(inviteData);
            }
        });

        var meetId=$("#meetId").val();
        if(meetId){//会议已开始
            $("#meetStatus").text("会议中...");
            meetService.initWebsocket(meetId);
            meetService.changeMeetControlState();
            $("#loading").hide();
        }else{
            $("#loading").hide();
        }
    },
    //检查会议中各个电话的连接状态
    checkStatus:function () {
        var meetId=$("#meetId").val();
        webSocketUtil.getStatus(meetId);
    },
    //改变会议操作的状态
    changeMeetControlState:function () {
        $("#startMeetBtn").text("结束会议");
        $("#edit_subject").parent().hide();
        $("#change_host").parent().hide();
        $("#isRecord").bootstrapSwitch("disabled",true);
        $("#isAllowSpeak").bootstrapSwitch("disabled",false);
        $("#playback").bootstrapSwitch("disabled",false);
        $("#lockMeet").bootstrapSwitch("disabled",false);
        $("#allowBegin").bootstrapSwitch("disabled",true);
        $("#noticeFailed").bootstrapSwitch("disabled",true);
        $("#muteAll").removeAttr("disabled");
        $("a.add-invite").attr("class","call");
        $("a.clear").attr("class","call");
        $("a.msg").show();
    },
    //会议结束后变更会议记录
    finishMeet:function () {
        var rid=$("#rid").val();
        $.ajax({
            type:"get",
            url:common.getContextPath()+"/meet/finishMeet/"+rid,
            dataType:"json",
            success:function(data){
                closeWindow();
            },error:function(err){
                console.log(err.responseText);
            }
        });
    },
    //添加主持人数据
    addHost:function (name,phone) {
        var h = {};
        h.phone = phone;
        h.name = name || phone;
        var host=$("body").data("host")||[];
        var flag=true;
        //判断要添加的是否重复
        for(var i=0;i<host.length;i++){
            if(host[i].phone==phone){
                flag=false;
                break;
            }
        }
        if(flag){
            host.push(h);
            $("body").data("host",host);
        }
    },
    //移除主持人数据
    removeHost:function (phone) {
        var host=$("body").data("host") ;
        if (host) {
            for (var i = 0, len = host.length; i < len; i++) {
                if (host[i].phone == phone) {
                    host.splice(i, 1);
                    $("body").data("host",host);
                    break;
                }
            }
        }
    },
    //获取联系人名称
    getName:function (phone) {
        var personalContacts = $("body").data("personalContacts");
        var publicContacts = $("body").data("publicContacts");
        var invite=$("body").data("invite");
        var inmeet=$("body").data("inmeet");
        var name;
        if(personalContacts&&personalContacts.length>0){
            for(var i=0,len=personalContacts.length;i<len;i++){
                var children=personalContacts[i].children;
                for(var n=0,size=children.length;n<size;n++){
                    if(children[n].phone==phone){
                        name=children[n].name;
                        break;
                    }
                }
                if(name){
                    break;
                }
            }
        }
        if(!name&&publicContacts&&publicContacts.length>0){
            for(var i=0,len=publicContacts.length;i<len;i++){
                var children=publicContacts[i].children;
                for(var n=0,size=children.length;n<size;n++){
                    if(children[n].phone==phone){
                        name=children[n].name;
                        break;
                    }
                }
                if(name){
                    break;
                }
            }
        }

        if(!name&&invite&&invite.length>0){
            for(var i=0,len=invite.length;i<len;i++){
                if(invite[i].phone==phone){
                    name=invite[i].name;
                    break;
                }
            }
        }

        if(!name&&inmeet&&inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                if(inmeet[i].phone==phone){
                    name=inmeet[i].name;
                    break;
                }
            }
        }

        if(!name){
            name=phone
        }
        return name;
    },
    //获取联系人角色
    getRole:function (phone) {
        var invite=$("body").data("invite");
        var inmeet=$("body").data("inmeet");
        var role;
        if(invite&&invite.length>0){
            for(var i=0,len=invite.length;i<len;i++){
                if(invite[i].phone==phone){
                    role=invite[i].role;
                    break;
                }
            }
        }

        if(!role&&inmeet&&inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                if(inmeet[i].phone==phone){
                    role=inmeet[i].role;
                    break;
                }
            }
        }
        return role;
    },
    checkTime:function () {
        var time=this.remainSec;
        this.remainSec--;
        if(time>0){
            $("#time").text(time);
            setTimeout("meetService.checkTime()", 1000);
        }else{
            closeWindow();
        }
    },
    //窗口关闭提示
    showCloseTip:function () {
        if(!this.isShow){
            this.isShow=true;
            var msg="会议连接超时，会场已关闭！<div class=\"text-danger\"><span id=\"time\">10</span>s后窗口将关闭</div>";
            showTip(msg, function(){
                meetService.finishMeet();
                //closeWindow();
            });
            setTimeout("meetService.checkTime()", 1000);
        }

    },
    //改变禁言操作状态
    changeSlienceState:function (phone,action) {
        $("#attendee_list table>tbody>tr").each(function(){
            var num=$(this).find("td:eq(1)").text().trim();
            if(num==phone){
                if(action==webSocketUtil.Action.ACTION_MEMBER_SILENT){
                    $(this).find("a.mic").attr("class","mic-off");
                }else{
                    $(this).find("a.mic-off").attr("class","mic");
                }
                return false;
            }
        });
    },
    //接收websocket服务器发送的消息
    //并改变操作状态或数据
    reciveResult:function (e) {
        var msg=e.data;
        console.log(msg);
        var result=JSON.parse(msg);
        switch(result.action){
            case webSocketUtil.Action.INIT://初始化
                if(result.status==webSocketUtil.Action.SUCCESS){
                    showInfo(result.content);
                    meetService.checkStatus();
                    //websocket连接成功，则录音按钮可用
                    $("#btn_record").removeAttr("disabled");
                    //如果会议开始后，根据会议录音状态更新录音按钮
                    var isRecord =$("#isRecord").bootstrapSwitch("state");
                    if(isRecord){
                        $("#btn_record").text("结束录音");
                    }else{
                        $("#btn_record").text("开始录音");
                    }
                }else if(result.status==webSocketUtil.Action.FAIL&&result.content.indexOf("meetId不存在")!=-1){
                    showInfo("会议连接失败！",true);
                    meetService.showCloseTip();
                    //关闭socket连接，并停止自动重连
                    webSocketUtil.ws.close();
                }
                break;
            case webSocketUtil.Action.ACTION_JOIN_MEET://加入会议
                break;
            case webSocketUtil.Action.ACTION_LEAVE_MEET://离开会议
                break;
            case webSocketUtil.Action.ACTION_MEMBER_SILENT://成员禁言
                var phone=result.caller;
                if(result.status==webSocketUtil.Action.SUCCESS&&result.caller){
                    var name=this.getName(phone);
                    showInfo(name+"("+phone+")已禁止发言");
                    meetService.changeSlienceState(phone,result.action);
                }else if(result.status==webSocketUtil.Action.FAIL){
                    showInfo("禁言操作失败", true);
                }
                break;
            case webSocketUtil.Action.ACTION_MEMBER_SPEAK://成员发言
                var phone=result.caller;
                if(result.status==webSocketUtil.Action.SUCCESS&&result.caller){
                    var name=this.getName(phone,result.action);
                    showInfo(name+"("+phone+")已允许发言");
                    meetService.changeSlienceState(phone);
                }else if(result.status==webSocketUtil.Action.FAIL){
                    showInfo("允许发言操作失败", true);
                }
                break;
            case webSocketUtil.Action.ACTION_BEGIN_PLAY_VOICE://开始放音
                if(result.status==webSocketUtil.Action.SUCCESS){
                    showInfo("开始会场放音");
                }else{
                    showInfo("会场放音失败",true);
                }
                break;
            case webSocketUtil.Action.ACTION_STOP_PLAY_VOICE://停止放音
                if(result.status==webSocketUtil.Action.SUCCESS){
                    showInfo("结束会场放音");
                }else{
                    showInfo("结束会场放音失败",true);
                }
                break;
            case webSocketUtil.Action.ACTION_CLOSE_MEET://结束会议
                if(result.status==webSocketUtil.Action.SUCCESS){
                    showInfo("会议结束");
                    closeWindow();
                }else{
                    showInfo("结束会议操作失败", true);
                    $("#loading").hide();
                }
                break;
            case webSocketUtil.Action.ACTION_CALL_STATUS_REPORT://呼叫状态变更通知
                meetService.changeInviteStatus(result.caller, result.status,result.action,result.ringRec);
                break;
            case webSocketUtil.Action.ACTION_GET_MEET_CALL_STATUS://获取会议成员呼叫状态
                if(result.status==webSocketUtil.Action.SUCCESS){
                    var callers=result.content;
                    for(var i=0,len=callers.length;i<len;i++){
                        meetService.changeInviteStatus(callers[i].caller, callers[i].status,result.action,result.ringRec);
                    }
                }else{
                    if(result.content.indexOf("会议ID不存在")!=-1){
                        showCloseTip();
                        meetService.finishMeet();
                    }
                }
                setTimeout("meetService.checkStatus()",5000);
                break;
            case webSocketUtil.Action.ACTION_MEET_REPORT_NUMBER:
                break;
            case webSocketUtil.Action.ACTION_MEET_OPER_LOCK://会议
                showInfo(result.content);
                break;
            case webSocketUtil.Action.ACTION_BEGIN_RECORD://开始录音
                if(result.status==webSocketUtil.Action.SUCCESS){
                    //改变会议记录录音状态
                    meetService.changeRecordState();
                    $("#btn_record").text("结束录音");
                }
                showInfo(result.content);
                break;
            case webSocketUtil.Action.ACTION_STOP_RECORD://结束录音
                if(result.status==webSocketUtil.Action.SUCCESS){
                    $("#btn_record").text("开始录音");
                }
                showInfo(result.content);
                break;
        }
    },
    //改变会议邀请人状态
    changeInviteStatus:function () {
        var phone=arguments[0];
        var code=arguments[1];
        var action=arguments[2];
        var ringRec=arguments[3];
        if(code==webSocketUtil.Status.CALL_STATUS_ANSWER){//加入会议
            var obj=meetService.removeInvite(phone);
            var name=obj.name,role=obj.role;
            if(!name){
                name=meetService.getName(phone);
            }
            if(role===undefined){
                role=meetService.getRole(phone);
            }
            meetService.addInMeet({name:name, phone:phone,role:role,id:obj.id});
            if(action==webSocketUtil.Action.ACTION_CALL_STATUS_REPORT){
                // showInfo(name+"("+phone+")"+"加入会议");
                var hostNum=$("#hostNum").val();
                if(hostNum==phone&&meetService.isFirst){//如果呼叫的是主持人且会议室刚开始，则在主持人加入会议，呼叫所有邀请人
                    meetService.callAllInvite();
                }
            }
        }else if(code==webSocketUtil.Status.CALL_STATUS_BYE||code==webSocketUtil.Status.CALL_STATUS_LEAVE_MEET){//离开会议
            var obj=meetService.removeInMeet(phone);
            var name=obj.name,role=obj.role;
            if(!name){
                name=meetService.getName(phone);
            }
            if(role===undefined){
                role=meetService.getRole(phone);
            }
            // if(action==webSocketUtil.Action.ACTION_CALL_STATUS_REPORT&&code==webSocketUtil.Status.CALL_STATUS_LEAVE_MEET){
            //     showInfo(name+"("+phone+")"+"离开会议");
            // }
            meetService.addInvite({name:name,phone:phone,role:role,id:obj.id});
            $("#btn_callAll").removeAttr("disabled");
        }else{
            meetService.changeState(phone,code,ringRec);
            //如果拒接，且开启拒接后短信通知，则发送短信通知
            // var notice=$("#noticeFailed").bootstrapSwitch("state");
            // if(notice&&code==webSocketUtil.Status.CALL_STATUS_REJECT){
            //     meetService.sendMsg($("#rid").val(),phone);
            // }
        }
    },
    changeState:function () {
        var phone=arguments[0];
        var code=arguments[1];
        var ringRec=arguments[2];
        $("#invite table>tbody>tr").each(function(){
            var num=$(this).find("td:eq(1)").text().trim();
            if(num==phone){
                var content=webSocketUtil.getStatusByCode(code);
                if(code==webSocketUtil.Status.CALL_STATUS_BYE||code==webSocketUtil.Status.CALL_STATUS_FAIL||code==webSocketUtil.Status.CALL_STATUS_REJECT){
                    $(this).find("td:last a.call-end").attr("class","call");
                    if(code==webSocketUtil.Status.CALL_STATUS_FAIL||code==webSocketUtil.Status.CALL_STATUS_REJECT){
                        content+="<a class=\"play\" data-rec=\""+meetService.getRecPrefix()+ringRec+"\"></a>";
                    }
                }else{
                    $(this).find("td:last a.call").attr("class","call-end");
                }
                $(this).find("td:eq(2)").html(content);
            }
        });
    },
    //呼叫所有会议邀请人
    callAllInvite:function () {
        var invite=$("body").data("invite");
        if(invite&&invite.length>0){
            var meetId=$("#meetId").val();
            var callers=[];
            var allowSpeak=$("#allowBegin").bootstrapSwitch("state");
            var level=allowSpeak?webSocketUtil.Action.MEET_LEVEL_CHARIMAN:webSocketUtil.Action.MEET_LEVEL_AUDIENCE;
            var hosts=meetService.getHostNum();
            for(var i=0,len=invite.length;i<len;i++){
                if(meetService.isHost(invite[i].phone)){//判断是否为主持人
                    level=webSocketUtil.Action.MEET_LEVEL_CHARIMAN;
                }
                callers.push({name:invite[i].name,caller:invite[i].phone,level:level});
            }
            webSocketUtil.joinMeeting(meetId, callers);
        }
    },
    //初始化Websocket
    initWebsocket:function () {
        var meetId=$("#meetId").val();
        var reqId=webSocketUtil.getReqId();
        var url=this.serviceUrl+"?reqId="+reqId+"&action=&meetId="+meetId;

        webSocketUtil.getWebSocket(url,{
            onmessage:function (e) {
                //关闭呼叫全部的tooltip
                $("#invite_ctrl").tooltip("destroy");
                meetService.reciveResult(e);
            }
        });
    },
    //http创建会议
    createMeet:function (obj) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/meet/createmeet",
            dataType:"json",
            data:obj,
            success:function(data){
                $("#startMeetBtn").removeAttr("disabled");
                if(data.result){
                    $("#meetStatus").text("会议中...");
                    $("#meetId").val(data.data.meetId);
                    $("#hostNum").val(data.data.hostNum);
//				$("#startMeetBtn").text("结束会议");
                    meetService.initWebsocket();
                    meetService.changeMeetControlState();
                }else {
                    if(data.result===false){
                        showTip("操作失败！",null);
                    }else if(data.result===undefined){
                        showTip("登陆超时，请重新登陆！",function () {
                            location.href=common.getContextPath()+"/login";
                        });
                    }
                    meetService.isFirst=false;
                }
            },error:function(err){
                console.log(err.responseText);
                showTip("操作失败！",null);
            },complete:function (XMLHttpRequest, textStatus) {
                $("#startMeetBtn").removeAttr("disabled");
            }
        });
    },
    //加载个人联系人数据
    //data：数据源
    //fields：数据字段
    //type：加载的是个人联系人还是公共联系人数据
    loadContacts:function (data, fields, type) {
        if (data) {
            var meetId=$("#meetId").val();
            var content = "";
            for (var i = 0, len = data.length; i < len; i++) {
                content += "<div class=\"group\" data-toggle=\"collapse\" data-target=\"#" + type + "_" + i + "\" aria-expanded=\"false\" aria-controls=\"" + type + "_" + i + "\">";
                content += "<i class=\"icon-caret-right\"></i>&nbsp;&nbsp;<input type=\"checkbox\" class=\"chkAll\" />&nbsp;" + data[i].name + "</div>";
                content += "<div class=\"collapse\" id=\"" + type + "_" + i + "\">";
                if (data[i].children && data[i].children.length > 0) {
                    content += " <table class=\"table table-hover table-condensed\">";
                    var children = data[i].children;
                    for (var j = 0, l = children.length; j < l; j++) {
                        content += "<tr>";
                        for (var n = 0, size = fields.length; n < size; n++) {
                            var txt=children[j][fields[n]];
                            txt=txt?txt:"";
                            if (n == 0) {
                                content += "<td><input type=\"checkbox\" />&nbsp;" + txt + "</td>";
                                continue;
                            }
                            content += "<td>" + txt + "</td>";
                        }
                        //判断会议是否开始
                        if(meetId){
                            content += "<td><a href=\"javascript:; \" class=\"call\"></a></td></tr>";
                        }else{
                            content += "<td><a href=\"javascript:; \" class=\"add-invite\"></a></td></tr>";
                        }

                    }
                    content += "</table>";
                } else {
                    content += "<div class=\"text-center\">无联系人数据</div>";
                }
                content += "</div>";
            }
            //console.log(content);
            $("#"+type).append(content);
        }
    },
    //联系人搜索
    searchContacts:function (str) {
        if (str.trim() == "") {
            return;
        }
        var result = [];
        var personalContacts = $("body").data("personalContacts");
        var publicContacts = $("body").data("publicContacts");
        if (personalContacts && personalContacts.length > 0) {
            for (var i = 0, len = personalContacts.length; i < len; i++) {
                var children = personalContacts[i].children;
                var group=personalContacts[i].name;
                for(var n = 0, size=children.length; n<size;n++){
                    var name = children[n].name;
                    var phone = children[n].phone;
                    if (name.indexOf(str) != -1 || phone.indexOf(str) != - 1) {
                        result.push({name: name, phone: phone, type: "个人通讯录",group:group});
                    }
                }
            }
        }
        if (publicContacts && publicContacts.length > 0) {
            for (var i = 0, len = publicContacts.length; i < len; i++) {
                var children = publicContacts[i].children;
                var group=publicContacts[i].name;
                for (var n = 0, size = children.length; n < size; n++) {
                    var name = children[n].name;
                    var phone = children[n].phone;
                    var position = children[n].position;
                    if (name.indexOf(str) != -1 || phone.indexOf(str) != -1 || (position && position.indexOf(str) != -1)) {
                        result.push({ name: name, phone: phone, type: "公共通讯录",group:group});
                    }
                }

            }
        }
        var content = "";

        if (result.length > 0) {
            for (var i = 0, len = result.length; i < len; i++) {
                var temp=result[i].name+"("+result[i].phone+")--"+result[i].type+"("+result[i].group+")";
                content+="<li title=\""+temp+"\">"+temp+"</li>";
            }
        } else {
            content = "<li>无搜索结果...</li>";
        }
        $("#contactsResult").html(content).show();
    },
    //选中搜索的联系人结果
    getContactResult:function (phone, type, group) {
        $("#contact_tab li:eq(" + type + ") a").click();
        var $tar = $("#contacts_content>div:eq(" + type + ")").find("div.group");
        $("#contacts_content div.collapse table tr").removeClass("success");
        $tar.each(function () {
            if ($(this).text().trim() == group) {
                $(this).next().collapse("show");
                $(this).next().find("tr").each(function () {
                    if ($(this).find("td:eq(1)").text().trim() == phone) {
                        $(this).addClass("success");
                        return false;
                    }
                });
                $(this).find("i").attr("class", "icon-caret-down");
            } else {
                $(this).next().collapse("hide");
                $(this).find("i").attr("class", "icon-caret-right");
            }
        });
    },
    //加载会议邀请人数据
    loadInvite:function (data) {
        if(data&&data.length>0){
            var meetId=$("#meetId").val();
            var content="";
            for(var i=0,len=data.length;i<len;i++){
                //如果邀请人已被删除，则不加载
                if(data[i].deleted){
                    continue;
                }
                var name=data[i].name;
                var phone=data[i].phone;
                meetService.changeContactAdded(phone);
                var display="";
                if(data[i].role==RoleType.HOST){
                    content+="<tr class='t_host' data-id='"+data[i].id+"' data-name='"+data[i].name+"' data-role='"+data[i].role+"'><td><i class='host'></i>"+name+"</td><td>"+phone+"</td><td>"+webSocketUtil.getStatusByCode(data[i].status)+"</td><td>";
                    display="style='display:none;'";
                }else{
                    content+="<tr class='t_attendee' data-id='"+data[i].id+"' data-name='"+data[i].name+"' data-role='"+data[i].role+"'><td>"+name+"</td><td>"+phone+"</td><td>"+webSocketUtil.getStatusByCode(data[i].status)+"</td><td>";
                }
                if(meetId){
                    content+="<a href=\"javascript:;\" class=\"call\"></a>&nbsp;&nbsp;<a href=\"javascript:void(0);\" class=\"msg\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"clear\" "+display+"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\" "+display+"></a></td></tr>";
                }else{
                    content+="<a href=\"javascript:void(0);\" class=\"msg\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"clear\" "+display+"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\" "+display+"></a></td></tr>";
                    meetService.addHost(name, phone);
                }
            }
            $("#invite table tbody").append(content);
        }
    },
    //移除参会人数据
    removeInvite:function () {
        var phone = arguments[0];
        var isHand=arguments[1];//是否是手动移除
        var name,role,id;
        $("#invite table>tbody>tr").each(function(){
            var tel=$(this).find("td:eq(1)").text().trim();
            if(tel==phone){
                name=$(this).find("td:eq(0)").text().trim();
                role=$(this).data("role");
                $(this).remove();
                return false;
            }
        });

        var meetId=$("#meetId").val();
        var invite=$("body").data("invite");

        if(invite&&invite.length>0){
            for(var i=0,len=invite.length;i<len;i++){
                if(invite[i].phone==phone){
                    if(!name){
                        name=invite[i].name;
                    }
                    if(role===undefined){
                        role=invite[i].role;
                    }
                    id=invite[i].id;
                    if(meetId){
                        if(isHand){
                            invite[i].deleted=true;
                        }else{
                            invite.splice(i,1);
                        }
                    }else{
                        invite.splice(i,1);
                    }
                    break;
                }
            }
        }

        $("body").data("invite",invite);
        if(!meetId||isHand){
            //将邀请人数据从服务器缓存中移除
            var rid=$("#rid").val();
            $.ajax({
                type:"post",
                url:common.getContextPath()+"/meet/delInvite",
                dataType:"json",
                data:{"rid":rid,"name":name,"phone":phone},
                success:function(data){
                    if(data.result){

                    }else{
                        if(data.result===undefined){
                            showTip("登陆超时，请重新登陆",function () {
                               location.href=common.getContextPath()+"/login";
                            });
                        }else{
                            showTip("操作失败！")
                        }
                    }
                },error:function(err){
                    console.log(err.responseText);
                }
            });
            meetService.removeHost(phone);
            $("#contacts_content div.collapse table tr").each(function(){
                var tel=$(this).find("td:eq(1)").text().trim();
                if(tel==phone){
                    var btn;
                    if(meetId){
                        btn="<a href=\"javascript:;\" class=\"call\"></a>";
                    }else{
                        btn="<a href=\"javascript:;\" class=\"add-invite\"></a>";
                    }
                    $(this).find("td:last").html(btn);
                    return false;
                }
            });
        }

        // alert("name:"+name+",phone:"+phone+",role:"+role);
        return {name:name,role:role,id:id};
    },
    //改变联系人的添加状态为已添加
    changeContactAdded:function (phone) {
        $("#contacts_content div.collapse>table tr").each(function(){
            var telnum=$(this).find("td:eq(1)").text().trim();
            if(telnum==phone){
                $(this).find("td:last").text("已添加");
            }
        });
    },
    //加载参会人数据
    loadInMeet:function (data) {
        if(data&&data.length>0){
            var content="";
            for(var i=0,len=data.length;i<len;i++){
                var phone=data[i].phone;
                meetService.changeContactAdded(phone);
                if(data[i].role==RoleType.HOST){
                    content+="<tr class='t_host' data-id='"+data[i].id+"' data-name='"+data[i].name+"' data-role='"+data[i].role+"'><td><i class='host'></i>"+data[i].name+"</td><td>"+phone+"</td><td>"+meetService.getAttendeeRole(data[i].role)+"</td><td><a href=\"javascript:;\" class=\"call-end\"></a>&nbsp;&nbsp;";
                    content+="<a href=\"javascript:;\" class=\"mic\"></a>&nbsp;&nbsp;<a href='javascript:void(0);' class='host' title='设置主持人' style='display: none;'></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\" style='display: none;'></a>&nbsp;&nbsp;</td></tr>";
                }else{
                    content+="<tr class='t_attendee' data-id='"+data[i].id+"' data-name='"+data[i].name+"' data-role='"+data[i].role+"'><td>"+data[i].name+"</td><td>"+phone+"</td><td>"+meetService.getAttendeeRole(data[i].role)+"</td><td><a href=\"javascript:;\" class=\"call-end\"></a>&nbsp;&nbsp;";
                    content+="<a href=\"javascript:;\" class=\"mic\"></a>&nbsp;&nbsp;<a href='javascript:void(0);' class='host' title='设置主持人'></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
                }

            }
            $("#attendee_list table tbody").html(content);
        }
    },
    //获取用户角色
    getAttendeeRole:function (role) {
        if(role==RoleType.HOST){
            return "主持人";
        }else{
            return "参会人";
        }
    },
    //移除参会者
    removeInMeet:function (phone) {
        var inmeet=$("body").data("inmeet");
        var name,role,id;

        $("#attendee_list table>tbody>tr").each(function(){
            var num=$(this).find("td:eq(1)").text().trim();
            if(num==phone){
                name=$(this).data("name");
                role=$(this).data("role");
                $(this).remove();
                return false;
            }
        });
        if(inmeet&&inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                if(phone==inmeet[i].phone){
                    if(!name){
                        name=inmeet[i].name;
                    }
                    if(role===undefined){
                        role=inmeet[i].role;
                    }
                    id=inmeet[i].id;
                    // alert("name:"+name+",role:"+role+",phone:"+phone);
                    inmeet.splice(i,1);
                    break;
                }
            }
            $("body").data("inmeet",inmeet);
        }
        return {name:name,role:role,id:id};
    },
    //获取主持人号码
    getHostNum:function () {
        var num=[];
        var invite=$("body").data("invite");
        var inmeet=$("body").data("inmeet");
        if(invite){
            for(var i=0;i<invite.length;i++){
                if(invite[i].role==RoleType.HOST){
                    num.push(invite[i].phone);
                }
            }
        }
        if(inmeet){
            for(var i=0;i<inmeet.length;i++){
                if(inmeet[i].role==RoleType.HOST){
                    num.push(inmeet[i].phone);
                }
            }
        }
        return num;
    },
    //是否为主持人
    isHost:function (phone) {
      var hosts=meetService.getHostNum()||[];
      var flag=false;
      for(var i=0;i<hosts.length;i++){
          if(phone==hosts[i]){
              flag=true;
              break;
          }
      }
      return flag;
    },
    //获取主持人名字
    getHostName:function () {
        var name = $("#mhost").text();
        name=name.substring(0,name.indexOf("（"));
        return name;
    },
    //添加参会者
    addInMeet:function (obj) {
        var name=obj.name;
        var phone=obj.phone;
        var role=obj.role||RoleType.ATTENDEE;
        var inmeet=$("body").data("inmeet");
        if(!inmeet){
            inmeet=[];
        }
        var flag=true;
        for(var i=0,len=inmeet.length;i<len;i++){
            if(inmeet[i].phone==phone){
                flag=false;
                break;
            }
        }
        // alert(JSON.stringify(inmeet));
        if(flag){
            meetService.changeContactAdded(phone);
            var hostNum=$("#hostNum").val();
            if(phone==hostNum){
                role=RoleType.HOST;
            }
            inmeet.push({name:name,phone:phone,role:role});
            showInfo(name+"("+phone+")"+"加入会议");
            $("body").data("inmeet",inmeet);
            var content;
            if(role==RoleType.HOST){
                content="<tr class='t_host' data-id='"+obj.id+"' data-name='"+name+"' data-role='"+role+"'><td><i class='host'></i>"+name+"</td><td>"+phone+"</td><td>"+meetService.getAttendeeRole(role)+"</td><td><a href=\"javascript:;\" class=\"call-end\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"mic\"></a>&nbsp;&nbsp;<a href='javascript:void(0);' class='host' title='设置主持人' style='display: none;'></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\" style='display: none;'></a>&nbsp;&nbsp;</td></tr>";
                var $host=$("#attendee_list table>tbody").find("tr.t_host");
                if($host&&$host.length>0){
                    $($host[0]).after(content);
                }else{
                    $("#attendee_list table>tbody").prepend(content);
                }
            }else{
                content="<tr class='t_attendee' data-id='"+obj.id+"' data-name='"+name+"' data-role='"+role+"'><td>"+name+"</td><td>"+phone+"</td><td>"+meetService.getAttendeeRole(role)+"</td><td><a href=\"javascript:;\" class=\"call-end\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"mic\"></a>&nbsp;&nbsp;<a href='javascript:void(0);' class='host' title='设置主持人'></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a>&nbsp;&nbsp;</td></tr>";
                $("#attendee_list table>tbody").append(content);
            }
        }
    },
    //将邀请人数据添加到服务器
    addInviteToServer:function (name,phone) {
        var rid=$("#rid").val();
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/meet/addInvite",
            dataType:"json",
            data:{"rid":rid,"name":name,"phone":phone},
            success:function(data){
                common.isLoginTimeout(data);
                if(data.result){
                    var invite=$("body").data("invite")||[];
                    invite.push({name:name,phone:phone,status:webSocketUtil.Status.CALL_STATUS_WAIT,role:"参会人"});
                    $("body").data("invite",invite);
                }else{
                    showTip({content:"操作失败！"});
                }
            },error:function(err){
                console.log(err.responseText);
                showTip({content:"操作失败！"});
            }
        });
    },
    //删除选择的联系人数据
    removeSelected:function (name,phone) {
        var selected=$("body").data("selected");
        for(var i=0,len=selected.length;i<len;i++){
            if(selected[i].phone==phone&&selected[i].name==name){
                selected.splice(i,1);
                break;
            }
        }
        $("body").data("selected",selected);
    },
    //添加选择的联系人数据
    addSelected:function (name, phone) {
        var flag = true;
        var selected = $("body").data("selected");
        if (!selected) {
            selected=[];
        }
        if (selected.length > 0) {
            for (var i = 0, len = selected.length; i < len; i++) {
                if (selected[i].phone == phone) {
                    flag = false;
                    break;
                }
            }
        }
        if (flag) {
            selected.push({ name: name, phone: phone });
            $("body").data("selected",selected);
        }
    },
    //向下
    pushDown:function (dom,phone) {
        $(dom).attr("class","push-up");
        var $tbody=$(dom).closest("tbody");
        var $tr=$(dom).closest("tr");
        $tr.remove();
        $tbody.append($tr);
    },
    //向上
    pushUp:function (dom,phone) {
        $(dom).attr("class","push-down");
        var $tbody=$(dom).closest("tbody");
        var $tr=$(dom).closest("tr");
        $tr.remove();
        var $host=$tbody.find("tr.t_host");
        if($host&&$host.length>0){
            $($host[0]).after($tr);
        }else{
            $tbody.prepend($tr);
        }
    },
    //选中会议中参会人搜索结果
    getInMeetResult:function (phone) {
        $("#attendee_list table>tbody>tr").removeClass("success");
        $("#attendee_list table>tbody>tr").each(function(){
            var telnum=$(this).find("td:eq(1)").text().trim();
            if(telnum==phone){
                $(this).addClass("success");
                return false;
            }
        });
    },
    //搜索会议中的参会人
    searchInMeet:function (str) {
        if(str.trim()==""){
            return;
        }
        var inmeet=$("body").data("inmeet");
        var result=[];
        if(inmeet&&inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                var name=inmeet[i].name;
                var phone=inmeet[i].phone;
                if(name.indexOf(str)!=-1||phone.indexOf(str)!=-1){
                    result.push({name:name,phone:phone});
                }
            }
        }
        var content="";
        if(result.length>0){
            for (var i = 0, len = result.length; i < len; i++) {
                var temp=result[i].name+"("+result[i].phone+")";
                content+="<li title=\""+temp+"\">"+temp+"</li>";
            }
        }else{
            content = "<li>无搜索结果...</li>";
        }
        $("#inmeetResult").html(content).show();
    },
    //选中参会人搜索结果
    getInviteResult:function (phone) {
        $("#invite table>tbody>tr").removeClass("success");
        $("#invite table>tbody>tr").each(function(){
            var telnum=$(this).find("td:eq(1)").text().trim();
            if(telnum==phone){
                $(this).addClass("success");
                return false;
            }
        });
    },
    //搜索参会人
    searchInvite:function (str) {
        if (str.trim() == "") {
            return;
        }
        var invite=$("body").data("invite");
        var result=[];
        if(invite&&invite.length>0){
            for(var i=0,len=invite.length;i<len;i++){
                var name=invite[i].name;
                var phone=invite[i].phone;
                if(name.indexOf(str)!=-1||phone.indexOf(str)!=-1){
                    result.push({name:name,phone:phone});
                }
            }
        }
        var content="";
        if(result.length>0){
            for (var i = 0, len = result.length; i < len; i++) {
                var temp=result[i].name+"("+result[i].phone+")";
                content+="<li title=\""+temp+"\">"+temp+"</li>";
            }
        }else{
            content = "<li>无搜索结果...</li>";
        }
        $("#inviteResult").html(content).show();
    },
    //添加会议邀请人
    addInvite:function (obj) {
        var name=obj.name;
        var phone=obj.phone;
        var role=obj.role||RoleType.ATTENDEE;
        var flag=true;
        //验证号码是否已在邀请人中或已在会议中
        var invite=$("body").data("invite")||[];
        var inmeet=$("body").data("inmeet")||[];

        if(invite&&invite.length>0){
            for(var i=0,len=invite.length;i<len;i++){
                if(phone==invite[i].phone){
                    if(invite[i].deleted&&obj.isHand){
                        invite[i].deleted=false;
                    }else{
                        flag=false;
                    }
                    break;
                }
            }
        }
        if(inmeet&&inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                if(phone==inmeet[i].phone){
                    flag=false;
                    break;
                }
            }
        }

        if(flag){
            meetService.changeContactAdded(phone);
            if(!invite){
                invite=[];
            }
            if(!inmeet){
                inmeet=[];
            }

            var meetId=$("#meetId").val();
            if(!meetId||obj.isHand){//如果会议未开始，或者为手动添加，则还需要将参会添加到服务器
                meetService.addInviteToServer(name,phone);
                meetService.addHost(name,phone);
            }else{
                invite.push({id:obj.id,name:name,phone:phone,status:webSocketUtil.Status.CALL_STATUS_WAIT,role:role});
                $("body").data("invite",invite);
                showInfo(name+"("+phone+")"+"离开会议");
            }

            //meetService.addHost(name, phone);
            var status=webSocketUtil.getStatusByCode(webSocketUtil.Status.CALL_STATUS_WAIT);
            var content="";
            if(role==RoleType.HOST){
                content="<tr class='t_host' data-id='"+obj.id+"' data-name='"+name+"' data-role='"+role+"'><td><i class='host'></i>"+name+"</td><td>"+phone+"</td><td>"+status+"</td><td>";
            }else{
                content="<tr class='t_attendee' data-id='"+obj.id+"' data-name='"+name+"' data-role='"+role+"'><td>"+name+"</td><td>"+phone+"</td><td>"+status+"</td><td>";
            }
            var meetId=$("#meetId").val();
            if(meetId){
                var display=(role==RoleType.HOST)?"style='display:none'":"";
                content+="<a href=\"javascript:;\" class=\"call\"></a>&nbsp;&nbsp;<a  href=\"javascript:;\" class=\"msg\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"clear\" "+display+"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\" "+display+"></a></td></tr>";
            }else{
                content+="<a  href=\"javascript:;\" class=\"msg\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"clear\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
            }
            if(role==RoleType.HOST){
                var $host = $("#invite table tbody").find("tr.t_host");
                if($host&&$host.length>0){
                    $($host[0]).after(content);
                }else{
                    $("#invite table tbody").prepend(content);
                }
            }else{
                $("#invite table tbody").append(content);
            }

        }
        return flag;
    },
    //全员静音
    allSlience:function () {
        var meetId=$("#meetId").val();
        var hostNum=meetService.getHostNum()||[];
        var inmeet=$("body").data("inmeet");
        var callers=[];
        if(inmeet&&inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                var name=inmeet[i].name;
                var phone=inmeet[i].phone;
                var flag=true;
                for(var n=0;n<hostNum.length;n++){
                    if(phone==hostNum[n]){
                        flag=false;
                    }
                }
                if(flag){
                    callers.push({name:name,caller:phone});
                }
            }
            if(callers.length>0){
                webSocketUtil.forbidTalk(meetId, callers);
            }
        }
    },
    //全体允许发言
    allSpeak:function () {
        var meetId=$("#meetId").val();
        var hostNum=$("#hostNum").val();
        var inmeet=$("body").data("inmeet");
        var callers=[];
        if(inmeet.length>0){
            for(var i=0,len=inmeet.length;i<len;i++){
                var name=inmeet[i].name;
                var phone=inmeet[i].phone;
                if(phone!=hostNum){
                    callers.push({name:name,caller:phone});
                }
            }
            if(callers.length>0){
                webSocketUtil.allowTalk(meetId, callers);
            }
        }
    },
    //根据数据生成主持人选项
    createHostChoice:function (data) {
        var result = "<ul class='list-unstyled' id='selectHost'>";
        var hostNum = $("#mhost").text();
        for (var i = 0, len = data.length; i < len; i++) {
            if (hostNum == data[i].name + "（" +data[i].phone + "）") {
                result += "<li class='bg-success' onclick='meetService.select(this)'>" +data[i].name + "（" +data[i].phone + "）</li>";
            } else {
                result += "<li onclick='meetService.select(this)'>" +data[i].name + "（" +data[i].phone + "）</li>";
            }
        }
        result += "</ul>";
        return result;
    },
    select:function (dom) {
        $("#selectHost li").removeAttr("class");
        $(dom).addClass("bg-success");
    },
    //开始或者结束录音
    startOrFinishRecord:function (flag,meetId) {
        if(flag){
            webSocketUtil.startRecord(meetId);
        }else{
            webSocketUtil.stopRecord(meetId);
        }
    },
    //改变会议录音状态
    changeRecordState:function () {
        var isRecord =$("#isRecord").bootstrapSwitch("state");
        //如果会议会议开始时未设置会议开始自动录音，且是第一次手动开始录音
        //则需要改变会议记录中的录音状态
        if(!isRecord&&meetService.isFirstStartRecord){
            var rid=$("#rid").val();
            $.ajax({
                type:"get",
                url:common.getContextPath()+"/meet/updateRecordState/"+rid,
                dataType:"json",
                success:function (data) {
                    console.log("change record state success:"+data.result);
                },error:function (err) {
                    console.log(err.responseText);
                }
            });
        }
    },
    //呼叫
    call:function (meetId,callers) {
        var allowSpeak=$("#allowBegin").bootstrapSwitch("state");
        var level=allowSpeak?webSocketUtil.Action.MEET_LEVEL_CHARIMAN:webSocketUtil.Action.MEET_LEVEL_AUDIENCE;
        var hosts=meetService.getHostNum();

        //呼叫号码，该号码呼叫按钮变为不能呼叫状态
        var rows = $("#invite table tbody tr");
        if(rows){
            var $row;
            for(var n=0,size=callers.length;n<size;n++) {
                for(var i=0,len=rows.length;i<len;i++){
                    $row=$(rows[i]);
                    var phone=$row.find("td:eq(1)").text().trim();
                    if(callers[n].caller==phone){
                        var callBtn = $row.find("a.call");
                        if(callBtn){
                            $(callBtn).attr("class","call disabled");
                        }
                        break;
                    }
                }
                //判断是否为主持人，如果是则主持人必须是主席
                if(meetService.isHost(callers[n].caller)){
                    level=webSocketUtil.Action.MEET_LEVEL_CHARIMAN;
                }
                callers[n].level=level;
            }
        }
        webSocketUtil.joinMeeting(meetId,callers);
    },
    /**
     * 添加个人联系人
     * @param obj
     */
    addPersonalContact:function (obj) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/meet/addPersonalContact",
            dataType:"json",
            data:obj,
            success:function (data) {
                common.isLoginTimeout(data);
                if(data.result){//添加成功
                    var groups = $("#personl_contacts").children("div.group");
                    var length=groups.length;
                    //个人联系人中没有分组
                    if(length==0){
                        var contactGrp="<div class='group' data-toggle='collapse' data-target='#personl_contacts_"+length+"' " +
                            " aria-expanded='true' aria-controls='personl_contacts_"+length+"'>";
                        contactGrp+="<i class='icon-caret-right'></i><input type='checkbox' class='chkAll'>未分组</div>";
                        contactGrp+="<div class='collapse' id='personl_contacts_"+length+"' aria-expanded='true'>";
                        contactGrp+="<table class='table table-hover table-condensed'><tbody><tr><td><input type='checkbox'>&nbsp;"+obj["c.name"]
                            +"</td><td>"+obj["c.phone"]+"</td><td>已添加</td></tr></tbody></table></div>";
                        $("#personl_contacts").append(contactGrp);
                    }else{
                        var $lastGroup=$(groups[length-1]);

                        if($lastGroup.text().trim()=="未分组"){
                            var contact="<tr>";
                            contact+="<td><input type='checkbox'>&nbsp;"+obj["c.name"]+"</td>";
                            contact+="<td>"+obj["c.phone"]+"</td>";
                            contact+="<td>已添加</td>";
                            contact+="</tr>";
                            var $tbody = $lastGroup.next().find("table>tbody");
                            if($tbody.get(0)){//未分组中有数据
                                $tbody.append(contact);
                            }else{//未分组中没有数据
                                $lastGroup.next().html("<table class='table table-hover table-condensed'><tbody>"+contact+"</tbody></table>");
                            }
                        }else{//没有未分组
                            var contactGrp="<div class='group' data-toggle='collapse' data-target='#personl_contacts_"+length+"' " +
                                " aria-expanded='true' aria-controls='personl_contacts_"+length+"'>";
                            contactGrp+="<i class='icon-caret-right'></i><input type='checkbox' class='chkAll'>未分组</div>";
                            contactGrp+="<div class='collapse' id='personl_contacts_"+length+"' aria-expanded='true'>";
                            contactGrp+="<table class='table table-hover table-condensed'><table><tbody><tr><td><input type='checkbox'>&nbsp;"+obj["c.name"]
                                +"</td><td>"+obj["c.phone"]+"</td><td>已添加</td></tr></tbody></table></div>";
                            $lastGroup.next().after(contact);
                        }
                    }
                }
            },error:function (err) {
                console.log(err.responseText);
            }
        });
    },
    /**
     * 发送通知短信
     * @param rid 会议记录的id
     * @param phone 要通知的手机号码
     * @param name 参会人姓名
     */
    sendMsg:function (rid,phone,name) {
        $.post(common.getContextPath()+"/msg/send",{rid:rid,phone:phone,name:name},function (data) {
            showInfo("已向"+name+"("+phone+")发送通知");
            Alert("已向"+name+"("+phone+")发送通知");
        });
    },
    /**
     * 获取录音地址前缀
     */
    getRecPrefix:function () {
        return $("#recPrefix").val();
    },
    /**
     * 播放录音
     * @param recUrl
     */
    playRec:function (recUrl) {
        var content="<div style='margin: 20px 80px;'><audio preload='auto' controls src='"+recUrl+"'>对不起，您的浏览器不支持播放音频文件！</audio></div>";
        content+="<script>$('audio').audioPlayer()</script>";
        showTip(content,function () {
           $("audio")[0].pause();
        },"播放音频","default");
    },
    /**
     * obj.rid 会议记录id
     * obj.phone 要设置的为主席的参会人电话
     * obj.meetId
     * obj.dom 点击对象
     * @param obj
     */
    setHost:function (obj) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/meet/setHost",
            dataType:"json",
            data:{rid:obj.rid,phone:obj.phone,meetId:obj.meetId},
            success:function (data) {
                if(data.result){
                    var hostNum=$("#hostNum").val();
                    //参会人
                    var inmeet=$("body").data("inmeet")||[];
                    for(var i=0;i<inmeet.length;i++){
                        var phone=inmeet[i].phone;
                        if(phone!=hostNum&&phone!=obj.phone){
                           inmeet[i].role=RoleType.ATTENDEE;
                           inmeet[i].host=false;
                        }
                        if(phone==obj.phone){
                            inmeet[i].role=RoleType.HOST;
                            inmeet[i].host=true;
                        }
                    }
                    //将设置的主持人置顶
                    var $tr = $(obj.dom).closest("tr");
                    var name = $tr.find("td:first").text();
                    $tr.find("td:first").html("<i class='host'></i>"+name);
                    $tr.find("td:eq(2)").text(meetService.getAttendeeRole(RoleType.HOST));
                    $tr.find("a.host,.push-up,.push-down").hide();
                    $tr.data("role",RoleType.HOST);
                    var $host=$tr.closest("tbody").find("tr.t_host");
                    $tr.remove();
                    if($host&&$host.length>0){
                        $($host[0]).after($tr);
                        $tr.attr("class","t_host");
                    }else{
                        $tr.closest("tbody").prepend($tr);
                    }
                    //将其他第二主持人设置为参会人
                    $("#attendee_list table>tbody>tr").each(function(){
                        var num=$(this).find("td:eq(1)").text().trim();
                        var role=$(this).data("role");
                        if(num!=hostNum&&num!=obj.phone&&role==RoleType.HOST){
                            $(this).attr("class","t_attendee");
                            var name = $(this).find("td:first").text();
                            $(this).find("td:first").html(name);
                            $(this).find("td:eq(2)").text(meetService.getAttendeeRole(RoleType.ATTENDEE));
                            $(this).data("role",RoleType.ATTENDEE);
                            $(this).find("a.host,.push-up,.push-down").show();
                            return false;
                        }
                    });

                    //待参会人
                    var invite=$("body").data("invite")||[];
                    for(var i=0;i<invite.length;i++){
                        if(invite[i].phone!=hostNum&&invite[i].role==RoleType.HOST){
                            invite[i].role=RoleType.ATTENDEE;
                            invite[i].host=false;
                            break;
                        }
                    }
                    $("#invite table>tbody>tr").each(function () {
                        var num=$(this).find("td:eq(1)").text().trim();
                        var role=$(this).data("role");
                        if(num!=hostNum&&role==RoleType.HOST){
                            $(this).data("role",RoleType.ATTENDEE);
                            var name = $(this).find("td:first").text().trim();
                            $(this).find("td:first").html(name);
                            $(this).find(".push-up,.push-down,.clear").show();
                            $(this).attr("class","t_attendee");
                            return false;
                        }
                    });
                }else if(data.result===false){
                    showTip("操作失败！",null);
                }else{
                    showTip("登陆超时，请重新登陆",function () {
                       location.href=common.getContextPath()+"/login";
                    });
                }
            },error:function (err) {
                console.log(err.responseText);
                showTip("操作失败！",null);
            }
        });
    },
    //将邀请人状态设为主持人状态
    setInviteToHost:function (hostNum) {
        $("#hostNum").val(hostNum);
        //设置会议邀请人为主席
        var invite=$("body").data("invite")||[];
        for(var i=0;i<invite.length;i++){
            if(hostNum==invite[i].phone){
                invite[i].role=RoleType.HOST;
                invite[i].host=true;
                break;
            }
        }
        var $hostTr;
        $("#invite table>tbody>tr").each(function () {
            var num=$(this).find("td:eq(1)").text().trim();
            var role=$(this).data("role");
            var name = $(this).find("td:first").text().trim();
            if(num==hostNum){
                $(this).data("role",RoleType.HOST);
                $(this).find("td:first").html("<i class='host'></i>"+name);
                $(this).find(".push-up,.push-down,.clear").hide();
                $(this).attr("class","t_attendee");
                $hostTr=$(this);
            }else{
                $(this).data("role",RoleType.ATTENDEE);
                $(this).find("td:first").html(name);
                $(this).find(".push-up,.push-down,.clear").show();
                $(this).attr("class","t_attendee");
            }
        });
        $hostTr.remove();
        $("#invite table>tbody").prepend($hostTr);
    }
}
