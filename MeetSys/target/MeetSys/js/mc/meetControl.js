$(function () {
    //初始化tooltip
    $("[data-toggle='tooltip']").tooltip();
    var local={};
    //初始化
    meetService.initPage();
    //===========
    //显示或隐藏消息
    $("#slide_btn").click(function () {
        if (local.expanded) {
            $(this).find("span").attr("class", "glyphicon glyphicon-chevron-down");
            $("#info").animate({"height": "150px"}, 100);
            $("#info_list").show();
            local.expanded=false;
        } else {
            $(this).find("span").attr("class", "glyphicon glyphicon-chevron-up");
            $("#info_list").hide();
            $("#info").animate({ "height": "36px" }, 100);
            local.expanded=true;
        }
    });
    //初始化bootstrap switch
    $("input[name=mycheckbox]").bootstrapSwitch();
    //全选
    $("#contacts_content").on("click",".group input[type=checkbox]", function (e) {
        var checked = this.checked;
        var chks = $(this).parent().next().find("input[type=checkbox]");
        if (chks) {
            for (var i = 0, len = chks.length; i < len;i++){
                chks[i].checked = checked;
                var name = $(chks[i]).parent().text().trim();
                var phone = $(chks[i]).parent().next().text().trim();
                if (checked) {
                    meetService.addSelected(name,phone);
                } else {
                    meetService.removeSelected(name,phone);
                }
            }
        }
        window.event ? window.event.cancelBubble = true : e.stopPropagation();
    });

    //单选
    $("#contacts_content").on("click", "table input[type=checkbox]", function () {
        var name = $(this).parent().text().trim();
        var phone = $(this).parent().next().text().trim();
        meetService.addSelected(name,phone);
    });

    //从通讯录中批量添加参会人
    $("#addInviteBtn").click(function () {
        var selected=$("body").data("selected");
        var meetId=$("#meetId").val();
        var callers=[];
        if(selected&&selected.length>0){
            for(var i=0,len=selected.length;i<len;i++){
                var name=selected[i].name;
                var phone=selected[i].phone;
                meetService.addInvite({name:selected[i].name,phone:selected[i].phone,isHand:true});
                if(meetId){
                    callers.push({name:name,caller:phone});
                }
            }
            if(meetId){//呼叫
                //webSocketUtil.joinMeeting(meetId, callers);
                meetService.call(meetId,callers);
            }
        }
        $("body").data("selected",null);//清空缓存数据
        $("#contacts_content input[type=checkbox]").attr("checked",false);
    });

    //从通讯录中单个添加参会人
    $("#contacts_content").on("click","a.add-invite,a.call",function(){
        var $tr = $(this).closest("tr");
        var name=$tr.find("td:first").text().trim();
        var phone=$tr.find("td:eq(1)").text().trim();
        var id=$(this).closest("tr").data("id");
        meetService.addInvite({name:name,phone:phone,id:id,isHand:true});
        var meetId=$("#meetId").val();
        if(meetId){
            var callers=[{name:name,caller:phone}];
            //webSocketUtil.joinMeeting(meetId, callers);
            meetService.call(meetId,callers);
        }
    });

    //展开或收缩联系人组
    $("#contacts_content").on("click","div.group",function () {
        //判断是事件是否是从子元素冒泡上来的
        var event = window.event || arguments.callee.caller.arguments[0];
        var targetObj = event.srcElement || event.target;
        if(targetObj.tagName=="DIV"){
            var cls = $(this).find("i").attr("class");
            if (cls == "icon-caret-down") {
                $(this).find("i").attr("class", "icon-caret-right");
            } else {
                $(this).find("i").attr("class", "icon-caret-down");
            }
        }

    });

    //更换主题
    $("#edit_subject").click(function () {
        var subject = $("#msubject").text();
        $("#msubject").hide();
        $("#subject_form").css("display", "inline-block");
        $("#subject_txt").val(subject);
        $(this).hide();
    });
    //确认更换主题
    $("#btn_sub").click(function () {
        var subject = $("#subject_txt").val();
        $(this).parent().hide();
        $("#msubject").text(subject).attr("title",subject).css("display", "inline-block");
        $("#edit_subject").show();
    });

    //更换主持人
    $("#change_host").click(function () {
        var data=$("body").data("host");
        if(!data){
            data=[];
        }
        var content = meetService.createHostChoice(data);
        showModal("选择主持人", content, function () {
            var host = $("#selectHost li.bg-success").text().trim();
            $("#mhost").text(host).attr("title",host);
            var hostNum=host.substring(host.indexOf("（")+1,host.indexOf("）"));
            meetService.setInviteToHost(hostNum);
        });
    });

    //创建会议备忘录
    $("#btn_remark").click(function () {
        var rid=$("#rid").val();
        $.ajax({
            type:"get",
            url:common.getContextPath()+"/meet/getRemark/"+rid,
            dataType:"text",
            success:function(data){
                showModal("创建备忘录", "<textarea class='form-control' rows='5' id='remark'>"+data+"</textarea>", function () {
                    var remark = $("#remark").val();
                    $.ajax({
                        type:"post",
                        url:"/meet/updateRemark/",
                        dataType:"json",
                        data:{"rid":rid,"txt":remark},
                        success:function(data){

                        },error:function(err){
                            console.log(err.responseText);
                        }
                    });
                }, true);
            },error:function(err){
                console.log(err.responseText);
            }
        });

    });

    //手动邀请参会者
    $("#add_invite").click(function () {
        var name = $("#name").val();
        var phone = $("#phone").val();
        var flag = true;
        if (name != "" && name.indexOf("（") != -1 || name.indexOf("）") != -1) {
            Alert("名称不能包含'（'或'）'字符");
            flag = false;
        }
        if (flag) {
            if (phone.trim() == "") {
                Alert("电话号码不能为空！");
                flag = false;
            } else  {
                //如果号码中包含"-"，且短横前的位数小于4位，则去掉"-"
                var idx=phone.indexOf("-");
                if(idx!=-1){
                    var prefix=phone.substring(0,idx);
                    if(prefix<=4){
                        phone = phone.replace("-","");
                    }
                }
                if (!common.phoneRegex.test(phone) && !common.telRegex.test(phone)){
                    Alert("电话号码格式不正确！");
                    flag=false;
                }
            }
        }

        if (flag) {
            if(!name){
                name=meetService.getName(phone);
            }
            meetService.addHost(name,phone);
            flag = meetService.addInvite({name:name, phone:phone,isHand:true});
            var meetId=$("#meetId").val();
            if(meetId&&flag){//呼叫
                var callers=[{name:name,caller:phone}];
                //webSocketUtil.joinMeeting(meetId, callers);
                meetService.call(meetId,callers);
            }
            //将该临时联系人添加个人通讯录中
            meetService.addPersonalContact({
                "c.name":name,
                "c.phone":phone
            });
            if(flag){
                $("#name").val("");
                $("#phone").val("");
                meetService.changeContactAdded(phone);
            }else{
                Alert("该号码已存在！");
            }
        }
    });

    //呼叫号码
    $("body").on("click","a.call",function(){
        var meetId=$("#meetId").val();
        var $tr=$(this).closest("tr");
        var name=$tr.find("td:eq(0)").text().trim();
        var phone=$tr.find("td:eq(1)").text().trim();
        var cls=$(this).attr("class");
        if(meetId&&cls.indexOf("disabled")==-1){//呼叫
            //$(this).addClass("disabled");
            var callers=[{name:name,caller:phone}];
            //webSocketUtil.joinMeeting(meetId, callers);
            meetService.call(meetId,callers);
        }
    });

    //挂断
    $("body").on("click","a.call-end",function(){
        var meetId=$("#meetId").val();
        var $tr=$(this).closest("tr");
        var name=$tr.find("td:eq(0)").text().trim();
        var phone=$tr.find("td:eq(1)").text().trim();
        if(meetId){//呼叫
            var callers=[{name:name,caller:phone}];
            webSocketUtil.leaveMeeting(meetId, callers);
        }
    });

    //向上
    $("body").on("click","a.push-up",function(){
        var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
        meetService.pushUp(this,phone);
    });

    //向下
    $("body").on("click","a.push-down",function(){
        var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
        meetService.pushDown(this,phone);
    });

    //选中联系人
    $("#contacts_content").on("click", "div.collapse table tr",function () {
        $("#contacts_content div.collapse table tr").removeClass("success");
        $(this).addClass("success");
    });

    //禁言
    $("#attendee_list").on("click","a.mic",function(){
        var meetId=$("#meetId").val();
        var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
        var name=$(this).closest("tr").find("td:first").text().trim();
        var callers=[{name:name,caller:phone}];
        webSocketUtil.forbidTalk(meetId, callers);
    });

    //允许发言
    $("#attendee_list").on("click","a.mic-off",function(){
        var meetId=$("#meetId").val();
        var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
        var name=$(this).closest("tr").find("td:first").text().trim();
        var callers=[{name:name,caller:phone}];
        webSocketUtil.allowTalk(meetId, callers);
    });

    //======================================
    //通讯录联系人搜索
    $("#search_contacts").keyup(function () {
        var txt = this.value;
        setTimeout('meetService.searchContacts("'+txt+'")', 500);
    });
    $("#search_contacts").blur(function () {
        if (!local.isHover) {
            $("#contactsResult").hide();
        }
    });
    $("#contactsResult").click(function () {
        $(this).hide();
    });
    $("#contactsResult").hover(function () { local.isHover = true; }, function () { local.isHover = false; });
    //选中获取的搜索联系人
    $("#contactsResult").on("click","li", function () {
        var str = $(this).text();
        var arr = str.split("--");
        var phone = arr[0];
        phone = phone.substring(phone.indexOf("(") + 1, phone.indexOf(")"));
        var type = arr[1];
        type = type.substring(0, type.indexOf("("));
        if (type == "公共通讯录") {
            type = 0;
        } else {
            type = 1;
        }
        var group = arr[1];
        group = group.substring(group.indexOf("(") + 1, group.indexOf(")"));
        //alert(phone+"/"+type+"/"+group);
        meetService.getContactResult(phone,type,group);
    });
    //==========================

    //选中邀请人
    $("#invite>table>tbody").on("click","tr",function(){
        $("#invite>table>tbody>tr").removeClass("success");
        $(this).addClass("success");
    });

    //===========================
    //邀请人搜索
    $("#search_invite").keyup(function(){
        var txt=this.value;
        setTimeout("meetService.searchInvite('"+txt+"')", 500);
    });
    $("#search_invite").blur(function () {
        if (!local.isHover1) {
            $("#inviteResult").hide();
        }
    });
    $("#inviteResult").click(function () {
        $(this).hide();
    });
    $("#inviteResult").hover(function () { local.isHover1 = true; }, function () { local.isHover1 = false; });
    //选中搜索结果
    $("#inviteResult").on("click","li",function(){
        var phone=$(this).text();
        phone=phone.substring(phone.indexOf("(")+1,phone.indexOf(")"));
        meetService.getInviteResult(phone);
    });
    //============================================

    //选中参会者
    $("#attendee_list table>tbody>tr").on("click","tr",function(){
        $("#attendee_list>table>tbody>tr").removeClass("success");
        $(this).addClass("success");
    });

    //==========会议中参会人搜索=====================
    $("#searchInMeet").keyup(function(){
        var txt=this.value;
        setTimeout("meetService.searchInMeet('"+txt+"')", 500);
    });
    $("#searchInMeet").blur(function () {
        if (!local.isHover2) {
            $("#inmeetResult").hide();
        }
    });
    $("#inmeetResult").click(function () {
        $(this).hide();
    });
    $("#inmeetResult").hover(function () { local.isHover2 = true; }, function () { local.isHover2 = false; });
    //选中搜索结果
    $("#inmeetResult").on("click","li",function(){
        var phone=$(this).text();
        phone=phone.substring(phone.indexOf("(")+1,phone.indexOf(")"));
        meetService.getInMeetResult(phone);
    });
    //=================END===================

    //移除参会者
    $("#invite").on("click","a.clear",function(){
        var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
        meetService.removeInvite(phone,true);
    });

    //开始会议
    $("#startMeetBtn").click(function(){
        var hostNum=$("#mhost").text();
        hostNum=hostNum.substring(hostNum.indexOf("（")+1,hostNum.indexOf("）"));
        var rid=$("#rid").val();
        if($(this).text()=="开始会议"){
            var isRecord =$("#isRecord").bootstrapSwitch("state");
            var subject=$("#msubject").text();
            var hostName=meetService.getHostName();
            var meetPwd=$("#mpwd").text();
            var notice=$("#noticeFailed").bootstrapSwitch("state");
            var allowBegin=$("#allowBegin").bootstrapSwitch("state");
            meetService.createMeet({"hostNum":hostNum,"hostName":hostName,"subject":subject,"rid":rid,"isRecord":isRecord,"notice":notice,"allowBegin":allowBegin});
            $("#startMeetBtn").attr("disabled","disabled");
            local.isFirst=true;//表示该会议第一次呼叫
        }else{//结束会议
            showModal("提示","<p style='text-align: center;'>是否结束当前会议？</p>",function () {
                $("#loading").show();
                var meetId=$("#meetId").val();
                webSocketUtil.stopMeeting(meetId);
            });
        }

    });

    //全体静音或允许发言
    $("#isAllowSpeak").on("switchChange.bootstrapSwitch",function(event,state){
        if(state){
            meetService.allSlience();
        }else{
            meetService.allSpeak();
        }
    });
    
    //全部静音
    $("#muteAll").click(function () {
        meetService.allSlience();
    });

    //背景音乐
    $("#playback").on("switchChange.bootstrapSwitch",function(event,state){
        var meetId=$("#meetId").val();
        if(state){
            webSocketUtil.startPlayback(meetId);
        }else{
            webSocketUtil.stopPlayback(meetId);
        }
    });

    //会议锁门
    $("#lockMeet").on("switchChange.bootstrapSwitch",function(event,state){
        var meetId=$("#meetId").val();
        webSocketUtil.lock(meetId,state);
    });

    //开始、结束录音
    $("#btn_record").click(function () {
        //开始录音
        var meetId=$("#meetId").val();
        var flag=true;
        if($(this).text()=="开始录音"){
            flag=true;
        }else{//结束录音
            flag=false;
        }
        meetService.startOrFinishRecord(flag,meetId);
    });

    //呼叫全部
    $("#btn_callAll").click(function () {
        meetService.callAllInvite();
        $(this).attr("disabled","disabled");
    });
    
    //发送短信通知
    $("body").on("click","a.msg",function () {
        if(!$(this).hasClass("disabled")){
            var rid=$("#rid").val();
            var name=$(this).closest("tr").find("td:first").text().trim();
            var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
            meetService.sendMsg(rid,phone,name);
            $(this).addClass("disabled");
            $(this).attr("disabled","disabled");
            var that=this;
            setTimeout(function () {
                $(that).removeClass("disabled");
                $(that).removeAttr("disabled");
            },10000);
        }
    });

    //播放录音
    $("body").on("click","a.play",function () {
        meetService.playRec($(this).data("rec"));
    });
    //设置为第二主持人
    $("body").on("click","a.host",function () {
        var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
        var rid=$("#rid").val();
        var meetId=$("#meetId").val();
        var that=this;
        meetService.setHost({rid:rid,phone:phone,meetId:meetId,dom:that});
    });

});