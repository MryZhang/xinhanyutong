var contact={
    //获取上下文
    getContextPath:function () {
      return $("#contextPath").val();
    },
    //添加分组
    addGroup:function (fn) {
        common.showDialog({
            title:"新建联系人组",
            content:"联系人组名称：<input type=\"text\" id=\"gname\" class=\"text\"/>",
            ok:function () {
                var gname=$("#gname").val();
                if(gname){
                    $.post(contact.getContextPath()+"/personalcontacts/addGroup",{"g.name":gname},function (data) {
                        if(data.result){//添加成功
                            fn&&fn(data);
                        }else{
                            if(data.result===false){//添加失败
                                common.showDialog({
                                    content:data.msg||"操作失败"
                                });
                            }else{//登陆过期
                                location.href=contact.getContextPath+"/login";
                            }
                        }
                    });
                }else{
                    return false;
                }
            }
        });
    },
    //获取条件
    getCondition:function (obj) {
        var condition="";
        if(obj){
            for(var p in obj){
                if(condition){
                    condition+="&";
                }else{
                    condition="?";
                }
                condition+=p+"="+obj[p];
            }
        }
        return encodeURI(condition);
    },
    //获取分页数据
    getGroupPage:function (obj) {
        var condition=contact.getCondition(obj);
        location.href=contact.getContextPath()+"/personalcontacts/page"+condition;
    },
    getPageCondition:function () {
        var obj={};
        var search=$("#searchStr").val();
        if(search){
            obj.search=search;
        }
        var groupId=$("#currentGroup").val();
        if(groupId){
            obj.groupId=groupId;
        }
        var pageIndex=$("#currentPage").val();
        if(pageIndex){
            obj.pageIndex=pageIndex;
        }
        return contact.getCondition(obj);
    },
    //修改分组名称
    updateGroup:function (name,gid,dom) {
        common.showDialog({
            title:"修改联系人组",
            content:"联系人组名称：<input type=\"text\" id=\"gname\" class=\"text\" value=\""+name+"\"/><div class=\"error\" style=\"display:none;\"></div>",
            ok:function () {
                var gname=$("#gname").val();
                if(gname){
                    var flag=true;
                    //验证重复
                    var ls=$(".ul_list li.group");
                    for(var i=0;i<ls.length;i++){
                        var temp=$(ls[i]).text().trim();
                        if(temp==gname){
                            flag=false;
                            $("#gname").next().text("该组名已存在，请重新输入").show();
                            break;
                        }
                    }
                    if(flag){
                        $.post(contact.getContextPath()+"/personalcontacts/updateGroup",{"g.id":gid,"g.name":gname},function (data) {
                            alert(JSON.stringify(data));
                            if(data.result){//修改成功
                                $(dom).closest("li").find("span.lspan").text(gname);
                            }else{
                                if(data.result===false){
                                    common.showDialog({
                                        content:data.msg||"操作失败"
                                    });
                                }else{
                                    location.href=contact.getContextPath()+"/login";
                                }
                            }
                        });
                    }
                }else{
                    return false;
                }
            }
        });
    },
    //删除分组
    deleteGroup:function (id,name) {
        common.showDialog({
            title:"删除联系人组",
            content:"是否确定删除"+name+"?<br/>删除组会同时删除该组的联系人",
            ok:function () {
                location.href=contact.getContextPath()+"/personalcontacts/deleteGroup/"+id;
            }
        });
    },
    //选中所有
    checkAll:function () {
        var checked=$("#chkAll").get(0).checked;
        var chks=$("#contactsTable input[type=checkbox]");
        for(var i=0,len=chks.length;i<len;i++){
            chks[i].checked=checked;
        }
    },
    //删除联系人
    deleteContacts:function () {
        var chkeds=$("#contactsTable input[type=checkbox]:checked");
        if(chkeds.length>0){
            common.showDialog({
                content:"是否删除？",
                ok:function () {
                    var ids="";
                    for(var i=0,len=chkeds.length;i<len;i++){
                        ids+=$(chkeds[i]).val();
                        if(i!=len-1){
                            ids+="-";
                        }
                    }
                    var queryString=contact.getPageCondition();
                    if(queryString){
                        queryString+="&ids="+ids;
                    }else{
                        queryString+="?ids="+ids;
                    }
                    location.href=contact.getContextPath()+"/personalcontacts/deleteContacts"+queryString;
                }
            });

        }
    },
    //获取分页数据
    getPage:function (index) {
        var obj={pageIndex:index};
        var search=$("#searchStr").val();
        if(search){
            obj.search=search;
        }
        var groupId=$("#currentGroup").val();
        if(groupId){
            obj.groupId=groupId;
        }
        contact.getGroupPage(obj);
    }
};


