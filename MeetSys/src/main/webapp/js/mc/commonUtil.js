/**
 * Created by MrQin on 2016/11/29.
 */

//显示消息
function showInfo(){
    var msg=arguments[0];
    var warn=arguments[1];
    if(warn){
        content="<div class=\"text-danger\">";
    }else{
        content="<div>";
    }
    var date=new Date();
    var hour = date.getHours();
    if(hour<10){
        hour="0"+hour;
    }
    var minute=date.getMinutes();
    if(minute<10){
        minute="0"+minute;
    }
    content+=msg+"<div class=\"pull-right\">"+hour+":"+minute+"</div></div>";
    $("#info_list").prepend(content);
}

//显示模态窗口
function showModal(title, content, okfn, isBig) {
    if (isBig) {
        $("#myModal .modal-dialog").removeClass("modal-sm");
    } else {
        $("#myModal .modal-dialog").addClass("modal-sm");
    }
    $("#myModal .modal-title").text(title);
    $("#myModal .modal-body").html(content);
    $("#cancelBtn").show();
    $("#myModal").modal("show");
    document.getElementById("closeBtn").onclick=function(){}
    document.getElementById("okBtn").onclick = function () {
        if(okfn){
            okfn();
        }
        dismiss();
    };
}

//消息提示框
function showTip(){
    var msg=arguments[0];
    var fn=arguments[1];
    var title=arguments[2]||"消息";
    var size=arguments[3]||"sm";
    if(size=="sm"){
        $("#myModal .modal-dialog").addClass("modal-sm");
    }else if(size=="lg"){
        $("#myModal .modal-dialog").addClass("modal-lg");
    }

    $("#myModal .modal-title").text(title);
    $("#myModal .modal-body").html("<div class=\"alert-txt text-center\">"+msg+"</div>");
    $("#cancelBtn").hide();
    $("#myModal").modal({backdrop:false});
    $("#myModal").modal("show");
    document.getElementById("okBtn").onclick = function () {
        dismiss();
    };
    $("#myModal").on("hidden.bs.modal",function (e) {
        fn&&fn();
    });
    document.getElementById("closeBtn").onclick=function(){
        // if(fn){
        //     fn();
        // }
    }
}

//隐藏模态窗口
function dismiss() {
    $("#myModal").modal("hide");
}

//创建提示
function Alert(msg) {
    var msgw, msgh, bgColor;
    msgw = 350;
    msgh = 25;
    bgColor = "#F4C600";
    var msgObj = document.createElement("div");
    msgObj.setAttribute("id", "alertmsgDiv");
    msgObj.style.position = "absolute";
    msgObj.style.bottom = "0";
    msgObj.style.textAlign = "center";
    msgObj.style.left = "50%";
    msgObj.style.font = "12px/1.6em Verdana, Geneva, Arial, Helvetica, sans-serif";
    msgObj.style.marginLeft = "-125px";
    msgObj.style.width = msgw + "px";
    msgObj.style.height = msgh + "px";
    msgObj.style.lineHeight = "25px";
    msgObj.style.background = bgColor;
    msgObj.style.zIndex = "99";
    msgObj.style.filter = "progid:DXImageTransform.Microsoft.Alpha(startX=20, startY=20, finishX=100, finishY=100,style=1,opacity=75,finishOpacity=100);";
    msgObj.style.opacity = "0.75";
    msgObj.style.color = "white";
    msgObj.innerHTML = msg;
    document.body.appendChild(msgObj);
    setTimeout("closewin()", 2000);
}

//关闭提示
function closewin() {
    //document.body.removeChild(document.getElementById("alertbgDiv"));
    //document.getElementById("alertmsgDiv").removeChild(document.getElementById("alertmsgTitle"));
    var msgObj = document.getElementById("alertmsgDiv");
    if (msgObj) {
        document.body.removeChild(msgObj);
    }
}

//关闭当前窗口并刷新打开该页面的窗口
function closeWindow(){
    if(window.opener){
        window.opener.location.reload();
    }
    window.opener=null;
    window.open("", "_self");
    window.close();
}
