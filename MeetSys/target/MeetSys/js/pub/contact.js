var pub={
    getQueryString:function () {
        return common.getQueryString(pub.getPageCondition());
    },
    //获取翻页的条件
    getPageCondition:function () {
        var obj={};
        var pageIndex=$("#currentPage").val();
        if(pageIndex){
            obj.pageIndex=pageIndex;
        }
        var search=$("#searchStr").val();
        if(search){
            obj.search=search;
        }
        var departId=$("#departId").val();
        if(departId){
            obj.departId=departId;
        }
        return obj;
    },
    getPage:function (obj) {
        location.href=common.getContextPath()+"/publiccontacts"+common.getQueryString(obj);
    },
    //显示添加部门
    showAddDepart:function (fn) {
        depart.showAddDepart(fn);
    },
    //显示修改部门
    showUpdateDepart:function (name) {
        depart.showUpdateDepart(name);
    },
    //删除部门及相关信息
    deleteDepart:function (did) {
        depart.deleteDepart(did,"/publiccontacts");
    },
    //全部选中
    checkAll:function () {
        var chked=$("#chkAll").get(0).checked;
        var chkboxs=$("#contab").find("input[type=checkbox]");
        for(var i=0,size=chkboxs.length;i<size;i++){
            chkboxs[i].checked=chked;
        }
    },
    //批量删除
    deleteBatch:function () {
        var chkeds=$("#contab").find("input[type=checkbox]:checked");
        if(chkeds.length>0){
            common.showDialog({
                content:"是否删除选中的所有用户?",
                ok:function () {
                    var ids="";
                    for(var i=0,len=chkeds.length;i<len;i++){
                        ids+=$(chkeds[i]).val();
                        if(i!=len-1){
                            ids+=",";
                        }
                    }
                    location.href=common.getContextPath()+"/publiccontacts/delete/"+ids+pub.getQueryString();
                }
            });
        }
    }
}

//添加页面的相关操作
var addPage={
    getCheckUrl:function () {
        return common.getContextPath()+"/publiccontacts/isExist";
    },
    validate:function (dom) {
        //使用jquery中deferred对象解决ajax异步验证返回值的问题
        var def=$.Deferred();
        var id=$(dom).attr("id");
        var value=$(dom).val();
        var flag=true;
        
        var isExist=function (obj,fn) {
            $.ajax({
                method:"post",
                url:addPage.getCheckUrl(),
                data:obj,
                dataType:"json",
                success:function (data) {
                    if(data.result===undefined){//登陆过时
                        location.href=common.getContextPath()+"/login";
                    }
                    fn&&fn(data);
                }
            });
        }
        switch(id){
            case "c_userName":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("请添加用户名").attr("class","addConTip error");
                    flag=false;
                    def.resolve(flag);
                }else{
                    var contactId=$("#conId").val();
                    if(contactId){//修改操作
                        isExist({"id":contactId,"name":value},function (data) {
                            if(data.result==true){//验证重复
                                flag=false;
                                $(dom).attr("class","text highlight2").next().next().text("联系人姓名重复").attr("class","addConTip error");
                            }else if(data.result==false){//验证通过
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly").next().text("").attr("class","addConTip");
                            }
                            def.resolve(flag);
                        });
                    }else{//添加操作
                        $(dom).attr("class","text").next().attr("class","blank icon_orderly").next().text("").attr("class","addConTip");
                        def.resolve(flag);
                    }
                }
                break;
            case "c_mobile":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("手机号码不能为空").attr("class","addConTip error");
                    flag=false;
                    def.resolve(flag);
                }else if(!common.phoneRegex.test(value)&&!common.telRegex.test(value)){
                    $(dom).attr("class","text highlight2").next().next().text("手机或固话号码格式不正确，请重新填写").attr("class","addConTip error");
                    flag=false;
                    def.resolve(flag);
                }else{
                    var phoneId=$("#phoneId").val();
                    if(id){//修改操作
                        isExist({phone:value,id:phoneId},function (data) {
                            if(data.result==true){//验证重复
                                flag=false;
                                $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("联系人姓名重复").attr("class","addConTip error");
                            }else if(data.result==false){//验证通过
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly").next().text("").attr("class","addConTip");
                            }
                            def.resolve(flag);
                        });
                    }else{//添加操作
                        isExist({phone:value},function (data) {
                            if(data.result==true){//验证重复
                                flag=false;
                                $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("联系人姓名重复").attr("class","addConTip error");
                            }else if(data.result==false){//验证通过
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly").next().text("").attr("class","addConTip");
                            }
                            def.resolve(flag);
                        });
                    }

                }
                break;
            case "c_position":
                $(dom).attr("class","text");
                def.resolve(flag);
                break;
            case "c_department":
                if(value=="0"){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().next().text("请选择部门").attr("class","addConTip error");
                }else{
                    $(dom).attr("class","text").next().next().text("").attr("class","addConTip");
                }
                def.resolve(flag);
                break;
        }
        return def.promise();
    },
    validateAll:function () {
        var def=$.Deferred();
        var flag=true;
        $(".text").each(function () {
            var d=addPage.validate(this);
            if(flag){
                d.then(function (data) {
                    flag=data;
                    if(!flag){
                        def.resolve(flag);
                    }
                });
            }
        });
        def.resolve(flag);
        return def.promise();
    },
    //添加联系人
    addUser:function (addContinue) {
        if(!addContinue){
            url=common.getContextPath()+"/publiccontacts/add";
        }else{
            url=common.getContextPath()+"/publiccontacts/addAndContinue";
        }
        var myForm = document.forms[0];
        myForm.action=url;
        myForm.submit();
    },
    //修改联系人
    updateUser:function () {
        var myForm = document.forms[0];
        myForm.action=common.getContextPath()+"/publiccontacts/update";
        myForm.submit();
    }
}


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
