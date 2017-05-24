/**
 * Created by MrQin on 2016/11/7.
 */
var login={
    //显示提示窗，需要引入artDialog
    showTip:function(txt,fn){
        dialog({title:"消息",content:txt,width:200,okValue:"确定",ok:function(){
            fn&&fn();
            return true;
        }}).showModal();
    },
    //显示错误提示
    showErrorTips:function (str) {
        $("#err_m").text(str);
        $("#error_tips").show();
    },
    //登陆验证
    dologin:function(){
        var $username=$("#userName");
        var $password=$("#password");
        var username=$username.val();
        var password=$password.val();
        var flag=true;
        //使用trim()，要先导入common.js
        if(username.trim()==""){
            flag=false;
            $username.focus();
            this.showErrorTips("用户名不能为空");
        }else if(password.trim()==""){
            flag=false;
            $password.focus();
            this.showErrorTips("密码不能为空");
        }else{
            var authcode=document.getElementById("authCode");
            if(authcode&&authcode.value.trim()==""){
                flag=false;
                $(authcode).focus();
                this.showErrorTips("验证码不能为空");
            }
        }
        return flag;
    },
    //判断当前浏览器是否支持H5 Websocket
    isSupportWebsocket:function(){
        if (!("WebSocket" in window) && !("MozWebSocket" in window)) {
            alert("对不起，您的浏览器不支持Html5，请下载Chrome浏览器！");
            location.href="http://sw.bos.baidu.com/sw-search-sp/software/13d93a08a2990/ChromeStandalone_55.0.2883.87_Setup.exe";
        }
    }
};
