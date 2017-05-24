/**
 * Created by MrQin on 2016/11/29.
 */
window.onError=function (msg) {
    console.log("error:"+msg);
    //alert("error");
    return true;
}
//webSocket操作对象
var webSocketUtil={
    //Websocket对象
    ws:null,
    //关闭WebSocket连接
    close:function () {
        if(this.ws){
            this.ws.close();
        }
    },
    //呼叫状态码
    Status:{
        CALL_STATUS_WAIT:"0",//
        CALL_STATUS_IDLE:"6000",
        CALL_STATUS_TRING:"6001",
        CALL_STATUS_RING:"6002",
        CALL_STATUS_ANSWER:"6003",
        CALL_STATUS_BYE:"6004",
        CALL_STATUS_FAIL:'6005',
        CALL_STATUS_REJECT:"6006",
        CALL_STATUS_JOIN_MEET:"6007",
        CALL_STATUS_LEAVE_MEET:"6008",
        CALL_STATUS_CANCEL_CALL:"6009"
    },
    Action:{
        INIT:"",//初始化websocket
        ACTION_CREATE_CALL:"1000",//发起呼叫
        ACTION_CREATE_MEET:"1001",//创建会议
        ACTION_JOIN_MEET:"1002",//加入会议
        ACTION_LEAVE_MEET:"1003",//离开会议
        ACTION_MEMBER_SILENT:"1004",//禁止发言
        ACTION_MEMBER_SPEAK:"1005",//允许发言
        ACTION_BEGIN_PLAY_VOICE:"1006",//开始会场放音
        ACTION_STOP_PLAY_VOICE:"1007",//停止会场放音
        ACTION_BEGIN_RECORD:"1008",//开始会场录音
        ACTION_STOP_RECORD:"1009",//停止会场录音
        ACTION_CLOSE_MEET:"1010",//结束会议
        ACTION_GET_MEET_TIME:"1011",//获取会议进行时长
        ACTION_GET_MEET_ALL_TIME:"1012",//获取会议中的总计时长
        ACTION_CALL_STATUS_REPORT:"1013",//会议状态变更上报
        ACTION_GET_MEET_CALL_STATUS:"1014",//获取会议中的与会电话状态
        ACTION_CALLLOG_REPORT:"1015",//通话记录上报
        ACTION_MEET_REPORT_NUMBER:"1016",//会场报数
        ACTION_WEBSOCKET_CONNECT:"1021",//websocket连接
        ACTION_BYE:"1022",//主动挂机
        ACTION_CREATE_CALLIN_MEET:"1023",//创建支持呼入式的会议
        ACTION_MEET_OPER_LOCK:"1024",//会议锁定/解锁操作
        YES:"3000",//是
        NO:"3001",//否
        SUCCESS:"4000",//成功
        FAIL:"4001",//失败
        MEET_LEVEL_AUDIENCE:"9000",//会议级别-听众
        MEET_LEVEL_CHARIMAN:"9001"//会议级别-主席
    },
    /**
     * 获取websocket对象
     * @param url ：websocket服务地址
     * @param options ：websocket的相关事件触发时执行的方法
     */
    getWebSocket:function (url,options) {
        if (!("WebSocket" in window) && !("MozWebSocket" in window)) {
            alert("对不起，您的浏览版本太低，请使用Firefox 4、Chrome 4、Opera 10.70以及Safari 5浏览器");
            closeWindow();
            return;
        }
        // var ws;
        try{
            // this.ws=new WebSocket(url);
            this.ws=new ReconnectingWebSocket(url);
            this.ws.reconnectInterval=5000;
        }catch (e){
            console.log(e);
            alert("Websocket连接失败，请重试！");
            closeWindow();
        }

        this.ws.onopen=function(e){
            if(options.onopen){
                options.onopen(e);
            }
        }
        this.ws.onmessage=function(e){
            if(options.onmessage){
                options.onmessage(e);
            }
        }
        this.ws.onerror=function(e){
            if(options.onerror){
                options.onerror(e);
            }
        }
        this.ws.onclose=function(e){
            if(options.onclose){
                options.onclose(e);
            }
        }
        return this.ws;
    },
    //根据状态码获取状态信息
    getStatusByCode:function (code) {
        var status="";
        switch(code){
            case this.Status.CALL_STATUS_WAIT:
                status="等待呼叫";
                break;
            case this.Status.CALL_STATUS_IDLE:
                status="空闲";
                break;
            case this.Status.CALL_STATUS_TRING:
                status="处理中";
                break;
            case this.Status.CALL_STATUS_RING:
                status="振铃中";
                break;
            case this.Status.CALL_STATUS_ANSWER:
                status="应答";
                break;
            case this.Status.CALL_STATUS_BYE:
                status="挂机";
                break;
            case this.Status.CALL_STATUS_FAIL:
                status="呼叫失败";
                break;
            case this.Status.CALL_STATUS_REJECT:
                status="拒接";
                break;
            case this.Status.CALL_STATUS_JOIN_MEET:
                status="加入会议";
                break;
            case this.Status.CALL_STATUS_LEAVE_MEET:
                status="通话结束";
                break;
            case this.Status.CALL_STATUS_CANCEL_CALL:
                status="取消呼叫";
                break;
        }
        return status;
    },
    //产生32位不重复随机数随机数
    getReqId:function () {
        var numbers=["0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"];
        var arr=new Array(32);
        for(var i=0;i<arr.length;i++){
            arr[i]=numbers[parseInt(Math.random()*35)];
        }
        var str=arr.join("");
        return str;
    },
    //发送消息
    send:function (msg) {
        if(this.ws){
            //判断websocket是否关闭，如果关闭则重新初始化
            if(this.ws.readyState==WebSocket.OPEN) {
                this.ws.send(msg);
            }else{
                showInfo("连接断开", true);
            }
            // }else if(this.ws.readyState=WebSocket.CLOSED){
            //     showInfo("连接断开", true);
            //     initWebsocket();
            // }
        }
    },
    //状态变更通知
    statusNotify:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_CALL_STATUS_REPORT+"\"}";
        webSocketUtil.send(msg);
    },
    //根据会议ID获取会议中的电话状态
    getStatus:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_GET_MEET_CALL_STATUS+"\"}";
        webSocketUtil.send(msg);
    },
    //停止会场录音
    stopRecord:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_STOP_RECORD+"\"}";
        webSocketUtil.send(msg);
    },
    //开始会场录音
    startRecord:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_BEGIN_RECORD+"\"}";
        webSocketUtil.send(msg);
    },
    //开始会场放音
    startPlayback:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_BEGIN_PLAY_VOICE+"\"}";
        webSocketUtil.send(msg);
    },
    //停止会场放音
    stopPlayback:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_STOP_PLAY_VOICE+"\"}";
        webSocketUtil.send(msg);
    },
    //开始报数
    startCount:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_MEET_REPORT_NUMBER+"\"}";
        webSocketUtil.send(msg);
    },
    //结束会议
    stopMeeting:function (meetId) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_CLOSE_MEET+"\"}";
        webSocketUtil.send(msg);
    },
    //加入会议
    joinMeeting:function (meetId,callers) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_JOIN_MEET+"\",\"callers\":"+JSON.stringify(callers)+"}";
        webSocketUtil.send(msg);
    },
    //操作成员离开会议
    leaveMeeting:function (meetId,callers) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_LEAVE_MEET+"\",\"callers\":"+JSON.stringify(callers)+"}";
        webSocketUtil.send(msg);
    },
    //发起呼叫
    call:function (caller,called) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"costNum1\":\""+caller+"\",\"showNum1\":\""+caller+"\",\"caller\":\""+caller+"\",\"costNum2\":\""+called+"\",\"showNum2\":\""+called+"\",\"action\":\""+this.Action.ACTION_CREATE_CALL+"\"}";
        webSocketUtil.send(msg);
    },
    //禁止发言
    forbidTalk:function (meetId,callers) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_MEMBER_SILENT+"\",\"callers\":"+JSON.stringify(callers)+"}";
        webSocketUtil.send(msg);
    },
    //允许发言
    allowTalk:function (meetId,callers) {
        var reqId=webSocketUtil.getReqId();
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_MEMBER_SPEAK+"\",\"callers\":"+JSON.stringify(callers)+"}";
        webSocketUtil.send(msg);
    },
    //状态变更通知
    // stateChangeNotify:function (meetId,caller) {
    //     var reqId=getReqId();
    //     var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_CALL_STATUS_REPORT+"\",}";
    //
    // }
    //挂机
    hangup:function () {
        
    },
    //会议锁门
    lock:function (meetId,isLock) {
        var reqId=webSocketUtil.getReqId();
        var l;
        if(isLock){
            l=this.Action.YES;
        }else{
            l=this.Action.NO;
        }
        var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+this.Action.ACTION_MEET_OPER_LOCK+"\",\"lock\":\""+l+"\"}";
        webSocketUtil.send(msg);
    }
}


