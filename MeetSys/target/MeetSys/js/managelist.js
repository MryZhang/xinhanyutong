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
var jid=0;//全局变量,会议
var webSockets;
var isconn=false;
var isExist=true;//判断会议是否还存在的全局变量
var forbid=[];//禁止发言的电话
var url="ws://111.200.245.187:8080/meetting/meet_api";

function register(reqId,action,meetId){
	webSockets=new WebSocket(url+'?reqId=' + reqId + '&action=' + action + '&meetId=' + meetId);
	
	webSockets.onerror=function(event){
		setTimeout("onError()",1000);
	};
	
	webSockets.onopen=function(event){
		onOpen(event);
	};
	
	webSockets.onmessage=function(event){
		onMessage(event);
	};
	
	webSockets.onclose=function(event){
		//alert(event.data);
//		onClose(event);
		setTimeout("onError()",1000);
		/*dialog({
			title:"消息",
			content:"连接超时,会场已关闭！",
			okValue:"确定",
			ok:function(){
				updateStatus();
			}
		}).showModal();*/
		
	}
	
}

//收到消息
function onMessage(event){
	//获取消息体
	var msg=event.data;
	var result=JSON.parse(msg);
	console.log(msg);
//	alert(msg);
	switch(result.action){
	case ""://初始化
		if(result.status=="4000"){
			$("#loading").hide();
			var meetId=$("#meetId").val();
			if(meetId){
				$("#lockMeet").removeClass("l-btn-disabled");
				//如果会议已开始，每隔5秒去查询会议中电话的状态
				checkStatus();
//				getStatus(meetId);//获取会议中的电话的状态
				if(!$.cookie("isFirst")){//判断页面是否是第一次进入
					//startRecord(meetId);//开始录音
					$.cookie("isFirst",true,{expires:1,path:"/"});
				}
				showInfo("会议连接成功！");
			}
		}else if(result.status=="4001"&&result.content.indexOf("meetId不存在")!=-1){
			onClose();
		}
		break;
	case "1002"://加入会议
		//alert(msg);
		break;
	case "1003"://离开会议
		if(result.status=="4000"){
			showInfo(result.content);
		}
		break;
	case "1013"://电话状态
		var code=result.status;
		var phone=result.caller;
		var rid=$("#rid").val();
		//更新状态
//		changeStatus(rid, phone, code);
		chkStatus2(code,phone);
		break;
	case "1014":
		var content=result.content;
		if(content!="当前会场没有找到成员"){
			var arr=content;
//			var arr=JSON.parse(content);
			for(var i=0,len=arr.length;i<len;i++){
				var caller=arr[i].caller;
				var status=arr[i].status;
				chkStatus1(status,caller);
			}
		}
		break;
	case "1004":
		if(result.status=="4000"&&result.meetId){
			//改变禁言操作按钮状态
			var phone=result.caller;
			$("#inMeetTab").prev().find("div.datagrid-body>table tr td[field=phone]").each(function(){
				//alert($(this).text()+"/"+phone);
				if($(this).text()==phone){
					$(this).next().find("a.mic").attr("class","mic-off");
					return;
				}
			});
			showInfo(result.content);
			forbid.push(phone);
			//$("#muteAll").text("所有发言");
		}else if(result.status=="4001"){
			showInfo("发言操作失败！",true);
			Alert("发言操作失败！");
		}
		break;
	case "1005":
		//alert(msg);
		if(result.status=="4000"&&result.meetId){
			//改变禁言操作按钮状态
			var phone=result.caller;
			$("#inMeetTab").prev().find("div.datagrid-body>table tr td[field=phone]").each(function(){
				if($(this).text()==phone){
					$(this).next().find("a.mic-off").attr("class","mic");
					return;
				}
			});
			
			for(var i=0,len=forbid.length;i<len;i++){
				if(forbid[i]==phone){
					forbid.splice(i, 1);
				}
			}
			showInfo(result.content);
			//$("#muteAll").text("所有禁言");
		}else if(result.status=="4001"){
			showInfo("禁言操作失败！",true);
			Alert("禁言操作失败！");
		}
		break;
	case "1006":
		if(result.status=="4000"){
			showInfo("开始会场放音。");
			$("#startPlay span.l-btn-text").text("停止放音");
		}else{
			showInfo("停止放音操作失败！",true);
			Alert("停止放音操作失败！");
		}
		break;
	case "1007":
		if(result.status=="4000"){
			showInfo("停止会场放音。");
			$("#startPlay span.l-btn-text").text("开始放音");
		}else{
			showInfo("开始放音操作失败！",true);
			Alert("开始放音操作失败！");
		}
		break;
	case "1008"://开始录音
	case "1009"://会场停止录音
		var str=result.content;
		//showTip(str);
		if(result.status=="4000"){//操作成功
			if(result.action=="1008"){
				$("#startRecord span.l-btn-text").text("停止录音");
				updateRecord(1);
			}else{
				$("#startRecord span.l-btn-text").text("开始录音");
				updateRecord(0);
			}
			
		}
		break;
	case "1010"://会议结束
//		alert(msg);
		if(result.status=="4000"&&result.content.indexOf("会场已关闭")!=-1){
			updateStatus();
			isExist=false;
		}else if(result.status=="4001"){
			if(result.content.indexOf("ID不存在")!=-1){
				updateStatus();
			}else{
				$("#loading").hide();
				showInfo("会议结束失败！",true);
				Alert("会议结束失败！");
			}
		}
		//updateStatus();
		break;
	case "1016":
		if(result.status=="4000"){
			showInfo("开始会场报数。");
		}else{
			showInfo("会场报数操作失败！",true);
			Alert("会场报数操作失败！");
		}
		break;
	case "1024"://会议锁门
		$("#lockMeet").removeClass("l-btn-disabled");
		if(result.status=="4000"){
			if(result.content=="会议已锁定"){
				showInfo("会议锁门");
				$("#lockMeet span.l-btn-text").text("会议解锁");
			}else{
				showInfo("会议解锁");
				$("#lockMeet span.l-btn-text").text("会议锁门");
			}
		}else{
			showInfo("会议锁门操作失败！");
			Alert("会议锁门操作失败！");
		}
		break;
	}
	//获取要操作的tr的jq对象
	function getDom(phone){
		var dom;
		var trs=$("#table2 tr");
		for(var i=0;i<trs.length;i++){//处理中的邀请人
			var p = $(trs[i]).find("td:eq(2)").text();
			if(p==phone){
				dom=$(trs[i]);
				break;
			}
		}
		
		if(!dom){//已在会议中
			trs=$("#table1 tr");
			for(var i=0;i<trs.length;i++){
				var p=$(trs[i]).find("td:eq(2)").text();
				if(p==phone){
					dom=$(trs[i]);
					break;
				}
			}
		}
		return dom;
	}
	//定时检查操作状态并改变操作状态
	function chkStatus1(code,phone){
		//检查数据，改变联系人表中操作状态
		chck("#personalTab",phone);
		chck("#publicTab",phone);
		
		var hostNum=$("#hostNum").text();//主持人号码
		var meetStatus=$("#meetStatus").val();//会议状态
		if(code=="6003"||code=="6007"){//加入会议
			var data=$("#inMeetTab").datagrid("getRows");
			//设置邀请人数据
			var rows=$("#invitTab").datagrid("getRows");
			for(var i=0;i<rows.length;i++){
				if(phone==rows[i].phone){
					//$("#inMeetTab").datagrid("deleteRow",index);
					rows.splice(i,1);
					break;
				}
			}
			$("#invitTab").datagrid("loadData",rows);
			chckForbid();
			//显示消息
			if(code=="6007"){
				showInfo(phone+"加入会议。");
			}

			if(data){
				var index;
				var row;
				var flag=true;
				for(var i=0;i<data.length;i++){
					if(data[i].phone==phone){
						flag=false;
						break;
					}
				}
				if(flag){
					var name=getName(phone);
					var obj={name:name,phone:phone,role:"参会人"};
					if(phone==hostNum){
						obj.role="主持人";
					}
					$("#inMeetTab").datagrid("appendRow",obj);
				}
				//更新与会人数量
				var num1=$("#inMeetTab").datagrid("getRows").length;
				$("#inMeetNum").text(num1);
			}
			
		}else{//改变状态
			//设置参会人数据
			var rows=$("#inMeetTab").datagrid("getRows");
			for(var i=0;i<rows.length;i++){
				if(phone==rows[i].phone){
					//$("#inMeetTab").datagrid("deleteRow",index);
					rows.splice(i,1);
					break;
				}
			}
			$("#inMeetTab").datagrid("loadData",rows);
			chckForbid();
			
			var data=$("#invitTab").datagrid("getRows");
			var temp=true;
			for(var i=0;i<data.length;i++){
				if(data[i].phone==phone){
					temp=false;
					break;
				}
			}
			
			if(temp){
				var name=getName(phone);
				var status=getStatusByCode(code);
				var obj={name:name,phone:phone,status:status};
				$("#invitTab").datagrid("appendRow",obj);
				//$("#clearAll").removeClass("l-btn-disabled");
				$("#callAll").removeClass("l-btn-disabled");
				//更新与会人数量
				var num=$("#invitTab").datagrid("getRows").length;
				$("#waitmeet").text(num);
			}
			//alert(code+"/"+status);
			var trs=$("#invitTab").prev().find("div.datagrid-body>table tr");
			var flag=true;//标志位，判断当前号码是否已在会议中
			for(var i=0;i<trs.length;i++){
				var $tr=$(trs[i]);
				//alert($tr.find("td:eq(1)").text());
				if(phone==$tr.find("td:eq(1)").text()){
					flag=false;
					$tr.find("td:eq(2) div").text(status);
					if(code=="6001"||code=="6002"||code=="6003"){
						$tr.find("td:last a:first").attr("class","call-end");
					}else{
						$tr.find("td:last a:first").attr("class","call");
					}
					//alert($tr.find("td:last a:first").attr("class"));
					break;
				}
			}
			
		}
		
		$("#personalTab").prev().find("div.datagrid-body>table tr").each(function(){
			var num = $(this).find("td[field=phone]").text();
			if(num&&phone==num.trim()){
				$(this).find("td:last span").show();
				$(this).find("td:last a.call").hide();
				return;
			}
		});
		$("#publicTab").prev().find("div.datagrid-body>table tr").each(function(){
			var num = $(this).find("td[field=phone]").text();
			if(num&&phone==num.trim()){
				$(this).find("td:last span").show();
				$(this).find("td:last a.call").hide();
				return;
			}
		});
		
		function chck(id,phone){
			var data = $(id).treegrid("getData");
			var mid=$("#meetId").val();
			for(var i=0;i<data.length;i++){
				var children=data[i].children;
				for(var n=0;n<children.length;n++){
					var child=children[n];
					if(child.phone==phone){
						$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
							//alert($(this).find("div").text());
							if($(this).find("div").text()==phone){
								$(this).next().find("div span").hide();
								if(mid){
									$(this).next().find("div a.call").show();
								}else{
									$(this).next().find("div a.joinMeet").show();
								}
								
							}
						});
						//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
					}
				}
			}
		}
	}
	
	//检查并改变会议操作状态
	function chkStatus2(code,phone){
		//检查数据，改变联系人表中操作状态
		chck("#personalTab",phone);
		chck("#publicTab",phone);
		
		var hostNum=$("#hostNum").text();//主持人号码
		var meetStatus=$("#meetStatus").val();//会议状态
		if(code=="6003"||code=="6007"){//加入会议
			var data=$("#inMeetTab").datagrid("getRows");
			//设置邀请人数据
			var rows=$("#invitTab").datagrid("getRows");
			for(var i=0;i<rows.length;i++){
				if(phone==rows[i].phone){
					//$("#inMeetTab").datagrid("deleteRow",index);
					rows.splice(i,1);
					break;
				}
			}
			$("#invitTab").datagrid("loadData",rows);
			chckForbid();
			//显示消息
			if(code=="6007"){
				showInfo(phone+"加入会议。");
			}

			if(data){
				var index;
				var row;
				var flag=true;
				for(var i=0;i<data.length;i++){
					if(data[i].phone==phone){
						flag=false;
						break;
					}
				}
				if(flag){
					var name=getName(phone);
					var status=getStatusByCode(code);
					var obj={name:name,phone:phone,role:"参会人"};
					if(phone==hostNum){
						obj.role="主持人";
					}
					$("#inMeetTab").datagrid("appendRow",obj);
					//更新与会人数量
					var num1=$("#inMeetTab").datagrid("getRows").length;
					$("#inMeetNum").text(num1);
				}
			}
			
		}else{//改变状态
			var rows=$("#inMeetTab").datagrid("getRows");
			for(var i=0;i<rows.length;i++){
				if(phone==rows[i].phone){
					//$("#inMeetTab").datagrid("deleteRow",index);
					rows.splice(i,1);
					break;
				}
			}
			$("#inMeetTab").datagrid("loadData",rows);
			chckForbid();
			var data=$("#invitTab").datagrid("getRows");
			var temp=true;
			for(var i=0;i<data.length;i++){
				if(data[i].phone==phone){
					temp=false;
					break;
				}
			}
			var status=getStatusByCode(code);
			if(temp){
				var name=getName(phone);
				var obj={name:name,phone:phone,status:status};
				$("#invitTab").datagrid("appendRow",obj);
				//$("#clearAll").removeClass("l-btn-disabled");
				$("#callAll").removeClass("l-btn-disabled");
				//更新与会人数量
				var num=$("#invitTab").datagrid("getRows").length;
				$("#waitmeet").text(num);
			}
			var trs=$("#invitTab").prev().find("div.datagrid-body>table tr");
			var flag=true;//标志位，判断当前号码是否已在会议中
			for(var i=0;i<trs.length;i++){
				var $tr=$(trs[i]);
				//alert($tr.find("td:eq(1)").text());
				if(phone==$tr.find("td:eq(1)").text()){
					flag=false;
					$tr.find("td:eq(2) div").text(status);
					if(code=="6001"||code=="6002"||code=="6003"){
						$tr.find("td:last a:first").attr("class","call-end");
					}else{
						$tr.find("td:last a:first").attr("class","call");
					}
					//alert($tr.find("td:last a:first").attr("class"));
					break;
				}
			}
			
			switch(code){
			case "6004":
				showInfo(phone+"已挂机。");
				//更新与会人以及邀请人数量
				var num=$("#invitTab").datagrid("getRows").length;
				var num1=$("#inMeetTab").datagrid("getRows").length;
				$("#inMeetNum").text(num1);
				$("#waitmeet").text(num);
				break;
			case "6005":
				showInfo(phone+"呼叫失败。");
				break;
			case "6006":
				showInfo(phone+"拒接。");
				break;
			}
			//
			$("#personalTab").prev().find("div.datagrid-body>table tr").each(function(){
				var num = $(this).find("td[field=phone]").text();
				if(num&&phone==num.trim()){
					$(this).find("td:last span").show();
					$(this).find("td:last a.call").hide();
					return;
				}
			});
			$("#publicTab").prev().find("div.datagrid-body>table tr").each(function(){
				var num = $(this).find("td[field=phone]").text();
				if(num&&phone==num.trim()){
					$(this).find("td:last span").show();
					$(this).find("td:last a.call").hide();
					return;
				}
			});
			
			if(hostNum==phone&&meetStatus=="0"){//如果是主持人号码，呼叫成功后，呼叫所有邀请人，并改变操作项
				$("#meetStatus").val(1);
				$("#personalTab").prev().find("div.datagrid-body>table a.call").each(function(){
					//alert($(this).parent().find("span").get(0).style.display);
					if($(this).parent().find("span").get(0).style.display=="none"){
						$(this).show();
					}else{
						$(this).hide();
					}
				});
				$("#personalTab").prev().find("div.datagrid-body>table a.joinMeet").hide();
				$("#personalTab").prev().find("div.datagrid-body>table td:last span").hide();
				$("#publicTab").prev().find("div.datagrid-body>table a.call").each(function(){
					if($(this).parent().find("span").get(0).style.display=="none"){
						$(this).show();
					}else{
						$(this).hide();
					}
				});
				$("#publicTab").prev().find("div.datagrid-body>table a.joinMeet").hide();
				$("#publicTab").prev().find("div.datagrid-body>table td[field=id] span").hide();

				$("#invitTab").prev().find("div.datagrid-body>table a.call").show();
				var callers=new Array();
				for(var i=0;i<data.length;i++){
					if(data[i].phone!=hostNum){
						//alert(data[i].phone);
						callers.push({caller:data[i].phone,name:data[i].name});
					}
				}
				var mid=$("#meetId").val();
				if(callers.length>0){
					joinMeeting(mid, callers);
				}
				
			}
		}
		
		function chck(id,phone){
			var data = $(id).treegrid("getData");
			var mid=$("#meetId").val();
			for(var i=0;i<data.length;i++){
				var children=data[i].children;
				for(var n=0;n<children.length;n++){
					var child=children[n];
					if(child.phone==phone){
						$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
							//alert($(this).find("div").text());
							if($(this).find("div").text()==phone){
								$(this).next().find("div span").hide();
								if(mid){
									$(this).next().find("div a.call").show();
								}else{
									$(this).next().find("div a.joinMeet").show();
								}
								
							}
						});
						//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
					}
				}
			}
		}
	}
	//检查禁言状态
	function chckForbid(){
		$("#inMeetTab").prev().find("div.datagrid-body tr").each(function(){
			var phone=$(this).find("td[field=phone]").text().trim();
			for(var i=0,len=forbid.length;i<len;i++){
				if(phone==forbid[i]){
					$(this).find("a.mic").attr("class","mic-off");
					break;
				}
			}
		
		});
	}
	//根据电话号码获取名字
	function getName(phone){
		var name;
		//从个人联系人中查找
		var $trs1 = $("#personalTab").prev().find("div.datagrid-body>table tr");
		for(var i=0,len=$trs1.length;i<len;i++){
			var num = $($trs1[i]).find("td[field=phone]").text();
			if(num&&phone==num.trim()){
				name=$($trs1[i]).find("td[field=name]").text().trim();
				break;
			}
		}
		if(!name){
			//从公司联系人中查找
			var $trs2 = $("#publicTab").prev().find("div.datagrid-body>table tr");
			for(var i=0,len=$trs2.length;i<len;i++){
				var num = $($trs2[i]).find("td[field=phone]").text();
				if(num&&phone==num.trim()){
					name=$($trs2[i]).find("td[field=name] span.tree-title").text().trim();
					break;
				}
			}
		}
		if(!name){
			//从cookie中查找
			var contacts=$.cookie("contacts");
			if(contacts){
				contacts=JSON.parse(contacts);
				for(var i=0,len=contacts.length;i<len;i++){
					if(phone==contacts[i].phone){
						name=contacts[i].name;
						break;
					}
				}
			}
		}
		
		if(!name){
			name=phone;
		}
		return name;
	}
}
//显示消息
function showInfo(msg,err){
	if(!err){
		$("#info").prepend("<li><div class=\"left\">"+msg+"</div><div class=\"right\">"+new Date().Format("HH:mm:ss")+"</div></li>");
	}else{
		$("#info").prepend("<li style=\"color:red\"><div class=\"left\">"+msg+"</div><div class=\"right\">"+new Date().Format("HH:mm:ss")+"</div></li>");
	}
	
}
//检查会议中各个电话的连接状态
function checkStatus(){
	var meetId=$("#meetId").val();
	getStatus(meetId);
	setTimeout("checkStatus()",5000);
}

