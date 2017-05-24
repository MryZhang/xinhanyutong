//监测js文件错误，并将错误上报到服务器
window.onerror=function(msg,url,line){
	$.ajax({
		type:"post",
		url:"/log",
		data:{"msg":msg,"url":url,"line":line},
		success:function(data){
			
		},error:function(err){
			console.log(err.statusText);
			console.log(err.responseText);
		}
	});
}
//============================================
var local = {};//全局对象，存储临时数据
//加载个人联系人数据
//data：数据源
//fields：数据字段
//type：加载的是个人联系人还是公共联系人数据
function loadContacts(data, fields, type) {
	if (data) {
		var meetId=$("#meetId").val();
		var content = "";
		for (var i = 0, len = data.length; i < len; i++) {
			content += "<div class=\"group\" data-toggle=\"collapse\" data-target=\"#" + type + "_" + i + "\" aria-expanded=\"false\" aria-controls=\"" + type + "_" + i + "\">";
			content += "<i class=\"icon-caret-right\"></i>&nbsp;&nbsp;<input type=\"checkbox\" class=\"chkAll\" />&nbsp;" + data[i].name + "</div>";
			content += "<div class=\"collapse\" id=\"" + type + "_" + i + "\">";
			if (data[i].children && data[i].children.length > 0) {
				content += " <table class=\"table table-hover table-condensed\">";
				var children = data[i].children;
				for (var j = 0, l = children.length; j < l; j++) {
					content += "<tr>";
					for (var n = 0, size = fields.length; n < size; n++) {
						var txt=children[j][fields[n]];
						txt=txt?txt:"";
						if (n == 0) {
							content += "<td><input type=\"checkbox\" />&nbsp;" + txt + "</td>";
							continue;
						}
						content += "<td>" + txt + "</td>";
					}
					//判断会议是否开始
					if(meetId){
						content += "<td><a href=\"javascript:; \" class=\"call\"></a></td></tr>";
					}else{
						content += "<td><a href=\"javascript:; \" class=\"add-invite\"></a></td></tr>";
					}
					
				}
				content += "</table>";
			} else {
				content += "<div class=\"text-center\">无联系人数据</div>";
			}
			content += "</div>";
		}
		//console.log(content);
		$("#"+type).append(content);
	}
}
//联系人搜索
function searchContacts(str) {
	if (str.trim() == "") {
		return;
	}
	var result = [];
	var personalContacts = $("body").data("personalContacts");
	var publicContacts = $("body").data("publicContacts");
	if (personalContacts && personalContacts.length > 0) {
		for (var i = 0, len = personalContacts.length; i < len; i++) {
			var children = personalContacts[i].children;
			var group=personalContacts[i].name;
			for(var n = 0, size=children.length; n<size;n++){
				var name = children[n].name;
				var phone = children[n].phone;
				if (name.indexOf(str) != -1 || phone.indexOf(str) != - 1) {
						result.push({name: name, phone: phone, type: "个人通讯录",group:group});
				}
			}
		}
	}
	if (publicContacts && publicContacts.length > 0) {
		for (var i = 0, len = publicContacts.length; i < len; i++) {
			var children = publicContacts[i].children;
			var group=publicContacts[i].name;
			for (var n = 0, size = children.length; n < size; n++) {
				var name = children[n].name;
				var phone = children[n].phone;
				var position = children[n].position;
				if (name.indexOf(str) != -1 || phone.indexOf(str) != -1 || (position && position.indexOf(str) != -1)) {
					result.push({ name: name, phone: phone, type: "公共通讯录",group:group});
				}
			}
			
		}
	}
	var content = "";
	
	if (result.length > 0) {
		for (var i = 0, len = result.length; i < len; i++) {
			var temp=result[i].name+"("+result[i].phone+")--"+result[i].type+"("+result[i].group+")";
			content+="<li title=\""+temp+"\">"+temp+"</li>";
		}
	} else {
		content = "<li>无搜索结果...</li>";
	}
	$("#contactsResult").html(content).show();
}

//选中搜索的联系人结果
function getContactResult(phone, type, group) {
	$("#contact_tab li:eq(" + type + ") a").click();
	var $tar = $("#contacts_content>div:eq(" + type + ")").find("div.group");
	$("#contacts_content div.collapse table tr").removeClass("success");
	$tar.each(function () {
		if ($(this).text().trim() == group) {
			$(this).next().collapse("show");
			$(this).next().find("tr").each(function () {
				if ($(this).find("td:eq(1)").text().trim() == phone) {
					$(this).addClass("success");
					return false;
				}
			});
			$(this).find("i").attr("class", "icon-caret-down");
		} else {
			$(this).next().collapse("hide");
			$(this).find("i").attr("class", "icon-caret-right");
		}
	});
}

