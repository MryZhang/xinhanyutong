/**
 * Created by DELL on 2017/03/01.
 */
var threshold={
    /**
     * 修改余额阙值
     * @param dom 显示余额阙值dom对象
     */
    udpate:function (dom) {
        common.showDialog({
            title:"修改余额阙值",
            content:function () {
                var content="<div>阙值：<input type='number' id='number' class='text' value='"+$(dom).text()+"' onfocus='threshold.onfocus(this)'/></div>";
                content+="<div class='error' id='t_error' style='display:none;margin-left: 70px;margin-top: 10px;'></div>";
                return content;
            },ok:function () {
                var value=$("#number").val().trim()
                var msg,flag=true;
                if(!value){
                    flag=false;
                }else if(!common.rationalRegex.test(value)){
                    msg="阙值格式不正确";
                    flag=false;
                }
                if(msg){
                    $("#t_error").text(msg).show();
                }
                if(flag){
                    $.ajax({
                        type:"post",
                        url:common.getContextPath()+"/admin/threshold/update",
                        dataType:"json",
                        data:{"value":value},
                        success:function (data) {
                            common.isLoginTimeout(data);
                            if(data.result){
                                $(dom).text(value);
                            }else{
                                var msg=data.msg||"操作失败";
                                common.showDialog({content:msg});
                            }
                        },error:function (err) {
                            console.log(err.responseText)
                        }
                    });
                }
                return flag;
            }
        });
    },
    onfocus:function (dom) {
        $(dom).parent().next().hide();
    }
};