//连接事件
function onOpen(event){
	//alert("连接成功！");
}

//websocket错误事件
function onError(){
	if(isExist){
		showInfo("连接已断开！",true);
		$("#loading").show();
		initWebsocket();
	}
//	dialog({
//		title:"消息",
//		content:"连接已断开！",
//		width:200,
//		okValue:"确定",
//		ok:function(){
//			//关闭当前窗口并刷新打开该页面的窗口
//			if(window.opener){
//				window.opener.location.reload();
//			}
//			window.opener=null;
//			window.open("", "_self");
//			window.close();
//		}
//	}).showModal();
}

//websocket连接断开
function onClose(){
	if(isExist){
		isExist=false;
		dialog({
			title:"消息",
			content:"会议连接超时，已关闭<div style=\"color:red;\"><span id=\"time\">60</span>s后关闭页面</div>",
			width:200,
			okValue:"确定",
			ok:function(){
				updateStatus();
			}
		}).showModal();
		chckTime();
	}
	
	function updateStatus(){
		var rid=$("#rid").val();
		$.ajax({
			type:"get",
			url:"/phonemeeting/updateMeetStatus/"+rid,
			dataType:"json",
			success:function(data){
				//alert(data.success);
				if(data.success){
					//关闭当前窗口并刷新打开该页面的窗口
					if(window.opener){
						window.opener.location.reload();
					}
					window.opener=null;
					window.open("", "_self");
					window.close();
				}
			},error:function(err){
				console.log(err.responseText);
			}
		});
	}
}