//加载会议邀请人数据
function loadInvite(data){
	if(data&&data.length>0){
		var meetId=$("#meetId").val();
		var content="";
		for(var i=0,len=data.length;i<len;i++){
			var name=data[i].name;
			var phone=data[i].phone;
			addHost(name, phone)
			changeContactAdded(phone);
			content+="<tr><td>"+name+"</td><td>"+phone+"</td><td>"+data[i].status+"</td><td>";
			if(meetId){
				content+="<a href=\"javascript:;\" class=\"call\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
			}else{
				content+="<a href=\"javascript:;\" class=\"clear\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
			}
		}
		$("#invite table tbody").append(content);
	}
}

//搜索参会人
function searchInvite(str){
	if (str.trim() == "") {
		return;
	}
	var invite=$("body").data("invite");
	var result=[];
	if(invite&&invite.length>0){
		for(var i=0,len=invite.length;i<len;i++){
			var name=invite[i].name;
			var phone=invite[i].phone;
			if(name.indexOf(str)!=-1||phone.indexOf(str)!=-1){
				result.push({name:name,phone:phone});
			}
		}
	}
	var content="";
	if(result.length>0){
		for (var i = 0, len = result.length; i < len; i++) {
			var temp=result[i].name+"("+result[i].phone+")";
			content+="<li title=\""+temp+"\">"+temp+"</li>";
		}
	}else{
		content = "<li>无搜索结果...</li>";
	}
	$("#inviteResult").html(content).show();
}

//选中参会人搜索结果
function getInviteResult(phone){
	$("#invite table>tbody>tr").removeClass("success");
	$("#invite table>tbody>tr").each(function(){
		var telnum=$(this).find("td:eq(1)").text().trim();
		if(telnum==phone){
			$(this).addClass("success");
			return false;
		}
	});
}

//搜索会议中的参会人
function searchInMeet(str){
	if(str.trim()==""){
		return;
	}
	var inmeet=$("body").data("inmeet");
	var result=[];
	if(inmeet&&inmeet.length>0){
		for(var i=0,len=inmeet.length;i<len;i++){
			var name=inmeet[i].name;
			var phone=inmeet[i].phone;
			if(name.indexOf(str)!=-1||phone.indexOf(str)!=-1){
				result.push({name:name,phone:phone});
			}
		}
	}
	var content="";
	if(result.length>0){
		for (var i = 0, len = result.length; i < len; i++) {
			var temp=result[i].name+"("+result[i].phone+")";
			content+="<li title=\""+temp+"\">"+temp+"</li>";
		}
	}else{
		content = "<li>无搜索结果...</li>";
	}
	$("#inmeetResult").html(content).show();
}

//选中会议中参会人搜索结果
function getInMeetResult(phone){
	$("#attendee_list table>tbody>tr").removeClass("success");
	$("#attendee_list table>tbody>tr").each(function(){
		var telnum=$(this).find("td:eq(1)").text().trim();
		if(telnum==phone){
			$(this).addClass("success");
			return false;
		}
	});
}

//加载参会人数据
function loadInMeet(data){
	if(data&&data.length>0){
		var content="";
		for(var i=0,len=data.length;i<len;i++){
			var phone=data[i].phone;
			changeContactAdded(phone);
			content+="<tr><td>"+data[i].name+"</td><td>"+phone+"</td><td>"+data[i].role+"</td><td><a href=\"javascript:;\" class=\"mic\"></a>";
			content+="<a href=\"javascript:;\" class=\"call-end\"></a><a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
		}
		$("#attendee_list table tbody").html(content);
	}
}

//向上
function pushUp(dom,phone){
	$(dom).attr("class","push-down");
	var $tbody=$(dom).closest("tbody");
	var $tr=$(dom).closest("tr");
	$tr.remove();
	$tbody.prepend($tr);
}

//向下
function pushDown(dom,phone){
	$(dom).attr("class","push-up");
	var $tbody=$(dom).closest("tbody");
	var $tr=$(dom).closest("tr");
	$tr.remove();
	$tbody.append($tr);
}

