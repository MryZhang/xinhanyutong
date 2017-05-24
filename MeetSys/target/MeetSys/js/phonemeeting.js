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
//邀请对象表格显示数据
var data=[{}];
var gridObj;//
function operate(record,rowIndex,colIndex,options){
	var n=gridObj.getRecordIndexValue(record, 'phone');
	if(n){
		return "<a href='javascript:;' onclick='del("+gridObj.getRecordIndexValue(record, 'phone')+")'><i class='icon-trash'></i></a>";
	}else{
		return "&nbsp;";
	}
	
}
//删除
function del(p){
	for(var i=0;i<data.length;i++){
		if(data[i].phone==p){
			data.splice(i,1);
			if(data.length==0){
				data.push({});
				$("#deleteAll").hide();
			}
			break;
		}
	}
	refresh();
//	var d=dialog({
//		title:"提示",
//		content:"你确定要删除该行记录？",
//		okValue:"确定",
//		ok:function(){
//			
//		},
//		cancelValue:"取消",
//		cancel:function(){}
//	});	
//	d.showModal();
	
}
//刷新表格
function refresh(){
	gridObj=$.fn.bsgrid.init("gbox_gridTable",{
		localData:data,
		isProcessLockScreen:false,//数据请求加载过程中不显示显示遮罩
		pageSize:5,
		stripeRow:true,
		displayPagingToolbarOnlyMultiPages:true,//仅当多页时才显示分页工具条
		pageIncorrectTurnAlert:false//翻页不小显示无页可翻的提示
	});
}
function showTips(dom){
	var id=dom.getAttribute("id");
	var txt;
	switch(id){
	case "label_autoMute":
		txt="<div style='font-size:12px;'>如果取消勾选，所有加入会议的参会者将会默认处于不可发言的状态，可以在电话键盘上输入*6申请发言</div>";
		break;
	case "label_autoAudio":
		txt="<div style='font-size:12px;'>勾选后参会者在电话键盘按*6可以直接获得发言权，未勾选则需要等待主持人批准后方可发言</div>";
		break;
	case "label_autoCall":
		txt="<div style='font-size:12px;'>如果勾选，到了会议开始时间，会议系统将自动呼叫主持人，并在主持人接通后自动呼叫邀请名单内的所有参会者电话，把他们接入会议</div>";
		break;
	case "isfixed_help":
		txt="<div style='font-size:12px;'>固定会议室在没有参会者时也不会自动结束，会议中中文档可以长期保留</div>";
		break;
	case "label_onlyInv":
		txt="<div style='font-size:12px;'>如果勾选，只有在创建会议时邀请的成员才可以加入会议，未被邀请的参会者通过拨打接入号并输入密码的方式无法加入会议</div>";
		break;
	}
	var d=dialog({content:txt,quickClose:true,width:200,padding:10});
	d.show(dom);
	dom.onmouseleave=function(){
		d.close().remove();
	}
}
$(document).ready(function(){
	gridObj=$.fn.bsgrid.init("gbox_gridTable",{
		localData:data,
		isProcessLockScreen:false,//数据请求加载过程中不显示显示遮罩
		pageSize:5,
		stripeRow:true,
		displayPagingToolbarOnlyMultiPages:true,//仅当多页时才显示分页工具条
		pageIncorrectTurnAlert:false,//翻页不小显示无页可翻的提示
		complete:function(options,XMLHttpRequest,textStatus){
			//alert("complete:"+JSON.stringify(options));	
		},
		processUserData:function(userdata,options){
			alert(userdata);
		}
	});
	
	//删除全部
	$("#deleteAll").click(function(){
		data=[{}];
		refresh();
//		var d=dialog({
//			title:"提示",
//			content:"你确定要删除所有邀请对象？",
//			okValue:"确定",
//			ok:function(){
//				data=[{}];
//				refresh();
//			},
//			cancelValue:"取消",
//			cancel:function(){}
//		});	
//		d.showModal();
	});
	
	$(".title-obj").click(function(){
		//关闭提示框
		if(err){
			err.close().remove();
			err=null;
		}
		if($("#inviteeObjId").is(":hidden")){
			$("#inviteeObjId").show();
			$("#objImg").attr("src","/img/minus.gif");
		}else{
			$("#inviteeObjId").hide();
			$("#objImg").attr("src","/img/plus.gif");
		}
	});
	
	$("#addContact").click(function(){
		$("#addInv").show();
	});
	
	$("#addInvTel").focus(function(){
		$(this).removeClass("highlight");
		if($(this).val().trim()=="手机/区号固话号码-分机号"){
			$(this).val("");
		}
		if(err){
			err.close().remove();
			err=null;
		}
	});
	
	$("#addInvTel").blur(function(){
		if($(this).val().trim()=="手机/区号固话号码-分机号"||$(this).val().trim()==""){
			$(this).val("手机/区号固话号码-分机号");
		}
	});
	
	//手动添加邀请对象
	$("#addTmpInv").click(function(){
		var flag=true;
		var txt;
		var num=$("#addInvTel").val().trim();
		var name,phone;
		if(num=="手机/区号固话号码-分机号"||num==""){
			$("#addInvTel").val("");
			txt="<div style='color:#FF0000;font-size:12px;'>请填写电话号码</div>";
			flag=false;
		}else if(!/((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/.test(num)){
			flag=false;
			txt="<div style='color:#FF0000;font-size:12px;'>格式错误,例：15088888888或0019168888888-123</div>";
		}else if(num==$("#hostNum").val().trim()){
			flag=false;
			txt="<div style='color:#FF0000;font-size:12px;'>成员号码不能与主持人相同</div>";
		}else{
			if($("#addInvName").val().trim()==""){
				name=$("#addInvTel").val();
			}else{
				name=$("#addInvName").val();
			}
			phone=$("#addInvTel").val();
		}
		//错误提示
		if(!flag&&!err){
			$("#addInvTel").addClass("highlight");
			err=dialog({align:"top",content:txt,width:150,padding:6});
			err.show(document.getElementById("addInvTel")); 
		}
		if(flag){
			//清除空元素
			if(!data||!data[0].phone){
				data.splice(0,1);
				//alert("ok");
			}
			var temp=true;
			if(data.length>0){
				for(var i=0;i<data.length;i++){
					if(data[i].phone==phone){
						temp=false;
						var d = dialog({
						    title: '提示',
						    content: '邀请对象【'+phone+'】已存在',
						    okValue: '确定',
						    ok: function () {}
						});
						d.showModal();
					}
				}
			}
			if(temp){
				var typeid=2;
				typename="参会者";
				var o={"name":name,"phone":phone,"typeid":typeid,"typename":typename};
				data.push(o);
				refresh();
				$("#addInvTel").val("").focus();
				$("#deleteAll").show();
			}
		}
		
		
	});
	
	
	$("#subject,#hostNum,#meetingNum").focus(function(){
		var id=$(this).attr("id");
		if(id=="subject"||id=="meetingNum"){
			$(this).removeClass("highlight").next().text("").addClass("hide");
		}else if(id=="hostNum"){
			$(this).removeClass("highlight");
			$("#hosttip").attr("class","pstnhnumtip").text("格式：手机/区号固话号码-分机号");
		}
	});
	//验证输入
	$("input.intervalTxt").keyup(function(){
		var val=this.value;
		var id=$(this).attr("id");
		switch(id){
		case "dayInterval":
			if(!/^[1-9]\d*$/.test(val)||parseInt(val)>365){
				$(this).next().attr("class","pstnhnumtip error wrong").text("格式不正确");
			}else{
				$(this).next().attr("class","wrong hide").text("");
			}
			break;
		case "weekInterval":
			if(!/^[1-9]\d*$/.test(val)||parseInt(val)>52){
				$(this).next().attr("class","pstnhnumtip error wrong").text("格式不正确");
			}else{
				$(this).next().attr("class","wrong hide").text("");
			}
			break;
		case "monthInterval":
			if(!/^[1-9]\d*$/.test(val)||parseInt(val)>31){
				$(this).next().attr("class","pstnhnumtip error wrong").text("格式不正确");
			}else{
				$(this).next().attr("class","wrong hide").text("");
			}
			break;
		}
	});
	//修改会议
	$("#upBtn").click(function(){
		var flag=true;
		var subject=$("#subject").val();
		var hostNum=$("#hostNum").val();
		var obj={};
		//var num=$("#meetingNum").val().trim();
		if(subject.trim()==""){
			$("#subject").addClass("highlight").next().text("主题不能为空").removeClass("hide");
			flag=false;
		}else{
			obj.subject=subject;
		}
		if(hostNum.trim()==""){
			$("#hostNum").addClass("highlight");
			$("#hosttip").attr("class","pstnhnumtip error wrong").text("还未输入电话号码");
			flag=false;
		}else if(!/((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/.test(hostNum.trim())){
			$("#hostNum").addClass("highlight");
			$("#hosttip").attr("class","pstnhnumtip error wrong").text("手机号码格式不正确");
			flag=false;
		}else{
			obj.hostNum=hostNum;
		}
		var cycleType=$("input.input_radio_check:checked").val();
		obj.type=cycleType;
		
		if(flag){
			obj.id=$("#id").val();
			var mtype=$("#mtype").val();
			obj.mtype=mtype;
			if(mtype=="3"){
				obj.hostPwd=$("#hostPwd").val();
				obj.listenerPwd=$("#listenerPwd").val();
			}else{
				obj.startTime=$("#startTime").val();
				if(obj.type=="0"){
					obj.startTime=$("#startTime").val();
				}else{
					obj.startTime=$("#stime").val();
				}
				if(data&&data.length>0&&data[0].phone){//邀请人
					obj.contacts=JSON.stringify(data);
				}
			}
			var hostName=$("#hostName").val();
			if(hostName){
				obj.hostName=hostName;
			}else{
				obj.hostName=hostNum;
			}
			obj.isRecord=$("input[name=isRecord]:checked").val();
			
			$.ajax({
				type:"post",
				url:"/ordermeet/update",
				dataType:"json",
				data:obj,
				success:function(data){
					if(data.success){
						$("#loading").show();
						var oid = $("#id").val();
						$.ajax({
							type:"post",
							url:"/meetlist/getDetail",
							dateType:"html",
							data:{"id":oid,"mtype":mtype},
							success:function(data){
								$("#maincontent").html(data);
								$("#loading").hide();
							},error:function(err){
								console.log(err.responseText);
							}
						});
					}else{
						dialog({
							title:"消息",
							content:"修改失败！",
							width:180,
							okValue:"确定",
							ok:function(){}
						}).showModal();
					}
				},error:function(err){
					console.log(err.responseText);
				}
			});
		}
	});
	//添加预约会议
	$("#okBtn").click(function(){
		var flag=true;
		var subject=$("#subject").val();
		var hostNum=$("#hostNum").val();
		var obj={};
		//var num=$("#meetingNum").val().trim();
		if(subject.trim()==""){
			$("#subject").addClass("highlight").next().text("主题不能为空").removeClass("hide");
			flag=false;
		}else{
			obj.subject=subject;
		}
		if(hostNum.trim()==""){
			$("#hostNum").addClass("highlight");
			$("#hosttip").attr("class","pstnhnumtip error wrong").text("还未输入电话号码");
			flag=false;
		}else if(!/((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/.test(hostNum.trim())){
			$("#hostNum").addClass("highlight");
			$("#hosttip").attr("class","pstnhnumtip error wrong").text("手机号码格式不正确");
			flag=false;
		}else{
			obj.hostNum=hostNum;
		}
//		if(num==""){
//			$("#meetingNum").addClass("highlight").next().text("会议方数不能为空").removeClass("hide");
//			flag=false;
//		}else if(!/^[0-9]*[1-9][0-9]*/.test(num)||num>20||num<1){
//			$("#meetingNum").addClass("highlight").next().text("会议方数只能为小于20的正整数").removeClass("hide");
//			flag=false;
//		}
		//检查重复类型
		var cycleType=$("input.input_radio_check:checked").val();
		obj.type=cycleType;
		var chked;
		switch(cycleType){
		case "0":
			obj.startTime=$("#startTime").val();
			break;
		case "1"://验证天的周期
			chked=$("input[name=dayChoice]:checked").val();
			obj.startTime=$("#stime").val();
			if(chked=="1"){
				var val=$("#dayInterval").val();
				if(!/^[1-9]\d*$/.test(val)||parseInt(val)>365){
					$("#dayInterval").next().attr("class","pstnhnumtip error wrong").text("格式不正确");
					flag=false;
				}else{
					obj.interval=val;
				}
			}else{
				obj.interval="workday";
			}
			break;
		case "2"://验证星期的周期
			var val=$("#weekInterval").val();
			obj.startTime=$("#stime").val();
			if(!/^[1-9]\d*$/.test(val)||parseInt(val)>52){
				$("#weekInterval").next().attr("class","pstnhnumtip error wrong").text("格式不正确");
				flag=false;
			}else{
				var wkdays=$("input[name=weekday]:checked");
				if(wkdays&&wkdays.length>0){
					var wkds="";
					for(var i=0,len=wkdays.length;i<len;i++){
						wkds+=wkdays[i].value+",";
					}
					wkds=wkds.substr(0,wkds.length-1);
					obj.orderNum=wkds;
					obj.interval=val;
				}else{
					$("#weekInterval").next().attr("class","pstnhnumtip error wrong").text("请选择具体的日期");
					flag=false;
				}
			}
			break;
		case "3"://验证月的周期
			chked=$("input[name=monthChoice]:checked").val();
			obj.startTime=$("#stime").val();
			if(chked=="1"){
				var val=$("#monthInterval").val();
				if(!/^[1-9]\d*$/.test(val)||parseInt(val)>31){
					$("#monthInterval").next().attr("class","pstnhnumtip error wrong").text("格式不正确");
					flag=false;
				}else{
					obj.interval=val;
				}
			}else{
				var weekNum=$("#weekNum").val();
				var weekday=$("#weekday").val();
				obj.orderNum=weekday;
				obj.interval=weekNum;
			}
			break;
		case "4":
			var hostPwd=$("#hostPwd").val();
			var listenerPwd=$("#listenerPwd").val();
			obj.hostPwd=hostPwd;
			obj.listenerPwd=listenerPwd;
			break;
		}
//		return;
		if(flag){
//			if(obj.type!="0"){
//				obj.endTime=$("#endTime").val();
//			}
//			startTime=startTime.substr(startTime.indexOf("：")+1).trim();
//			var endTime=$("#finishTime").val();
//			var hostPwd=$("#hostPwd").val();
//			var listenerPwd=$("#listenerPwd").val();
//			var subject=$("#subject").val();
			//var obj={"caller":hostNum,"startTime":startTime,"subject":subject,"isRecord":isRecord};
			if(data&&data.length>0&&data[0].phone&&obj.type!="4"){//邀请人
				obj.contacts=JSON.stringify(data);
			}
			var hostName=$("#hostName").val();
			if(hostName){
				obj.hostName=hostName;
			}else{
				obj.hostName=hostNum;
			}
			obj.isRecord=$("input[name=isRecord]:checked").val();
			$.ajax({
				type:"post",
				url:"/ordermeet/startOrderMeet",
				dataType:"json",
				data:obj,
				success:function(data){
					$("#loading").show();
					$.ajax({
						type:"get",
						url:"/ordermeet/showResult/"+data.success,
						data:obj,
						success:function(data){
							$("#maincontent").html(data);
						},error:function(err){
							console.log(err.responseText);
						}
					}).done(function(){
						$("#loading").hide();
					});
				},error:function(err){
					console.log(err.responseText);
				}
			});
		}
	});
	
});

//检验周期输入
function chkInterval(dom){
	
}

function showManage(mid){
	$("#loading").show();
	$.ajax({
		type:"get",
		url:"/meetingmanage/"+mid,
		dataType:"html",
		success:function(data){
			if(data.unlogin){
				location.href="/login";
			}
			$("#loading").hide();
			//
			$("li.lefttree_link").each(function(){
				$(this).attr("class","lefttree_link tab_style");
			});
			$("#item_1").attr("class","lefttree_link current_tab");
			$("#maincontent").html(data);
		},
		error:function(err){
			alert(err.responseText);
		}
	});
}