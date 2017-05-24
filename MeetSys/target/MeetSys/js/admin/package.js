var package={
    positiveRegex:/^([1-9]\d*\.\d*|0\.\d+|[1-9]\d*|0)$/,
    //验证
    validate:function (dom) {
        var id=$(dom).attr("id");
        var value=$(dom).val().trim();
        var flag=true;
        switch (id){
            case "c_name":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("名称不能为空").attr("class","addConTip error");
                }else if(common.specilStringRegex.test(value)){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("名称包含非法字符").attr("class","addConTip error");
                }else{
                    package.exists(value,function () {
                       $("#c_name").attr("class","text highlight2").next().text("套餐名重复").attr("class","addConTip error");
                    });
                }
                break;
            case "c_callInMode":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("请选择呼入计费模式").attr("class","addConTip error");
                }
                break;
            case "c_callInRate":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("呼入计费费率不能为空").attr("class","addConTip error");
                }else if(!package.positiveRegex.test(value)){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("呼入计费费率格式不正确").attr("class","addConTip error");
                }
                break;
            case "c_passRate":
                if($(dom).is(":visible")){
                    if(value==""){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().text("超方费率不能为空").attr("class","addConTip error");
                    }else if(!package.positiveRegex.test(value)){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().text("超方费率格式不正确").attr("class","addConTip error");
                    }
                }
                break;
            case "c_callOutRate":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("呼出费率不能为空").attr("class","addConTip error");
                }else if(!package.positiveRegex.test(value)){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("呼出费率格式不正确").attr("class","addConTip error");
                }
                break;
            case "c_smsRate":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("短信费率不能为空").attr("class","addConTip error");
                }else if(!package.positiveRegex.test(value)){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("短信费率格式不正确").attr("class","addConTip error");
                }
                break;
        }
        return flag;
    },
    //判断套餐名是否存在
    exists:function (name,fn) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/admin/package/exist",
            dataType:"json",
            data:{name:name},
            success:function (data) {
                common.isLoginTimeout(data);
                if(data.result){
                    fn&&fn();
                }
            },error:function (err) {
                console.log(err.responseText);
            }
        });
    }
};