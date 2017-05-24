var admin={
    //获取上下文
    getContextPath:function () {
        return $("#contextPath").val()||"";
    },
    getPageUrl:function () {
        return this.getContextPath()+"/admin/page";
    },
    //首页
    page:function (obj) {
        var where="?currentPage="+obj["pageNum"];
        if(obj["companyId"]){
            where+="&cid="+obj["companyId"];
        }
        if(obj["username"]){
            where+="&username="+obj["username"];
        }
        location.href=this.getPageUrl()+where;
    },
    getQueryStr:function () {
        var currentPage=$("#currentIndex").val();
        var companyId=$("#companyId").val();
        var username=$("#username").val();
        var where="";
        if(currentPage){
            if(where){
                where+="&";
            }else{
                where="?";
            }
            where+="currentPage="+currentPage;
        }
        if(companyId){
            if(where){
                where+="&";
            }else{
                where="?";
            }
            where+="a.cid="+companyId;
        }
        if(username){
            if(where){
                where+="&";
            }else{
                where="?";
            }
            where+="username="+username;
        }
        return where;
    },
    //显示加载状态
    loading:function () {
      $("#loading").show();
    },
    //隐藏加载状态
    loaded:function () {
        $("#loading").hide();
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
    //获取公司信息
    getCompany:function (id) {
        this.loading();
        return $.ajax({
            type:"get",
            url:admin.getContextPath()+"/company/getCompany/"+id,
            dataType:"json"
        });
    },
    updateCompany:function (obj) {
        admin.showDialog({
            title:"修改公司",
            content:function () {
                var content="<div>公司名称：<input type=\"text\" id=\"cname\" class=\"text\" value=\""+obj.name+"\"/></div><div class=\"error\" style=\"display:none;margin-left: 70px;\" ></div>";
                content+="<div style=\"margin-top:10px;\">呼入号码：<input type=\"text\" id=\"code\" value=\""+obj.code+"\" class=\"text\"/></div><div class=\"error\" style=\"display:none;margin-left: 70px;\"></div>";
                content+="<div style=\"margin-top:10px;\">呼出号码：<input type=\"text\" id=\"showNum\" value=\""+obj.showNum+"\" class=\"text\"/></div><div class=\"error\" style=\"display:none;margin-left: 70px;\"></div>";
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
                    $.post(admin.getContextPath()+"/company/update",{"company.id":obj.id,"company.name":cname,"company.code":code,"company.showNum":showNum},function (data) {
                        if(data.result){//添加成功,更新数据
                            obj.dom.find("input.c_code").val(data.data.code);
                            obj.dom.find("input.showNum").val(data.data.showNum);
                            obj.dom.find("span.lspan").text(data.data.name);
                        }else{//添加失败
                            admin.showDialog({"content":data.msg});
                        }
                    });

                }
                return flag;
            }
        });

    },
    //删除公司
    deleteCompany:function (id) {
        this.showDialog({
            content:"点击确定将会删除该公司及与该公司相关的所有信息，是否删除？",
            ok:function () {
                $.get(admin.getContextPath()+"/company/delete/"+id,function (data) {
                    if(data.result){//删除成功
                        location.href=common.getContextPath()+"/admin/page";
                    }else{
                        admin.showDialog({
                            content:"删除失败！"
                        });
                    }
                });
            }
        });
    },
    deleteUsers:function (obj) {
        admin.showDialog({
            content:"是否删除？",
            ok:function () {
                $.post(admin.getContextPath()+"/admin/deleteUsers",{"id":obj.join(",")},function (data) {
                    if(data.result){//删除成功
                        location.reload();
                    }else{
                        admin.showDialog({
                            content:"删除失败"
                        });
                    }
                });
            }
        });

    }
}