//添加选择的联系人数据
function addSelected(name, phone) {
	var flag = true;
	var selected = $("body").data("selected");
	if (!selected) {
		selected=[];
	}
	if (selected.length > 0) {
		for (var i = 0, len = selected.length; i < len; i++) {
			if (selected[i].phone == phone) {
				flag = false;
				break;
			}
		}
	}
	if (flag) {
		selected.push({ name: name, phone: phone });
		$("body").data("selected",selected);
	}
}
//删除选择的联系人数据
function removeSelected(name,phone){
	var selected=$("body").data("selected");
	for(var i=0,len=selected.length;i<len;i++){
		if(selected[i].phone==phone&&selected[i].name==name){
			selected.splice(i,1);
			break;
		}
	}
	$("body").data("selected",selected);
}

//添加会议邀请人
function addInvite(name,phone){
	var invite=$("body").data("invite");
	var flag=true;
	//验证号码是否已在邀请人中或已在会议中
	var invite=$("body").data("invite");
	var inmeet=$("body").data("inmeet");
	
	if(invite&&invite.length>0){
		for(var i=0,len=invite.length;i<len;i++){
			if(phone==invite[i].phone){
				flag=false;
				break;
			}
		}
	}
	if(inmeet&&inmeet.length>0){
		for(var i=0,len=inmeet.length;i<len;i++){
			if(phone==inmeet[i].phone){
				flag=false;
				break;
			}
		}
	}

	if(flag){
		changeContactAdded(phone);
		if(!invite){
			invite=[];
		}
		if(!inmeet){
			inmeet=[];
		}
		invite.push({name:name,phone:phone,status:Status.CALL_STATUS_WAIT});
		$("body").data("invite",invite);
		var meetId=$("#meetId").val();
		if(!meetId){
			addInviteToServerCache(name,phone);
		}
		
		addHost(name, phone);
		var status=getStatusByCode(Status.CALL_STATUS_WAIT)
		var content="<tr><td>"+name+"</td><td>"+phone+"</td><td>"+status+"</td><td>";
		var meetId=$("#meetId").val();
		if(meetId){
			content+="<a href=\"javascript:;\" class=\"call\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
		}else{
			content+="<a href=\"javascript:;\" class=\"clear\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a></td></tr>";
		}
		
		$("#invite table tbody").append(content);
	}
	return flag;
}

//将邀请人数据添加到服务器缓存
function addInviteToServerCache(name,phone){
	var rid=$("#rid").val();
	$.ajax({
		type:"post",
		url:"/meet/addInvite",
		dataType:"json",
		data:{"rid":rid,"name":name,"phone":phone},
		success:function(data){
			
		},error:function(err){
			console.log(err.responseText);
		}
	});
}

//移除参会人数据
function removeInvite(){
	var phone = arguments[0];
	var flag=arguments[1];//判断是否改变通信录操作状态
	$("#invite table>tbody>tr").each(function(){
		var tel=$(this).find("td:eq(1)").text().trim();
		if(tel==phone){
			$(this).remove();
			return false;
		}
	});
	
	var invite=$("body").data("invite");
	var name;
	if(invite&&invite.length>0){
		for(var i=0,len=invite.length;i<len;i++){
			if(invite[i].phone==phone){
				name=invite[i].name;
				invite.splice(i,1);
				break;
			}
		}
	}
	
	$("body").data("invite",invite);
	var meetId=$("#meetId").val();
	if(!meetId){
		//将邀请人数据从服务器缓存中移除
		var rid=$("#rid").val();
		$.ajax({
			type:"post",
			url:"/meet/delInvite",
			dataType:"json",
			data:{"rid":rid,"name":name,"phone":phone},
			success:function(data){
				
			},error:function(err){
				console.log(err.responseText);
			}
		});
		
		removeHost(phone);
	}
	if(!flag){
		$("#contacts_content div.collapse table tr").each(function(){
			var tel=$(this).find("td:eq(1)").text().trim();
			if(tel==phone){
				$(this).find("td:last").html("<a href=\"javascript:;\" class=\"add-invite\"></a>");
			}
		});
	}
	
	return name;
}

//添加参会者
function addInMeet(name,phone){
	var inmeet=$("body").data("inmeet");
	if(!inmeet){
		inmeet=[];
	}
	var flag=true;
	for(var i=0,len=inmeet.length;i<len;i++){
		if(inmeet[i].phone==phone){
			flag=false;
			break;
		}
	}
	if(flag){
		changeContactAdded(phone);
		var hostNum=getHostNum();
		var role="参会人";
		if(phone==hostNum){
			role="主持人";
		}
		inmeet.push({name:name,phone:phone,role:role});
		$("body").data("inmeet",inmeet);
		var content="<tr><td>"+name+"</td><td>"+phone+"</td><td>"+role+"</td><td><a href=\"javascript:;\" class=\"call-end\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"mic\"></a>&nbsp;&nbsp;<a href=\"javascript:;\" class=\"push-up\"></a>&nbsp;&nbsp;</td></tr>";
		$("#attendee_list table>tbody").append(content);
	}
	
}

