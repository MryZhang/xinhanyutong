/**
 * Created by MrQin on 2016/11/7.
 */
var meetlist={
    //展开或收起操作
    expand:function (dom) {
        var $content=$(dom).parent().next().find("td>div");
        if($content.is(":hidden")){
            $(dom).find("img").get(0).src=common.getContextPath()+"/img/minus.gif";
            $content.show();
        }else{
            $(dom).find("img").get(0).src=common.getContextPath()+"/img/plus.gif";
            $content.hide();
        }
    },
    //取消固定会议
    cancelMeet:function (obj,dom) {
        common.showDialog({
            content:"是否取消当前会议？",
            ok:function () {
                $.ajax({
                    type:"post",
                    url:common.getContextPath()+"/ordermeet/cancel",
                    data:obj,
                    dataType:"json",
                    success:function (data) {
                        //common.isLoginTimeout(data);
                        if(data.result===true){//取消成功
                            $(dom).closest("tr").remove();
                        }else{
                            // if(data.result===undefined){//登陆过期
                            //     location.href=common.getContextPath()+"/login"
                            // }
                        }
                    }
                });
            }
        });
    },
    //取消及时会议
    cancelMeeting:function (rid,dom) {
        common.showDialog({
            content:"是否取消当前会议？",
            ok:function () {
                $.ajax({
                    type:"get",
                    url:common.getContextPath()+"/meet/cancelMeet/"+rid,
                    dataType:"json",
                    success:function (data) {
                        if(data.result===true){//取消成功
                            $(dom).closest("tr").remove();
                        }else{
                            if(data.result===undefined){//登陆过期
                                location.href=common.getContextPath()+"/login"
                            }else{
                                common.showDialog({
                                    content:"操作失败！"
                                });
                            }
                        }
                    }
                });
            }
        });
    }
}