var addContactPage={
    getContextPath:function () {
      return $("#contextPath").val();
    },
    getQueryString:function () {
      return $("#queryString").val();
    },
    //数据验证
    validate:function (dom) {
        var id=$(dom).attr("id");
        var value=$(dom).val();
        var flag=true;
        switch(id){
            case "c_userName":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("姓名不能为空").attr("class","addConTip error");
                    flag=false;
                }else if(value.length>10){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("姓名长度不超过10").attr("class","addConTip error");
                    flag=false;
                }else{
                    var pid=$("#pid").val();
                    if(pid){//修改
                        addContactPage.isExistName(value,function (data) {
                            if(data.result){
                                $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("联系人姓名重复").attr("class","addConTip error");
                            }else{
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly");
                            }
                        });
                    }else{//添加
                        $(dom).attr("class","text").next().attr("class","blank icon_orderly");
                    }

                }
                break;
            case "c_mobile":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("手机号码不能为空").attr("class","addConTip error");
                    flag=false;
                }else if(!common.phoneRegex.test(value)&&!common.telRegex.test(value)){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("手机或固话号码格式不正确，请重新填写").attr("class","addConTip error");
                    flag=false;
                }else{
                    var id=$("#cid").val();
                    if(id){//修改
                        addContactPage.isExistPhone({"phone":value,"id":id},function (data) {
                            if(data.result){
                                $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("电话号码重复").attr("class","addConTip error");
                            }else if(data.result===false){
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly");
                            }
                        })
                    }else{
                        addContactPage.isExistPhone({"phone":value},function (data) {
                            if(data.result){
                                $(dom).attr("class","text highlight2").next().next().text("电话号码重复").attr("class","addConTip error");
                            }else if(data.result===false){
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly");
                            }
                        })
                    }


                }
                break;
            case "c_email":
                if(value!=""&&!common.emailRegex.test(value)){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("邮箱格式不正确").attr("class","addConTip error");
                    flag=false;
                }else{
                    $(this).attr("class","text").next().attr("class","blank icon_orderly");
                }
                break;
        }
        return flag;
    },
    //验证电话号码是否重复
    isExistPhone:function (obj,fn) {
        $.post(addContactPage.getContextPath()+"/personalcontacts/isExist",obj,function (data) {
            if(data.result==undefined){
                location.href=addContactPage.getContextPath+"/personalcontacts/page"+addContactPage.getQueryString();
            }
            fn&&fn(data);
        });
    },
    //判断姓名是否重复
    isExistName:function (name,fn) {
        $.post(addContactPage.getContextPath()+"/personalcontacts/isExist",{"name":name,"pid":$("#pid").val()},function (data) {
            if(data.result==undefined){
                location.href=addContactPage.getContextPath+"/personalcontacts/page"+addContactPage.getQueryString();
            }
            fn&&fn(data);
        });
    }
};


//上传页面
var importPage={
    importContacts:function () {
        var value=$("#contactFile").val();
        if(value){
            var type=value.substr(value.lastIndexOf(".")+1);
            if(type!="xls"){
                $("#import-tip").fadeIn(800).fadeOut(1200);
                return false;
            }
            return true;
        }
        return false;
    }
};