function chckTime(){
	var time=$("#time").text();
	if(time){
		time=parseInt(time);
		if(time>0){
			time--;
			$("#time").text(time);
			setTimeout("chckTime()",1000);
		}else{
			updateStatus();
		}
	}
}
//websocket发送消息
function send(msg){
	webSockets.send(msg);
}

//状态变更通知
function statusNotify(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1013\"}";
	send(msg);
}


//根据会议ID获取会议中的电话状态
function getStatus(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1014\"}";
	send(msg);
}

//停止会场录音
function stopRecord(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1009\"}";
	send(msg);
}

//开始会场录音
function startRecord(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1008\"}";
	send(msg);
}

//开始会场放音
function startPlayback(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1006\"}";
	send(msg);
}

//停止会场放音
function stopPlayback(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1007\"}";
	send(msg);
}

//开始报数
function startCount(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1016\"}";
	send(msg);
}

//结束会议
function stopMeeting(meetId){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1010\"}";
	send(msg);
}

//加入会议
function joinMeeting(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1002\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//操作成员离开会议
function leaveMeeting(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1003\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//发起呼叫
function call(caller,called){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"costNum1\":\""+caller+"\",\"showNum1\":\""+caller+"\",\"caller\":\""+caller+"\",\"costNum2\":\""+called+"\",\"showNum2\":\""+called+"\",\"action\":\"1000\"}";
	send(msg);
}

//禁止发言
function forbidTalk(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1004\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//允许发言
function allowTalk(meetId,callers){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1005\",\"callers\":"+JSON.stringify(callers)+"}";
	send(msg);
}

//状态变更通知
function stateChangeNotify(meetId,caller){
	var reqId=getReqId();
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1013\",}";
}
//挂断
function hangup(){
	
}
//会议锁门
function lock(meetId,isLock){
	var reqId=getReqId();
	var l;
	if(isLock){
		l="3000";
	}else{
		l="3001";
	}
	var msg="{\"reqId\":\""+reqId+"\",\"meetId\":\""+meetId+"\",\"action\":\"1024\",\"lock\":\""+l+"\"}";
	send(msg);
}
//消息提示
function showTip(msg){
	dialog({
		title:"消息",
		content:msg,
		width:200,
		okValue:"确定",
		ok:function(){}
	}).showModal();
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

//根据状态ID获取状态
function getStatusByCode(code){
	var status="";
	switch(code){
	case "6000":
		status="空闲";
		break;
	case "6001":
		status="处理中";
		break;
	case "6002":
		status="振铃中";
		break;
	case "6003":
		status="应答";
		break;
	case "6004":
		status="挂机";
		break;
	case "6005":
		status="失败";
		break;
	case "6006":
		status="拒接";
		break;
	case "6007":
		status="加入会议";
		break;
	case "6008":
		status="离开会议";
		break;
	}
	return status;
}

//更新与会人员状态
function changeStatus(rid,phone,status){
	$.ajax({
		type:"post",
		url:"/phonemeeting/changeStatus",
		dataType:"json",
		data:{rid:rid,phone:phone,status:status},
		success:function(data){
			//alert(data.success);
		},error:function(err){
			console.log(err.responseText);
		}
	});
}

//更新与会人员状态
function updateAttendeStatus(id,status,tid){
	/*var $loading;
	if(tid=="table1"){//根据操作table的id来判断显示哪个加载
		$loading=$("#loadTip_1");
	}else{
		$loading=$("#loadTip_2");
	}
	$loading.show();*/
	$.ajax({
		type:"post",
		url:"/meetingmanage/updateStatus",
		dataType:"json",
		data:{"id":id,"status":status},
		success:function(data){
			if(data.success){
				//$loading.hide();
			}
			//alert(data.success);
		},error:function(err){
			console.log(err.responseText);
		}
	});
}

//会议结束，修改数据库中的会议状态
function updateStatus(){
	//清除cookie
	$.cookie("isFirst",null,{path:"/"});
	//关闭当前窗口并刷新打开该页面的窗口
	if(window.opener){
		window.opener.location.reload();
	}
	window.opener=null;
	window.open("", "_self");
	window.close();
	
	/*var rid=$("#rid").val();
	$.ajax({
		type:"get",
		url:"/phonemeeting/updateMeetStatus/"+rid,
		dataType:"json",
		success:function(data){
			//alert(data.success);
			if(data.success){
				//清除cookie
				$.cookie("isFirst",null,{path:"/"});
				//关闭当前窗口并刷新打开该页面的窗口
				if(window.opener){
					window.opener.location.reload();
				}
				window.opener=null;
				window.open("", "_self");
				window.close();
//				$("#meetId").val("");
//				$("#meetStatus").val("2");
//				$(".meeting-status strong").text("会议已结束");
			}
		},error:function(err){
			console.log(err.responseText);
		}
	});*/
//	$.ajax({
//		type:"get",
//		url:"/meetingmanage/stopMeeting",
//		dataType:"json",
//		success:function(data){
//			alert(data.success);
//			if(data.success){
//				$("#loading").show();
//				$.ajax({
//					type:"get",
//					url:"/meetinglist",
//					dataType:"html",
//					success:function(data){
//						$("#maincontent").html(data);
//					},error:function(err){
//						console.log(err.responseText);
//						alert("加载失败！");
//					}
//				});
//			}
//			
//		},error:function(err){
//			console.log(err.responseText);
//		}
//	});
}

String.prototype.trim=function()
{
     return this.replace(/(^\s*)|(\s*$)/g,"");
}


function updateRecord(isRecord){
	$.ajax({
		type:"get",
		url:"/meetingmanage/updateRecord/"+isRecord,
		dataType:"json",
		success:function(data){
			
		},error:function(err){
			console.log(err.responseText);
		}
		
	});
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
Date.prototype.Format = function(formatStr)   
{   
var str = formatStr;   
var Week = ['日','一','二','三','四','五','六'];  

str=str.replace(/yyyy|YYYY/,this.getFullYear());   
str=str.replace(/yy|YY/,(this.getYear() % 100)>9?(this.getYear() % 100).toString():'0' + (this.getYear() % 100));   

str=str.replace(/MM/,this.getMonth()>9?this.getMonth().toString():'0' + (this.getMonth()+1));   
str=str.replace(/M/g,this.getMonth());   

str=str.replace(/w|W/g,Week[this.getDay()]);   

str=str.replace(/dd|DD/,this.getDate()>9?this.getDate().toString():'0' + this.getDate());   
str=str.replace(/d|D/g,this.getDate());   

str=str.replace(/hh|HH/,this.getHours()>9?this.getHours().toString():'0' + this.getHours());   
str=str.replace(/h|H/g,this.getHours());   
str=str.replace(/mm/,this.getMinutes()>9?this.getMinutes().toString():'0' + this.getMinutes());   
str=str.replace(/m/g,this.getMinutes());   

str=str.replace(/ss|SS/,this.getSeconds()>9?this.getSeconds().toString():'0' + this.getSeconds());   
str=str.replace(/s|S/g,this.getSeconds());   

return str;   
}   