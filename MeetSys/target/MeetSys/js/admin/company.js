/**
 * Created by DELL on 2017/04/05.
 */
var company={
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
                    var obj={name:value};
                    var $input = $("input[name='cv.id']");
                    if($input&&$input.get(0)){
                        obj.id=$input.val();
                    }
                    company.exists(obj,function () {
                       $("#c_name").attr("class","text highlight2").next().text("该公司已存在").attr("class","addConTip error");
                    });
                }
                break;
            case "c_callNum":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("请选择呼入号码").attr("class","addConTip error");
                }
                break;
            case "c_showNum":
                if(value==""){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("请选择呼出号码").attr("class","addConTip error");
                }
                break;
            case "c_package":
                if(value==""||value=="请选择"){
                    flag=false;
                    $(dom).attr("class","text highlight2").next().text("请选择套餐").attr("class","addConTip error");
                }
                break;
            case "c_count":
                if($(dom).is(":visible")){
                    if(value==""){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().text("包月方数不能为空").attr("class","addConTip error");
                    }else if(!common.positiveIntegerRegex.test(value)){
                        flag=false;
                        $(dom).attr("class","text highlight2").next().text("包月方数格式不正确").attr("class","addConTip error");
                    }
                }
                break;
        }
        return flag;
    },
    //验证公司是否已存在
    exists:function (obj,fn) {
        $.ajax({
            type:"post",
            url:common.getContextPath()+"/company/exists",
            dataType:"json",
            data:obj,
            success:function (data) {
                common.isLoginTimeout(data);
                if(data.result){
                    fn&&fn();
                }
            },error:function (err) {
                console.log(err.responseText);
            }
        });
    },
    //选择套餐
    selectPackage:function (dom) {
        var pgdata=$("body").data("pgdata");
        if(!pgdata){
            $.ajax({
                type:"get",
                url:common.getContextPath()+"/admin/package/list",
                dataType:"json",
                success:function (data) {
                    common.isLoginTimeout(data);
                    //将数据缓存
                    $("body").data("pgdata",data.data);
                    loadpg(data.data);
                },error:function (err) {
                    console.log(err.responseText);
                }
            });
        }else{
            loadpg(pgdata);
        }

        function loadpg(data) {
            var content="<div style='max-height: 200px;overflow-y: auto;'><table id='ptable'><thead><tr><th class=\"t_index\" rowspan=\"2\"></th>";
            content+="<th class=\"t_name\" rowspan=\"2\">套餐名称</th>";
            content+="<th class=\"t_callin\" colspan=\"2\">呼入</th>";
            content+="<th class=\"t_callout\" colspan=\"2\">呼出</th>";
            content+="<th rowspan=\"2\" class=\"t_sms\">短信费率</th>";
            content+="</tr><tr>";
            content+="<th class=\"t_callin_mode\">计费模式</th>";
            content+="<th class=\"t_callin_rate\">费率</th>";
            content+="<th class=\"t_callout_mode\">计费模式</th>";
            content+="<th class=\"t_callout_rate\">费率</th>";
            content+="</tr></thead>";
            content+="<tbody>";
            for(var i=0;i<data.length;i++){
                content+="<tr>";
                content+="<td><input type='checkbox' value='"+data[i].id+"'></td>";
                content+="<td>"+data[i].name+"</td>";
                content+="<td>"+data[i].callInMode+"</td>";
                content+="<td>"+data[i].callInRate+"</td>";
                content+="<td>"+data[i].callOutMode+"</td>";
                content+="<td>"+data[i].callOutRate+"</td>";
                content+="<td>"+data[i].smsRate+"</td></tr>";
            }
            content+="</tbody></div>";
            common.showDialog({
                title:"选择套餐",
                content:content,
                ok:function () {
                    var $ckb = $("#ptable input[type=checkbox]:checked");
                    if($ckb&&$ckb.get(0)){
                        var id=$ckb.val();
                        $("#c_pid").val(id);
                        $(dom).val($ckb.closest("tr").find("td:eq(1)").text());
                        var mode=$ckb.closest("tr").find("td:eq(2)").text();
                        //如果计费模式为包月，则显示包月方数
                        if(mode.indexOf("包月")!=-1){
                            $("#monthlyCount").show();
                        }else{
                            $("#monthlyCount").hide();
                        }
                    }
                }
            });
            $("body").on("change","#ptable input[type=checkbox]",function () {
                $("#ptable input[type=checkbox]").each(function () {
                    this.checked=false;
                });
                this.checked=true;
            });
        }
    }
};