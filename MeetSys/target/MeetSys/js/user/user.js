/**
 * Created by MrQin on 2016/11/23.
 */
var userPage={
    //获取上下文路径
    getContextPath:function () {
        return $("#contextPath").val();
    },
    getQueryString:function (obj) {
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
        location.href=userPage.getContextPath()+"/user"+userPage.getQueryString(obj);
    },
    //部门相关操作
    depart:{
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
            depart.deleteDepart(did,"/user");
        }
    },
    //全部选中
    checkAll:function () {
        var chked=$("#chkAll").get(0).checked;
        var chkboxs=$("#userTable").find("input[type=checkbox]");
        for(var i=0,size=chkboxs.length;i<size;i++){
            chkboxs[i].checked=chked;
        }
    },
    //删除用户
    deleteUser:function () {
        var chkeds=$("#userTable").find("input[type=checkbox]:checked");
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
                    location.href=userPage.getContextPath()+"/user/delete/"+ids+userPage.getQueryString(userPage.getPageCondition());
                }
            });
        }
    }
}

var userAdd={
    getContextPath:function () {
        return $("#contextPath").val();
    },
    //验证
    validate:function (dom) {
        //使用jquery中deferred对象解决ajax异步验证返回值的问题
        var def=$.Deferred();
        var id=$(dom).attr("id");
        var value=$(dom).val();
        var flag=true;
        switch(id){
            case "c_userName":
                if(!$(dom).attr("readonly")){//当用户名为可读写时才验证
                    if(value==""){
                        $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("用户名不能为空").attr("class","addConTip error");
                        flag=false;
                        def.resolve(flag);
                    }else if(!common.phoneRegex.test(value)){
                        $(dom).attr("class","text highlight2").next().next().text("手机号码格式输入不正确").attr("class","addConTip error");
                        flag=false;
                        def.resolve(flag);
                    }else{
                        $.post(userAdd.getContextPath()+"/user/isExist",{username:value},function (data) {
                            if(data.result==undefined){//登陆过期
                                location.href=userAdd.getContextPath()+"login";
                            }
                            if(data.result){//号码重复
                                $(dom).attr("class","text highlight2").next().next().text("该号码已被注册").attr("class","addConTip error");
                                flag=false;
                            }else{
                                $(dom).attr("class","text").next().attr("class","blank icon_orderly");
                            }
                            def.resolve(flag);
                        });
                    }
                }
                break;
            case "c_password":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("密码不能为空").attr("class","addConTip error");
                    flag=false;
                }else if(value.length<6||value.length>16){
                    $(dom).attr("class","text highlight2").next().next().text("密码长度只能在6-16字符之间").attr("class","addConTip error");
                    flag=false;
                }
                def.resolve(flag);
                break;
            case "c_chkpwd":
                var pwd=$("#c_password").val();
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("请填写确认密码").attr("class","addConTip error");
                    flag=false;
                }else if(pwd!=value){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("两次输入的密码不一致,请核实后再输!").attr("class","addConTip error");
                    flag=false;
                }
                def.resolve(flag);
                break;
            case "c_name":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("姓名不能为空").attr("class","addConTip error");
                }
                def.resolve(flag);
                break;
            case "c_email":
                if(value!=""){
                    if(!common.emailRegex.test(value)){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("邮箱格式不正确").attr("class","addConTip error");
                    }
                }
                def.resolve(flag);
                break;
            case "departSelection":
                if(value=="0"){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().next().text("请选择部门").attr("class","addConTip error");
                }
                def.resolve(flag);
                break;
        }
        if(flag&&id!="c_userName"){
            if($(dom).val()!=""){
                $(dom).attr("class","text").next().attr("class","blank icon_orderly");
            }else{
                $(dom).attr("class","text");
            }
        }
        return def.promise();
    },
    //验证所有数据
    validateAll:function () {
        var def=$.Deferred();
        var flag=true;
        $(".text").each(function () {
            var d=userAdd.validate(this);
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
    //添加用户
    addUser:function (addContinue) {
        if(!addContinue){
            url=userAdd.getContextPath()+"/user/add";
        }else{
            url=userAdd.getContextPath()+"/user/addAndContinue";
        }
        var myForm = document.forms[0];
        myForm.action=url;
        myForm.submit();
    },
    //修改用户
    updateUser:function () {
        var myForm = document.forms[0];
        myForm.action=userAdd.getContextPath()+"/user/update";
        myForm.submit();
    }
};

var updateSelf={
    //修改
    update:function (data) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/user/updateSelf",
            dataType:"json",
            data:data,
            success:function (result) {
                common.isLoginTimeout(result);
                if(result.result){
                    common.showDialog({content:"修改成功!"});
                }else{
                    common.showDialog({content:result.msg||"修改失败！"})
                }
            },error:function (e) {
                console.log(e.responseText);
            }
        });
    }
}

//密码修改页面
var changePwdPage={
    showTips:function (dom) {
        $(dom).attr("class","text highlight1").nextAll("div").attr("class","addConTip").text("密码长度不能少于6位");
    },
    validate:function (dom) {
        var name=$(dom).attr("name");
        var value=$(dom).val();
        var flag=true;
        switch(name){
            case "oldpassword":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().attr("class","error").text("请输入旧密码");
                }else{
                    $(dom).attr("class","text").next().attr("class","addConTip").text("");
                }
                break;
            case "password":
                if(value==""||value.length<6||value.length>16){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().next().attr("class","error").text("密码长度只能在6-16字符之间");
                }else{
                    $(dom).attr("class","text").next().next().attr("class","addConTip").text("");
                }
                break;
            case "chkPwd":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().next().attr("class","error").text("请再次输入密码");
                }else{
                    var password=$("#password").val();
                    if(value!=password){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().next().attr("class","error").text("两次输入的密码不一致,请核实后再输!");
                    }else{
                        $(dom).attr("class","text").next().next().attr("class","addConTip").text("");
                    }
                }
                break;
        }
        return flag;
    },
    //修改密码
    changePwd:function (obj,fn) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/user/changePwd",
            data:obj,
            dataType:"json",
            success:function (data) {
                if(data.result){
                    common.showDialog({
                        content:"修改成功，请记牢修改后的密码",
                        ok:function () {
                            fn&&fn();
                        }
                    });
                }else{
                    if(data.result===false){
                        common.showDialog({
                            content:data.msg||"操作失败"
                        });
                    }else{//登陆过期
                        location.href=common.getContextPath()+"/login";
                    }
                }
            }
        });
    }
};