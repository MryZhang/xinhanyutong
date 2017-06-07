/**
 * Created by MrQin on 2016/11/14.
 */
var order={
    CONTACTS:"contacts",//联系人数据的键值
    INVITOR:"invitor",//会议邀请人的键值
    tempData:null,//用来存储会议邀请人的零时数据
    getContextPath:function () {
      return $("#contextPath").val();
    },
    //从缓存中获取数据
    getValue:function(key){
        return $("body").data(key);
    },
    //将数据存入缓存
    setValue:function (key,value) {
        $("body").data(key,value);
    },
    //切换会议重复周期类型
    selectPeriod:function (dom) {
        $(".repeat").hide();
        var val=$(dom).val();
        switch(val){
            case "0":
                $("#startTime").show();
                $("#stime").hide();
                $("#timeRange").hide();
                $("tr.invitTr").show();
                $("#startTimeTr").show();
                // $("#meetPwd").hide();
                break;
            case "1":
                $("#startTime").hide();
                $("#stime").show();
                $("#everyday").show();
                $("#timeRange").show();
                $("tr.invitTr").show();
                $("#startTimeTr").show();
                // $("#meetPwd").hide();
                break;
            case "2":
                $("#startTime").hide();
                $("#stime").show();
                $("#everyWeek").show();
                $("#timeRange").show();
                $("tr.invitTr").show();
                $("#startTimeTr").show();
                // $("#meetPwd").hide();
                break;
            case "3":
                $("#startTime").hide();
                $("#stime").show();
                $("#everyMonth").show();
                $("#timeRange").show();
                $("tr.invitTr").show();
                $("#startTimeTr").show();
                // $("#meetPwd").hide();
                break;
            case "4":
                $("tr.invitTr").hide();
                $("#startTimeTr").hide();
                // $("#meetPwd").show();
                break;
        }
    },
    //选择主持人
    selectHost:function (host) {
        var setting={
            check:{
                enable:true,
                chkStyle:"radio",
                radioType:"all"
            },
            callback:{
                onClick:function (event, treeId, treeNode, clickFlag) {
                    if(treeNode.value){
                        var treeObj = $.fn.zTree.getZTreeObj("tree");
                        treeObj.checkNode(treeNode,!treeNode.checked,true,true);
                    }
                }
            }
        };
        var content="<div class='pstnhosttip'>请选择主持人</div>";
        content+="<div class='pstnhost'><ul id='tree' class='ztree'></ul></div>";
        var zTreeObj;
        common.showDialog({
            title:"设置会议主持人",
            content:content,
            ok:function () {
                zTreeObj=$.fn.zTree.getZTreeObj("tree");
                if(zTreeObj){
                    //获取选中节点
                    var selectedNode;
                    var nodes=zTreeObj.getCheckedNodes(true);
                    for(var i=0,length=nodes.length;i<length;i++){
                        if(nodes[i].value){
                            selectedNode=nodes[i];
                            break;
                        }
                    }
                    if(selectedNode){
                        $("#hostNum").val(selectedNode.value);
                        var name=selectedNode.name;
                        if(name.indexOf("(")!=-1){
                            name=name.substr(0,name.indexOf("("));
                        }
                        $("#hostName").val(name);
                    }
                }
            }
        });
        order.getContacts({
            fn:function (data) {
                zTreeObj=$.fn.zTree.init($("#tree"), setting, data);
                if(host){//默认选中
                    var node=zTreeObj.getNodeByParam("value",host);
                    zTreeObj.checkNode(node, true, true);
                }
            }
        })
    },
    //获取联系人数据
    getContacts:function (obj) {
        var data=order.getValue(order.CONTACTS);
        if(data){//如果缓存中存在数据直接获取
            if(obj.fn&&(typeof obj.fn==="function")){
                obj.fn(data);
            }
        }else{//如果缓存中没有数据，则从服务器中获取
            $.ajax({
                type:"post",
                url:order.getContextPath()+"/ordermeet/getContacts",
                dataType:"json",
                data:obj.data||{},
                success:function (data) {
                    order.setValue(order.CONTACTS,data);//将获取的数据存进缓存
                    if(obj.fn&&(typeof obj.fn==="function")){
                        obj.fn(data);
                    }
                }
            });
        }
    },
    //重新生成密码
    reproducePwd:function (dom) {
        $.post(order.getContextPath()+"/ordermeet/producePwd",{},function (data) {
            $(dom).val(data.result);
        });
    },
    //移除会议邀请人
    removeInvite:function (dom) {
        var $tr=$(dom).closest("tr");
        // var name=$tr.find("td:eq(0)").text().trim();
        var phone=$tr.find("td:eq(1)").text().trim();
        var inviteArr=order.getValue(order.INVITOR);
        if(inviteArr){
           for(var i=0,length=inviteArr.length;i<length;i++){
               if(inviteArr[i].phone==phone){
                   inviteArr.remove(inviteArr[i]);
                   order.setValue(order.INVITOR,inviteArr);
                    break;
               }
           }
            $tr.remove();
        }
    },
    //判断号码是否已在邀请人中
    isExistPhone:function (phone) {
        var inviteData=order.getValue(order.INVITOR);
        if(inviteData){
            for(var i=0,length=inviteData.length;i<length;i++){
                if(inviteData[i].phone==phone){
                    return true;
                }
            }
        }
        var host=$("#hostNum").val().trim();
        if(host==phone){
            return true;
        }
        return false;
    },
    //添加会议邀请人
    addInvite:function (obj) {
        if(!order.isExistPhone(obj.phone)){
            var content="<tr>";
            content+="<td>"+obj.name+"</td>";
            content+="<td>"+obj.phone+"</td>";
            content+="<td><a href='javascript:;' onclick='order.removeInvite(this)'><i class='icon-trash'></i></a></td>";
            content+="</tr>";
            $("#gbox_gridTable tbody").append(content);
            //添加到邀请人缓存数据中
            var invited=order.getValue(order.INVITOR)||[];
            invited.push(obj);
            order.setValue(order.INVITOR,invited);
            //清空
            $("#addInvName").val("");
            $("#addInvTel").val("");
        }
    },
    //获取邀请人树形菜单对象
    getTreeObj:function () {
        var iTreeObj;
        if(!iTreeObj){
            iTreeObj=$.fn.zTree.getZTreeObj("inviteeTree");
        }
        return iTreeObj;
    },
    //初始化邀请人树形菜单
    initInviteTree:function (data) {
        var setting={
            check:{
                enable:true,
                chkStyle:"checkbox",
                chkboxType:{"Y":"ps","N":"ps"}
            },
            callback:{
                onClick:function (event, treeId, treeNode, clickFlag) {
                    var iTreeObj=order.getTreeObj();
                    if(iTreeObj){
                        iTreeObj.checkNode(treeNode,!treeNode.checked,true,true);
                    }
                },
                onCheck:function (event, treeId, treeNode) {
                    if(treeNode.value){
                        // var obj={"name":treeNode.name,"phone":treeNode.value};
                        // if(treeNode.checked){//勾选
                        //     var hostNum=$("#hostNum").val().trim();
                        //     if(obj.phone==hostNum){//选择号码与支持人号码相同
                        //         common.showDialog({
                        //             content:"成员号码不能与主持人相同"
                        //         });
                        //         iTreeObj=getiTreeObj();
                        //         if(iTreeObj){
                        //             iTreeObj.checkNode(treeNode,false.checked,true,true);
                        //         }
                        //     }else{
                        //          onChecked(obj);
                        //     }
                        // }else{//取消勾选
                        //      onUnChecked(obj);
                        // }
                    }
                }
            }
        }
        var zTreeObj=$.fn.zTree.init($("#inviteeTree"), setting, data);
        var invited=order.getValue(order.INVITOR);
        if(invited){
            //选中已邀请的
            for(var i=0,length=invited.length;i<length;i++){
                var node=zTreeObj.getNodeByParam("value",invited[i].phone);
                if(node){
                    zTreeObj.checkNode(node, true, true,true);
                }
            }
        }
    },
    //搜索联系人
    searchContacts:function (dom) {
        var str=$(dom).prev().val().trim();
        order.getContacts({
            data:{
                "search":str
            },
            fn:function (data) {
                var tempData=$.extend(true,[],data);//数组深拷贝
                if(data){
                    for(var i=0,length=tempData.length;i<length;i++){
                        for(var n=0,size=tempData[i].children.length;n<size;n++){
                            for(var m=0,slen=tempData[i].children[n].children.length;m<slen;m++){
                                var name=tempData[i].children[n].children[m].name;
                                var phone=tempData[i].children[n].children[m].value;

                                if(name.indexOf(str)==-1&&phone.indexOf(str)==-1){
                                    tempData[i].children[n].children.remove(tempData[i].children[n].children[m]);
                                }
                            }
                        }
                    }
                    order.initInviteTree(tempData);
                }
            }
        });

    },
    //从通讯录中选择参会人
    selectAttendeeFromContacts:function () {
        order.tempData=[];
        var iTreeObj;
        // //勾选后
        // var onChecked=function (obj) {
        //     order.tempData.push(obj);
        //     var name=obj.name+"("+obj.phone+")";
        //     var item="<li><a title='"+name+"'>"+name+"<button class='btn_remove' onclick='order.removeInvite(this)' style='display:none;'>&nbsp;</button></a></li>";
        //     $("#selectedTree").append(item);
        // }
        // //取消勾选后
        // var onUnChecked=function (obj) {
        //     var items=$("#selectedTree li");
        //     for(var i=0,length=items.length;i<length;i++){
        //         var text=$(item[i]).find("a").text().trim();
        //         //var name=text.substring(0,text.indexOf("("));
        //         var phone=text.substring(text.indexOf("(")+1,text.indexOf(")"));
        //         if(phone==obj.phone){
        //             order.tempData.remove(obj);
        //             $(item[i]).remove();
        //             break;
        //         }
        //     }
        // }

        var content="<div>搜索：<input type='text' id='searchInv' />&nbsp;&nbsp;<button type='button' onclick='order.searchContacts(this)' class='btn_style' id='btnSearch'>搜索</button></div>";
        content+="<hr style='margin:3px 0;clear:both;height:1px;border:none;border-top:1px dashed #0066CC;'>";
        // content+="<div class='srcInvitee'>可选邀请对象</div>";
        // content+="<div class='selInvitee'>已选邀请对象</div>";
        // content+="<div style='clear:both;'></div>";
        content+="<div class='inviteeTree'><ul id='inviteeTree' class='ztree'></ul>"
        content+="<div id='leftloading' class='hide'><img border='0' id='lloading' src='/img/loading.gif'/>";
        content+="<span id='lloadtip'>正在努力加载数据...</span></div></div>";
        // content+="<div class='inviteeBtn'><button type='button' id='clearTreeNode' class='btn_style'><全部删除</button></div>";
        // content+="<div class='selectedTree'><ul id='selectedTree' class='ztree'></ul></div>";
        common.showDialog({
            title:"从通信录中选择参会者",
            content:content,
            ok:function () {
                iTreeObj=order.getTreeObj();
                if(iTreeObj){
                    var selected=iTreeObj.getCheckedNodes(true);
                    for(var i=0,length=selected.length;i<length;i++){
                        if(selected[i].value){
                            var name=selected[i].name;
                            var index=name.indexOf("(");
                            if(index!=-1){
                                name=name.substr(0,index);
                            }
                            order.addInvite({name:name,phone:selected[i].value});
                        }
                    }
                }
            }
        });
        //加载显示树节点
        // $("#lloadtip").show();
        order.getContacts({
            fn:function (data) {
                order.initInviteTree(data);
                // $("#lloadtip").hide();
            }
        });
        
    },
    //手动添加邀请对象
    addTmpInv:{
        //显示提示信息
        showTips:function (obj) {
            dialog({
                align:obj.align||"top",
                content:obj.content,
                quickClose:true,
                width:obj.width||150,
                padding:obj.padding||6
            }).show(obj.target);
        },
        add:function () {
            var flag=true;
            var content,num=$("#addInvTel").val().trim();
            var name,phone;
            //验证号码
            if(num=="手机/区号固话号码-分机号"||num==""){
                $("#addInvTel").val("");
                content="<div style='color:#FF0000;font-size:12px;'>请填写电话号码</div>";
                flag=false;
            }else if(!common.phoneRegex.test(num)&&!common.telRegex.test(num)){
                flag=false;
                content="<div style='color:#FF0000;font-size:12px;'>电话号码格式错误</div>";
            }else if(num==$("#hostNum").val().trim()){
                flag=false;
                content="<div style='color:#FF0000;font-size:12px;'>成员号码不能与主持人相同</div>";
            }else{
                if($("#addInvName").val().trim()==""){
                    name=$("#addInvTel").val();
                }else{
                    name=$("#addInvName").val();
                }
                phone=$("#addInvTel").val();
            }
            if(!flag){//验证失败
                $("#addInvTel").addClass("highlight");
                order.addTmpInv.showTips({
                    content:content,
                    target:document.getElementById("addInvTel")
                });
            }
            if(flag){//验证通过
                if(order.isExistPhone(phone)){
                    common.showDialog({
                        content:'邀请对象【'+phone+'】已存在'
                    });
                }else{
                    order.addInvite({name:name,phone:phone});
                }
            }
        }
    },
    //添加预约会议
    addOrderMeet:{
        numRegex:/^[1-9]\d*$/,
        validate:function () {
            var flag=true;
            var subject=$("#subject").val().trim();
            var hostNum=$("#hostNum").val().trim();
            var obj={};

            if(subject==""){
                $("#subject").addClass("highlight").next().text("主题不能为空").removeClass("hide");
                flag=false;
            }else{
                obj["order.subject"]=subject;
            }

            if(hostNum==""){
                $("#hostNum").addClass("highlight");
                $("#hosttip").attr("class","pstnhnumtip error wrong").text("还未输入电话号码");
                flag=false;
            }else if(!common.phoneRegex.test(hostNum)&&!common.telRegex.test(hostNum)){
                $("#hostNum").addClass("highlight");
                $("#hosttip").attr("class","pstnhnumtip error wrong").text("手机号码格式不正确");
                flag=false;
            }else{
                obj["order.hostNum"]=hostNum;
            }

            //检查预约会议重复周期类型
            var cycleType=$("input.input_radio_check:checked").val();
            obj["order.period"]=cycleType;
            var chked;
            var setContacts=function () {
                var contacts=order.getValue(order.INVITOR);
                if(contacts){
                    obj["order.contacts"]=JSON.stringify(contacts);
                }
            }
            //主持人密码
            var hostPwd=$("#hostPwd").val();
            obj["order.hostPwd"]=hostPwd;
            switch(cycleType){
                case "0":
                    obj["order.startTime"]=$("#startTime").val();
                    setContacts();
                    break;
                case "1"://验证天的周期
                    chked=$("input[name='order.dayChoice']:checked").val();
                    obj["order.startTime"]=$("#stime").val();
                    
                    if(chked=="interval"){//重复周期为间隔多少天
                        var val=$("#dayInterval").val();
                        var testDay=function (num) {
                            if(common.isLeapYear(new Date())){//闰年
                                return parseInt(num)>366;
                            }else{
                                return parseInt(num)>365;
                            }
                        }
                        if(!order.addOrderMeet.numRegex.test(val)||testDay(val)){
                            $("#dayInterval").next().attr("class","pstnhnumtip error wrong").text("格式不正确");
                            flag=false;
                        }else{
                            obj["order.interval"]=val;
                        }
                    }else{//重复周期为工作日
                        obj["order.interval"]="workday";
                    }
                    setContacts();
                    break;
                case "2"://验证星期的周期
                    var val=$("#weekInterval").val();
                    obj["order.startTime"]=$("#stime").val();
                    if(!order.addOrderMeet.numRegex.test(val)||parseInt(val)>52){
                        $("#weekInterval").next().attr("class","pstnhnumtip error wrong").text("格式不正确");
                        flag=false;
                    }else{
                        var wkdays=$("input[name='order.weekdays']:checked");
                        if(wkdays&&wkdays.length>0){
                            var wkds="";
                            for(var i=0,len=wkdays.length;i<len;i++){
                                wkds+=wkdays[i].value+",";
                            }
                            wkds=wkds.substr(0,wkds.length-1);
                            obj["order.orderNum"]=wkds;
                            obj["order.interval"]=val;
                        }else{
                            $("#weekInterval").next().attr("class","pstnhnumtip error wrong").text("请选择具体的日期");
                            flag=false;
                        }
                    }
                    setContacts();
                    break;
                case "3"://验证月的周期
                    chked=$("input[name='order.monthChoice']:checked").val();
                    obj["order.startTime"]=$("#stime").val();
                    if(chked=="dayOfInterval"){//重复周期为间隔多少天
                        var val=$("#monthInterval").val();
                        if(!order.addOrderMeet.numRegex.test(val)||parseInt(val)>31){
                            $("#monthInterval").next().attr("class","pstnhnumtip error wrong").text("格式不正确");
                            flag=false;
                        }else{
                            obj["order.interval"]=val;
                        }
                    }else{//重复周期为每个月的第几个星期几
                        var weekNum=$("#weekNum").val();
                        var weekday=$("#weekday").val();
                        obj["order.orderNum"]=weekNum;
                        obj["order.weekday"]=weekday;
                    }
                    setContacts();
                    break;
                case "4":
                    var listenerPwd=$("#listenerPwd").val();
                    obj["order.listenerPwd"]=listenerPwd;
                    break;
            }
            if(flag){
                var invited=order.getValue(order.INVITOR);
                if(invited&&invited.length>0&&obj.type==4){
                    obj.contacts=JSON.stringify(invited);
                }
                var hostName=$("#hostName").val();
                if(hostName){
                    obj["order.hostName"]=hostName;
                }else{
                    obj["order.hostName"]=hostNum;
                }
                obj["order.isRecord"]=$("input[name='order.isRecord']:checked").val();
                obj["order.smsRemind"]=$("#smsRemind").get(0).checked;
                if(obj["order.smsRemind"]===true){
                    obj["order.smsRemindTime"]=$("#smsRemindTime").val();
                }
                obj["order.smsNotice"]=$("#smsNotice").get(0).checked;
                obj["order.containHost"]=$("#containHost").get(0).checked;
                var $isCallInitDom=$("#isCallInitiative");
                obj["order.callInitiative"]=$isCallInitDom.get(0)?$isCallInitDom.get(0).checked:true;
                return {result:true,data:obj};
            }
            return {result:false};
        },
        //添加预约会议操作
        add:function () {
            $("span.wrong").attr("class","wrong hide");
            var result=order.addOrderMeet.validate();
            if(result.result){//添加成功
                //判断是否添加会议邀请人
                if(!result.data["order.contacts"]){
                    common.showDialog({content:"没有添加会议邀请人，是否添加？",cancel:function () {
                        add(result.data);
                    }});
                }else{
                    add(result.data);
                }

                function add(dataObj) {
                    $("#loading").show();
                    $.ajax({
                        type:"post",
                        url:common.getContextPath()+"/ordermeet/create",
                        dataType:"json",
                        data:dataObj,
                        success:function (data) {
                            if(data.result){//添加成功
                                order.toast("添加成功","div.block_h1_content");
                            }else{
                                if(data.result===false){//添加失败
                                    order.toast(data.msg||"操作失败","div.block_h1_content");
                                }else{//登陆过期
                                    location.href=order.getContextPath()+"/login";
                                }
                            }
                        },complete:function () {
                            $("#loading").hide();
                        }
                    });
                }
            }
        }
    },
    /**
     * 吐司消息
     * @param msg 消息内容
     */
    toast:function () {
        var msg=arguments[0];
        var tag=arguments[1]||"body";
        var $toast=$(".my-toast");
        if($toast&&$toast.get(0)){
            $toast.find("div.my-toast-content").text(msg);
        }else{
            var html="<div class='my-toast'><div class='my-toast-title'>提示</div><div class='my-toast-content'>"+msg+"</div></div>";
            $("body").append(html);
        }
        $(".my-toast").fadeIn(100);
        setTimeout(function(){$(".my-toast").fadeOut(100)},1500);
    }
};