//移除参会者
function removeInMeet(phone){
	var inmeet=$("body").data("inmeet");
	var name;
	if(inmeet&&inmeet.length>0){
		for(var i=0,len=inmeet.length;i<len;i++){
			if(phone==inmeet[i].phone){
				name=inmeet[i].name;
				inmeet.splice(i,1);
				break;
			}
		}
		$("body").data("inmeet",inmeet);
	}
	
	$("#attendee_list table>tbody>tr").each(function(){
		var num=$(this).find("td:eq(1)").text().trim();
		if(num==phone){
			$(this).remove();
			return false;
		}
	});
	return name;
}

//改变联系人的添加状态为已添加
function changeContactAdded(phone){
	$("#contacts_content div.collapse>table tr").each(function(){
		var telnum=$(this).find("td:eq(1)").text().trim();
		if(telnum==phone){
			$(this).find("td:last").text("已添加");
		}
	});
}

//=====================会场操作START===================================

//初始化websocket
function initWebsocket(){
	var meetId=$("#meetId").val();
	var reqId=getReqId();
	var url="ws://60.190.236.54:21280/app/meet_api?reqId="+reqId+"&action=&meetId="+meetId;
	//var url="ws://111.200.245.187:8082/meetting/meet_api?reqId="+reqId+"&action=&meetId="+meetId;
	getWebSocket(url, local.options);
}

//接收websocket服务器发送的消息
function reciveResult(e){
	var msg=e.data;
	console.log(msg);
	var result=JSON.parse(msg);
	switch(result.action){
	case Action.INIT:
		if(result.status==Action.SUCCESS){
			showInfo(result.content);
			checkStatus();
		}else if(result.status==Action.FAIL&&result.content.indexOf("meetId不存在")!=-1){
			showInfo("会议连接失败！",true);
			showCloseTip();
		}
		break;
	case Action.ACTION_JOIN_MEET:
		break;
	case Action.ACTION_LEAVE_MEET:
		break;
	case Action.ACTION_MEMBER_SILENT:
		var phone=result.caller;
		if(result.status==Action.SUCCESS&&result.caller){
			var name=getName(phone);
			showInfo(name+"("+phone+")已禁止发言");
			changeSlienceState(phone,result.action);
		}else if(result.status==Action.FAIL){
			showInfo("禁言操作失败", true);
		}
		break;
	case Action.ACTION_MEMBER_SPEAK:
		var phone=result.caller;
		if(result.status==Action.SUCCESS&&result.caller){
			var name=getName(phone,result.action);
			showInfo(name+"("+phone+")已允许发言");
			changeSlienceState(phone);
		}else if(result.status==Action.FAIL){
			showInfo("允许发言操作失败", true);
		}
		break;
	case Action.ACTION_BEGIN_PLAY_VOICE:
		if(result.status==Action.SUCCESS){
			showInfo("开始会场放音");
		}else{
			showInfo("会场放音失败",true);
		}
		break;
	case Action.ACTION_STOP_PLAY_VOICE:
		if(result.status==Action.SUCCESS){
			showInfo("结束会场放音");
		}else{
			showInfo("结束会场放音失败",true);
		}
		break;
	case Action.ACTION_CLOSE_MEET:
		if(result.status==Action.SUCCESS){
			showInfo("会议结束");
			closeWindow();
		}else{
			showInfo("结束会议操作失败", true);
		}
		break;
	case Action.ACTION_CALL_STATUS_REPORT:
		changeInviteStatus(result.caller, result.status,result.action);
		break;
	case Action.ACTION_GET_MEET_CALL_STATUS:
		if(result.status==Action.SUCCESS){
			var callers=result.content;
			for(var i=0,len=callers.length;i<len;i++){
				changeInviteStatus(callers[i].caller, callers[i].status,result.action);
			}
		}else{
			if(result.content.indexOf("会议ID不存在")!=-1){
				showCloseTip();
				finishMeet();
			}
		}
		break;
	case Action.ACTION_MEET_REPORT_NUMBER:
		break;
	case Action.ACTION_MEET_OPER_LOCK:
		showInfo(result.content);
		break;
	}
}

//改变禁言操作状态
function changeSlienceState(phone,action){
	$("#attendee_list table>tbody>tr").each(function(){
		var num=$(this).find("td:eq(1)").text().trim();
		if(num==phone){
			if(action==Action.ACTION_MEMBER_SILENT){
				$(this).find("a.mic").attr("class","mic-off");
			}else{
				$(this).find("a.mic-off").attr("class","mic");
			}
			return false;
		}
	});
}

