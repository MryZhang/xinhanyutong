/**
 * Created by DELL on 2017/02/27.
 */
var detail={
    modeChange:function (obj) {
        var mode=$(obj).val();
        if(mode=="2"){
            $(".month-count-div").show();
        }else{
            $(".month-count-div").hide();
        }
    },
    /**
     * 修改计费模式
     * @param mode 计费模式
     * @param count 包月方数
     * @param bid 账户余额id
     */
    changeMode:function (mode,count,bid) {
        common.showDialog({
            title:"修改计费模式",
            content:function () {
                var content="<div>计费模式：<select name='mode' class='text' onchange='detail.modeChange(this)'>";
                if(mode==1){
                    content+="<option value='1' selected='selected' name='chargingMode'>";
                }else{
                    content+="<option value='1'>";
                }
                content+="分钟计费</option>";
                if(mode==2){
                    content+="<option value='2' selected='selected'>";
                }else {
                    content+="<option value='2'>";
                }
                content+="包月计费</option></select></div>";
                if(mode==1){
                    content+="<div class='month-count-div' style='display: none;margin-top: 10px;'>包月方数：<input class='text' name='monthCount' type='number'/></div>";
                }else{
                    content+="<div class='month-count-div' style='margin-top: 10px;'>包月方数：<input class='text' name='monthCount' type='number' value='"+count+"'/></div>";
                }
                content+="<div class='info' style='margin-top: 10px;margin-left: 70px;'>*修改后将于下月生效</div>";
                content+="<div class='error' id='mode_error' style='display:none;margin-left: 70px;margin-top: 10px;'></div>";
                return content;
            },
            ok:function () {
                var smode=$("select[name=mode]").val();
                var scount=$("input[name=monthCount]").val();
                if(scount.trim()==""){
                    return false;
                }
                $.ajax({
                    type:"post",
                    url:common.getContextPath()+"/admin/charge/changeMode",
                    dataType:"json",
                    data:{"mode":smode,"count":scount,"bid":bid},
                    success:function (data) {
                        common.isLoginTimeout(data);
                        if(data.result){
                            // var desc;
                            // if(smode=="1"){
                            //     desc="分钟计费";
                            //     $("#month_count").hide();
                            // }else{
                            //     desc="包月计费";
                            //     $("#month_count").show();
                            //     $("#monthCountSpan").text(scount);
                            // }
                            // $("#modeDescSpan").text(desc);
                        }else{
                            $("#mode_error").text(data.msg).show();
                            return false;
                        }
                    },error:function (err) {
                        console.log(err.responseText)
                    }
                });
            }
        });
    }
};
