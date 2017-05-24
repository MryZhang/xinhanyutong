var changepwd={
    getContextPath:function () {
        return $("#contextPath").val();
    },
    //验证
    validate:function (dom) {
        var name=$(dom).attr("id");
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
                        $(dom).attr("class","text highlight2").next().next().attr("class","error").text("两次输入的密码不一致,请核实后再输");
                    }else{
                        $(dom).attr("class","text").next().next().attr("class","addConTip").text("");
                    }

                }
                break;
        }
        return flag;
    }
};