//改变会议邀请人状态
function changeInviteStatus(phone,code,action){
	if(code==Status.CALL_STATUS_ANSWER){//加入会议
		var name=removeInvite(phone,true);
		if(!name){
			name=getName(phone);
		}
		addInMeet(name, phone);
		if(action==Action.ACTION_CALL_STATUS_REPORT){
			showInfo(name+"("+phone+")"+"加入会议");
			var hostNum=getHostNum();
			if(hostNum==phone&&local.isFirst){//如果呼叫的是主持人且会议室刚开始，则在主持人加入会议，呼叫所有邀请人
				callAllInvite();
			}
		}
	}else if(code==Status.CALL_STATUS_BYE||code==Status.CALL_STATUS_LEAVE_MEET){
		var name=removeInMeet(phone);
		if(!name){
			name=getName(phone);
		}
		if(action==Action.ACTION_CALL_STATUS_REPORT&&code==Status.CALL_STATUS_LEAVE_MEET){
			showInfo(name+"("+phone+")"+"离开会议");
		}
		addInvite(name, phone);
	}else{
		changeState(phone,code);
	}
}
function changeState(phone,code){
	$("#invite table>tbody>tr").each(function(){
		var num=$(this).find("td:eq(1)").text().trim();
		if(num==phone){
			$(this).find("td:eq(2)").text(getStatusByCode(code));
			if(code==Status.CALL_STATUS_BYE||code==Status.CALL_STATUS_FAIL||code==Status.CALL_STATUS_REJECT){
				$(this).find("td:last a.call-end").attr("class","call");
			}else{
				$(this).find("td:last a.call").attr("class","call-end");
			}
		}
	});
}

//呼叫所有会议邀请人
function callAllInvite(){
	var invite=$("body").data("invite");
	if(invite&&invite.length>0){
		var meetId=$("#meetId").val();
		var callers=[];
		for(var i=0,len=invite.length;i<len;i++){
			callers.push({name:invite[i].name,caller:invite[i].phone});
		}
		joinMeeting(meetId, callers);
	}
}

//全体静音
function allSlience(){
	var meetId=$("#meetId").val();
	var hostNum=getHostNum();
	var inmeet=$("body").data("inmeet");
	var callers=[];
	if(inmeet.length>0){
		for(var i=0,len=inmeet.length;i<len;i++){
			var name=inmeet[i].name;
			var phone=inmeet[i].phone;
			if(phone!=hostNum){
				callers.push({name:name,caller:phone});
			}
		}
		if(callers.length>0){
			forbidTalk(meetId, callers);
		}
	}
}

//全体允许发言
function allSpeak(){
	var meetId=$("#meetId").val();
	var hostNum=getHostNum();
	var inmeet=$("body").data("inmeet");
	var callers=[];
	if(inmeet.length>0){
		for(var i=0,len=inmeet.length;i<len;i++){
			var name=inmeet[i].name;
			var phone=inmeet[i].phone;
			if(phone!=hostNum){
				callers.push({name:name,caller:phone});
			}
		}
		if(callers.length>0){
			allowTalk(meetId, callers);
		}
	}
}


//检查会议中各个电话的连接状态
function checkStatus(){
	var meetId=$("#meetId").val();
	getStatus(meetId);
	setTimeout("checkStatus()",5000);
}

//窗口关闭提示
function showCloseTip(){
	var msg="会议连接超时，会场已关闭！<div class=\"text-danger\"><span id=\"time\">10</span>s后窗口将关闭</div>";
	showTip(msg, function(){
		finishMeet();
		//closeWindow();
	});
	setTimeout("checkTime()", 1000);
}
function checkTime(){
	var time=$("#time").text();
	time=parseInt(time);
	time--;
	if(time>0){
		$("#time").text(time);
		setTimeout("checkTime()", 1000);
	}else{
		closeWindow();
	}
}

var Action={
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
		FAIL:"4001"//失败
};

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

//websocket发送消息
function send(msg){
	if(ws){
		//判断websocket是否关闭，如果关闭则重新初始化
		if(ws.readyState=WebSocket.OPEN){
			ws.send(msg);
		}else if(ws.readyState=WebSocket.CLOSED){
			showInfo("连接断开", true);
			initWebsocket();
		}
	}
}

//状态变更通知
function statusNotify(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_CALL_STATUS_REPORT+"\"}";
	send(msg);
}


//根据会议ID获取会议中的电话状态
function getStatus(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_GET_MEET_CALL_STATUS+"\"}";
	send(msg);
}

