var employee={
    //获取上下文
    getContextPath:function () {
        return $("#contextPath").val();
    },
    //显示添加部门
    showAddDepart:function () {
        common.showDialog({
            title:"新建部门",
            content:"<div>部门名称：<input type=\"text\" id=\"dname\" class=\"text\" /></div><div class=\"error\" style=\"display:none;margin-left: 70px;\" ></div>",
            ok:function () {
                var flag=true;
                var dname=$("#dname").val();
                if(dname==""){
                    $("#dname").parent().next().text("部门名称不能为空！").show();
                    flag=false;
                }else{//判断部门是否重复添加
                    var $departs=$("#departSelection option");
                    for(var i=0,length=$departs.length;i<length;i++){
                        if(dname==$($departs[i]).text()){
                            flag=false;
                            $("#dname").parent().next().text("该部门已存在！").show();
                            break;
                        }
                    }
                }
                if(flag){
                    console.log(dname);
                    var data={"d.name":dname};
                    employee.addDepart(data);
                }
                return flag;
            }
        });
    },
    //添加部门
    addDepart:function (obj) {
        $.post(employee.getContextPath()+"/department/add",obj,function (data) {
            if(data.result){//添加成功
                var option="<option value=\""+data.data.id+"\">"+data.data.name+"</option>";
                $("#departSelection").append(option);
            }else{
                if(data.result===false){//
                    common.showDialog({
                        content:data.msg||"操作失败"
                    })
                }else{//登陆过期
                    location.href=employee.getContextPath+"/login";
                }

            }
        });
    },
    //验证信息
    validate:function (dom) {
        var id=$(dom).attr("id");
        var value=$(dom).val();
        var flag=true;
        switch (id){
            case "c_name":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("姓名不能为空").attr("class","addConTip error");
                }
                break;
            case "c_email":
                if(value!=""){
                    if(!common.emailRegex.test(value)){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("邮箱格式不正确").attr("class","addConTip error");
                    }
                }
                break;
            case "departSelection":
                if(value=="0"){
                    $(dom).attr("class","text highlight2").next().attr("class","blank").next().text("请选择所属部门").attr("class","addConTip error");
                }
                break;
        }
        return flag;
    }
};