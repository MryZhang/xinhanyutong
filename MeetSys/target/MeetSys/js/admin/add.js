var add={
    getContextPath:function () {
      return $("#contextPath").val();
    },
    validate:function (dom) {
        var id=$(dom).attr("id");
        var value=$(dom).val();
        var flag=true;
        switch (id){
            case "c_userName":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("用户名不能为空").attr("class","addConTip error");
                }else if(!common.phoneRegex.test(value)){
                    $(dom).attr("class","text highlight2").next().next().text("手机号码格式输入不正确").attr("class","addConTip error");
                    flag=false;
                }else{
                    this.isExistUser({
                        phone:value,
                        success:function () {//验证通过
                            $(dom).attr("class","text").next().attr("class","blank icon_orderly").next().text("").attr("class","addConTip");
                        },
                        fail:function () {//验证未通过
                            $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("该用户已存在").attr("class","addConTip error");
                        }
                    });
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
                break;
            case "c_chkpwd":
                if(value==""){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("请填写确认密码").attr("class","addConTip error");
                    flag=false;
                }else{
                    var pwd=$("#c_password").val();
                    if(pwd!=value){
                        $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("两次输入的密码不一致,请核实后再输").attr("class","addConTip error");
                        flag=false;
                    }
                }
                break;
            case "c_companySelect":
                if(!parseInt(value)){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("请填选择所属公司").attr("class","addConTip error");
                    flag=false;
                }
                break;
        }
        return flag;
    },
    //判断用户名是否重复
    isExistUser:function (obj) {
        $.post(this.getContextPath()+"/admin/isExist",{"username":obj.phone},function (data) {
            if(data.result){//用户名重复
                obj.fail&&obj.fail();
            }else{
                obj.success&&obj.success();
            }
        });
    },
    //显示模态窗口
    showDialog:function (obj) {
        dialog({
            title:obj.title||"提示",
            content:obj.content||"",
            okValue:"确定",
            ok:function () {
                if(obj.ok&&(typeof obj.ok)=="function"){
                    return obj.ok();
                }
            },
            cancelValue:"取消",
            cancel:function () {
                
            }
        }).show();
    },
    //添加公司
    addCompany:function () {
        this.showDialog({
            title:"创建公司",
            content:function () {
                var content="<div>公司名称：<input type=\"text\" id=\"cname\" class=\"text\" /></div><div class=\"error\" style=\"display:none;margin-left: 70px;\" ></div>";
                content+="<div style=\"margin-top:10px;\">呼入号码：<input type=\"text\" id=\"code\" class=\"text\"/></div><div class=\"error\" style=\"display:none;margin-left: 70px;\"></div>";
                content+="<div style=\"margin-top:10px;\">呼出号码：<input type=\"text\" id=\"showNum\" class=\"text\"/></div><div class=\"error\" style=\"display:none;margin-left: 70px;\"></div>";
                return content;
            },
            ok:function () {
                var cname=$("#cname").val().trim();
                var code=$("#code").val().trim();
                var showNum=$("#showNum").val().trim();
                var flag=true;
                if(cname==""){
                    $("#cname").parent().next().text("公司名称不能为空！").show();
                    flag=false;
                }
                if(code==""){
                    $("#code").parent().next().text("呼叫号码不能为空！").show();
                    flag=false;
                }else{
                    if(!common.telRegex.test(code)&&!common.phoneRegex.test(code)){
                        $("#code").parent().next().text("呼叫号码必须为手机或座机号码！").show();
                        flag=false;
                    }

                }
                if(showNum==""){
                    $("#showNum").parent().next().text("呼出号码不能为空！").show();
                    flag=false;
                }else{
                    if(!common.telRegex.test(showNum)&&!common.phoneRegex.test(showNum)){
                        $("#showNum").parent().next().text("呼出号码必须为手机或座机号码！").show();
                        flag=false;
                    }
                }

                if(flag){//验证通过
                    $.post(add.getContextPath()+"/company/add",{"company.name":cname,"company.code":code,"company.showNum":showNum},function (data) {
                        if(data.result){//添加成功
                            var option="<option value=\""+data.data.id+"\">"+data.data.name+"</option>";
                            $("#c_companySelect").append(option);
                        }else{//添加失败
                            common.isLoginTimeout(data);
                            add.showDialog({"content":data.msg||"操作失败！"});
                        }
                    });

                }
                return flag;
            }


        });
        $("#cname,#code,#showNum").on("focus",function () {
            $(this).parent().next().text("");
        });
    }
};