var smtpPage={
    //验证
    validate:function(dom){
        var id=$(dom).attr("id");
        var value=$(dom).val();
        var flag=true;
        switch (id){
            case "c_email":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("邮箱不能为空").attr("class","addConTip error");
                }else if(!common.emailRegex.test(value)){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("邮箱格式不正确").attr("class","addConTip error");
                }
                break;
            case "c_password":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("密码不能为空").attr("class","addConTip error");
                    flag=false;
                }
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
                break;
        }
        if(flag){
            $(dom).attr("class","text").next().attr("class","blank icon_orderly");
        }
        return flag;
    },
    //保存
    save:function (data) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/smtp/save",
            dataType:"json",
            data:data,
            success:function (result) {
                common.isLoginTimeout(result);
                if(result.result){
                    common.showDialog({content:"操作成功!"});
                }else{
                    common.showDialog({content:result.msg||"操作失败！"})
                }
            },error:function (e) {
                console.log(e.responseText);
            }
        });
    }
}