//停止会场录音
function stopRecord(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_STOP_RECORD+"\"}";
	send(msg);
}

//开始会场录音
function startRecord(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_BEGIN_RECORD+"\"}";
	send(msg);
}

//开始会场放音
function startPlayback(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_BEGIN_PLAY_VOICE+"\"}";
	send(msg);
}

//停止会场放音
function stopPlayback(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_STOP_PLAY_VOICE+"\"}";
	send(msg);
}

//开始报数
function startCount(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_MEET_REPORT_NUMBER+"\"}";
	send(msg);
}

//结束会议
function stopMeeting(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_CLOSE_MEET+"\"}";
	send(msg);
}

//加入会议
function joinMeeting(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_JOIN_MEET+"\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//操作成员离开会议
function leaveMeeting(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_LEAVE_MEET+"\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//发起呼叫
function call(caller,called){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"costNum1\":\""+caller+"\",\"showNum1\":\""+caller+"\",\"caller\":\""+caller+"\",\"costNum2\":\""+called+"\",\"showNum2\":\""+called+"\",\"action\":\""+Action.ACTION_CREATE_CALL+"\"}";
	send(msg);
}

//禁止发言
function forbidTalk(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_MEMBER_SILENT+"\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//允许发言
function allowTalk(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_MEMBER_SPEAK+"\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//状态变更通知
function stateChangeNotify(meetId,caller){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_CALL_STATUS_REPORT+"\",}";
}
//挂断
function hangup(){
	
}
//会议锁门
function lock(meetId,isLock){
	var reqId=getReqId();
	var l;
	if(isLock){
		l=Action.YES;
	}else{
		l=Action.NO;
	}
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\""+Action.ACTION_MEET_OPER_LOCK+"\",\"lock\":\""+l+"\"}";
	send(msg);
}

//产生32位不重复随机数随机数
function getReqId(){
	var numbers=["0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"];
	var arr=new Array(32);
	for(var i=0;i<arr.length;i++){
		arr[i]=numbers[parseInt(Math.random()*35)];
	}
	var str=arr.join("");
	return str;
}
//=============================会场操作END==================================

//http创建会议
function createMeet(hostName,hostNum,subject,isRecord,rid,meetPwd){
	$.ajax({
		type:"post",
		url:"/meet/createmeet",
		dataType:"json",
		data:{"hostNum":hostNum,"hostName":hostName,"subject":subject,"rid":rid,"isRecord":isRecord},
		success:function(data){
			$("#startMeetBtn").removeAttr("disabled");
			if(data.result){
				$("#meetStatus").text("会议中...");
				$("#meetId").val(data.meetId);
//				$("#startMeetBtn").text("结束会议");
				initWebsocket();
				changeMeetControlState();
			}else{
				local.isFirst=false;
				showTip("操作失败！",null);
			}
		},error:function(err){
			console.log(err.responseText);
			$("#startMeetBtn").removeAttr("disabled");
		}
	});
}

//获取主持人号码
function getHostNum(){
	var num = $("#mhost").text();
	num=num.substring(num.indexOf("（")+1,num.indexOf("）"));
	return num;
}
//获取主持人名字
function getHostName(){
	var name = $("#mhost").text();
	name=name.substring(0,name.indexOf("（"));
	return name;
}

//添加主持人数据
function addHost(name, phone) {
	var h = {};
	h.phone = phone;
	h.name = name || phone;
	var host=$("body").data("host");
	if (!host) {
        host =[];
	}
    host.push(h);
	$("body").data("host",host);
}
//移除主持人数据
function removeHost(phone) {
	var host=$("body").data("host") ;
	if (host) {
       for (var i = 0, len = host.length; i < len; i++) {
            if (host[i].phone == phone) {
            	host.splice(i, 1);
				$("body").data("host",host);
                break;
			}
		}
	}
}

//获取联系人名称
function getName(phone){
	var personalContacts = $("body").data("personalContacts");
	var publicContacts = $("body").data("publicContacts");
	var invite=$("body").data("invite");
	var inmeet=$("body").data("inmeet");
	var name;
	if(personalContacts&&personalContacts.length>0){
		for(var i=0,len=personalContacts.length;i<len;i++){
			var children=personalContacts[i].children;
			for(var n=0,size=children.length;n<size;n++){
				if(children[n].phone==phone){
					name=children[n].name;
					break;
				}
			}
			if(name){
				break;
			}
		}
	}
	if(!name&&publicContacts&&publicContacts.length>0){
		for(var i=0,len=publicContacts.length;i<len;i++){
			var children=publicContacts[i].children;
			for(var n=0,size=children.length;n<size;n++){
				if(children[n].phone==phone){
					name=children[n].name;
					break;
				}
			}
			if(name){
				break;
			}
		}
	}
	
	if(!name&&invite&&invite.length>0){
		for(var i=0,len=invite.length;i<len;i++){
			if(invite[i].phone==phone){
				name=invite[i].name;
				break;
			}
		}
	}
	
	if(!name&&inmeet&&inmeet.length>0){
		for(var i=0,len=inmeet.length;i<len;i++){
			if(inmeet[i].phone==phone){
				name=inmeet[i].name;
				break;
			}
		}
	}
	
	if(!name){
		name=phone
	}
	return name;
}

//呼叫状态对象
var Status={
	CALL_STATUS_WAIT:"0",//
	CALL_STATUS_IDLE:"6000",
	CALL_STATUS_TRING:"6001",
	CALL_STATUS_RING:"6002",
	CALL_STATUS_ANSWER:"6003",
	CALL_STATUS_BYE:"6004",
	CALL_STATUS_FAIL:'6005',
	CALL_STATUS_REJECT:"6006",
	CALL_STATUS_JOIN_MEET:"6007",
	CALL_STATUS_LEAVE_MEET:"6008"
}; 

//根据状态码获取电话的状态
function getStatusByCode(code){
	var status="";
	switch(code){
	case Status.CALL_STATUS_WAIT:
		status="等待呼叫";
		break;
	case Status.CALL_STATUS_IDLE:
		status="空闲";
		break;
	case Status.CALL_STATUS_TRING:
		status="处理中";
		break;
	case Status.CALL_STATUS_RING:
		status="振铃中";
		break;
	case Status.CALL_STATUS_ANSWER:
		status="应答";
		break;
	case Status.CALL_STATUS_BYE:
		status="挂机";
		break;
	case Status.CALL_STATUS_FAIL:
		status="失败";
		break;
	case Status.CALL_STATUS_REJECT:
		status="拒接";
		break;
	case Status.CALL_STATUS_JOIN_MEET:
		status="加入会议";
		break;
	case Status.CALL_STATUS_LEAVE_MEET:
		status="通话结束";
		break;
	}
	return status;
}

	//根据数据生成主持人选项
	function createHostChoice(data) {
		var result = "<ul class='list-unstyled' id='selectHost'>";
		var hostNum = $("#mhost").text();
		for (var i = 0, len = data.length; i < len; i++) {
			if (hostNum == data[i].name + "（" +data[i].phone + "）") {
				  result += "<li class='bg-success' onclick='select(this)'>" +data[i].name + "（" +data[i].phone + "）</li>";
			 } else {
				   result += "<li onclick='select(this)'>" +data[i].name + "（" +data[i].phone + "）</li>";
			}
		}
		result += "</ul>";
		return result;
    }
    function select(dom) {
		$("#selectHost li").removeAttr("class");
		$(dom).addClass("bg-success");
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
	function showTip(msg,fn){
		$("#myModal .modal-dialog").addClass("modal-sm");
		$("#myModal .modal-title").text("消息");
		$("#myModal .modal-body").html("<div class=\"alert-txt text-center\">"+msg+"</div>");
		$("#cancelBtn").hide();
		$("#myModal").modal({backdrop:false});
		$("#myModal").modal("show");
		document.getElementById("okBtn").onclick = function () {
			if(fn){
				fn();
			}
            dismiss();
		};
		document.getElementById("closeBtn").onclick=function(){
			if(fn){
				fn();
			}
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

String.prototype.trim = function () {
    return this.replace(/(^\s*)|(\s*$)/g, "");
}

	//---------------------------------------------------  
	//日期格式化  
	//格式 YYYY/yyyy/YY/yy 表示年份  
	//MM/M 月份  
	//W/w 星期  
	//dd/DD/d/D 日期  
	//hh/HH/h/H 时间  
	//mm/m 分钟  
	//ss/SS/s/S 秒  
	//---------------------------------------------------  
Date.prototype.Format = function (formatStr) {
	var str = formatStr;
	var Week = ['日', '一', '二', '三', '四', '五', '六'];

    str = str.replace(/yyyy|YYYY/, this.getFullYear());
    str = str.replace(/yy|YY/, (this.getYear() % 100) > 9 ? (this.getYear() % 100).toString() : '0' + (this.getYear() % 100));

    str = str.replace(/MM/, this.getMonth() > 9 ? this.getMonth().toString() : '0' + (this.getMonth() + 1));
    str = str.replace(/M/g, this.getMonth());

    str = str.replace(/w|W/g, Week[this.getDay()]);

    str = str.replace(/dd|DD/, this.getDate() > 9 ? this.getDate().toString() : '0' + this.getDate());
    str = str.replace(/d|D/g, this.getDate());

    str = str.replace(/hh|HH/, this.getHours() > 9 ? this.getHours().toString() : '0' + this.getHours());
    str = str.replace(/h|H/g, this.getHours());
    str = str.replace(/mm/, this.getMinutes() > 9 ? this.getMinutes().toString() : '0' + this.getMinutes());
    str = str.replace(/m/g, this.getMinutes());

    str = str.replace(/ss|SS/, this.getSeconds() > 9 ? this.getSeconds().toString() : '0' + this.getSeconds());
    str = str.replace(/s|S/g, this.getSeconds());

	return str;
}

function closeWindow(){
	//关闭当前窗口并刷新打开该页面的窗口
	if(window.opener){
		window.opener.location.reload();
	}
	window.opener=null;
	window.open("", "_self");
	window.close();
}

//改变会议操作的状态
function changeMeetControlState(){
	$("#startMeetBtn").text("结束会议");
	$("#edit_subject").parent().hide();
	$("#change_host").parent().hide();
	$("#isRecord").bootstrapSwitch("disabled",true);
	$("#isAllowSpeak").bootstrapSwitch("disabled",false);
	$("#playback").bootstrapSwitch("disabled",false);
	$("#lockMeet").bootstrapSwitch("disabled",false);
	$("a.add-invite").attr("class","call");
	$("a.clear").attr("class","call");
}

//会议结束后变更会议记录
function finishMeet(){
	var rid=$("#rid").val();
	$.ajax({
		type:"get",
		url:"/meet/finishMeet/"+rid,
		dataType:"json",
		success:function(data){
			closeWindow();
		},error:function(err){
			console.log(err.responseText);
		}
	});
}

//初始化数据
function init(){
	$("#loading").show();
	//加载个人通信录
	var personalContacts;
	$.ajax({
		type:"get",
		url:"/personalcontacts/getContacts",
		dataType:"json",
		success:function(data){
			personalContacts=data;
		},error:function(err){
			console.log(err.responseText);
		}
	}).done(function(){
		$("body").data("personalContacts",personalContacts);
		loadContacts(personalContacts, ["name","phone"], "personl_contacts");
		//加载公共通信录
		var publicContacts;
		$.ajax({
			type:"get",
			url:"/publiccontacts/getContacts",
			dataType:"json",
			success:function(data){
				publicContacts=data;
			},error:function(err){
				console.log(err.responseText);
			}
		}).done(function(){
			$("body").data("publicContacts",publicContacts);
			loadContacts(publicContacts, ["name","phone","position"], "public_contacts");
			//加载会议中的数据
			var inmeetData=$("#inmeetData").text();
			if(inmeetData){
				inmeetData=JSON.parse(inmeetData);
				$("body").data("inmeet",inmeetData);
				loadInMeet(inmeetData);
			}
			//加载会议邀请人数据
			var inviteData=$("#inviteData").text();
			if(inviteData){
				inviteData=JSON.parse(inviteData);
				$("body").data("invite",inviteData);//将数据存入缓存
				loadInvite(inviteData);
			}
		});
	});
	
	
	var meetId=$("#meetId").val();
	if(meetId){//会议已开始
		$("#meetStatus").text("会议中...");
		initWebsocket(meetId);
		changeMeetControlState();
		$("#loading").hide();
	}else{
		$("#loading").hide();
	}
}

//websocket的配置对象
local.options={
		onmessage:function(e){
			reciveResult(e);
		}
};

/*
 * 创建websocket
 * url:websocket的地址信息
 * options:websocket的相关事件触发时执行的方法
 */
function getWebSocket(url,options){
	if (!("WebSocket" in window) && !("MozWebSocket" in window)) {
		alert("对不起，您的浏览版本太低，请使用Firefox 4、Chrome 4、Opera 10.70以及Safari 5浏览器");
		closeWindow();
        return;
    }
	var ws=new WebSocket(url);
	ws.onopen=function(e){
		if(options.onopen){
			options.onopen(e);
		}
	}
	ws.onmessage=function(e){
		if(options.onmessage){
			options.onmessage(e);
		}
	}
	ws.onerror=function(e){
		if(options.onerror){
			options.onerror(e);
		}
	}
	ws.onclose=function(e){
		if(options.onclose){
			options.onclose(e);
		}
	}
	window.ws=ws;
}