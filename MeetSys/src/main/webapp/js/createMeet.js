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
$(document).ready(function(){
	showHeight();
	//初始化操作
	//init();
	var isonresult={flag1:false,flag2:false};//判断鼠标是否在搜索结果上的标志位
	$("#searchContact").next().find("input[type=text]").on("blur",function(){
		if(!isonresult.flag1){
			$("#contactSearch").hide();
		}
	});
	$("#inmeetSearch").next().find("input[type=text]").on("blur",function(){
		if(!isonresult.flag2){
			$("#inMeetResult").hide();
		}
	});
	//搜索会议中的联系人
	$("#inmeetSearch").next().find("input[type=text]").on("keyup",function(){
		isonresult.flag2=false;
		var search=this.value.trim()
		if(!search){
			$("#inMeetResult").hide();
			return;
		}
		var arr=[];//存储搜索得到的结果
		//
		var data=$("#inMeetTab").datagrid("getRows");
		for(var i=0;i<data.length;i++){
			if(data[i].name.indexOf(search)!=-1||data[i].phone.indexOf(search)!=-1){
				arr.push({name:data[i].name,phone:data[i].phone,id:data[i].id});
			}
		}
		if(arr.length>0){
			var result="";
			for(var i=0;i<arr.length;i++){
				result+="<li onclick=\"getResult(this)\" data-id=\""+arr[i].id+"\">"+arr[i].name+"("+arr[i].phone+")"+"</li>";
			}
			$("#inMeetResult").html(result).show();
		}else{
			$("#inMeetResult").html("<li >无搜索结果...</li>").show();
		}
		
	});
	$("#contactSearch").hover(function(){isonresult.flag1=true;},function(){isonresult.flag2=false;});
	$("#inMeetResult").hover(function(){isonresult.flag2=true;},function(){isonresult.flag2=false;});
	
	//搜索通信录中联系人
	$("#searchContact").next().find("input[type=text]").on("keyup",function(){
		isonresult.flag1=false;
		var search=this.value.trim();
		if(!search){
			$("#contactSearch").hide();
			return;
		}
		var arr=new Array();//搜索到的数据
		//搜索个人联系人中的数据
		var data=$("#personalTab").treegrid("getData");
		searchData(data,arr,"个人通信录");
		 //搜索公共联系人中的数据
		 data=$("#publicTab").treegrid("getData");
		 searchData(data,arr,"公共通信录");
		 //搜索会议历史记录
//		 data=$("#recordTab").treegrid("getData");
//		 searchData(data,arr,"会议历史记录");
		 
		 //显示搜索结果
		 var result="";
		 for(var i=0;i<arr.length;i++){
			 result+="<li onclick=\"searfn(this)\" data-val=\""+arr[i].id+"\" data-type=\""+arr[i].type+"\">"+arr[i].name+"("+arr[i].phone+")——"+arr[i].type+"</li>";
		 }
		 if(result){
			 $("#contactSearch").html(result).show();
		 }else{
			 result+="<li>无搜索结果...</li>";
			 $("#contactSearch").html(result).show();
		 }
		 
		 //搜索
		 function searchData(data,arr,type){
			 for(var i=0;i<data.length;i++){
				 var children=data[i].children;
				 //alert(children.length);
				 for(var n=0;n<children.length;n++){
					 var child=children[n];
					 if(child.name.indexOf(search)!=-1||child.phone.indexOf(search)!=-1){
						 var flag=false;
						 for(var k=0;k<arr.length;k++){
							 if(arr[k].name==child.name&&arr[k].phone==child.phone){
								 flag=true;
								 break;
							 }
						 }
						 if(flag){
							 return;
						 }
						 arr.push({id:child.id,name:child.name,phone:child.phone,type:type});
					 }
				 }
			 }
		 }
	});
	
	//加入会议
	$("#joinMeetId").click(function(){
		var chked = $("#mytab").find("input:checked");
		//alert(chked.length);
		var callers=[];
		var hostNum=$("#hostNum").text();
		if(chked.length>0){
			for(var i=0;i<chked.length;i++){
				var name=$(chked[i]).data("name");
				var phone=$(chked[i]).data("phone");
				if(name&&phone){
					var flag=true;
					//检测该号码是否已在邀请人中
					var rows=$("#invitTab").datagrid("getRows");
					for(var n=0;n<rows.length;n++){
						if(rows[n].phone==phone){
							flag=false;
							break;
						}
					}
					//该号码是否已加入会议
					rows=$("#inMeetTab").datagrid("getRows");
					for(var k=0,len=rows.length;k<rows.len;k++){
						if(rows[k].phone==phone){
							flag=false;
							break;
						}
					}
					//alert(name+"\t"+phone);
				
					if(flag){
						var rid=$("#rid").val();
						var obj = {name:name,phone:phone,rid:rid}
						
						if(hostNum==phone){
							obj.type=1;
						}else{
							obj.type=2;
						}
						callers.push(obj);
					}
					
				}
				chked[i].checked=false;
			}
			//alert(callers.length);
			if(callers.length>0){
				//改变添加的联系人的操作状态
				var str=JSON.stringify(callers);
				$.ajax({
					type:"post",
					url:"/phonemeeting/addChecked",
					dataType:"json",
					data:{callers:str},
					success:function(result){
						if(result.success){
							//invite.push({name:name,phone:phone,status:"等待呼叫",id:id});
							callers=result.list;
							//alert(callers.length);
							var data=[];
							for(var i=0;i<callers.length;i++){
								var caller=callers[i];
								$("#invitTab").datagrid("appendRow",{
									name:caller.name,
									phone:caller.phone,
									status:"等待呼叫",
									id:caller.id
								});
								chck("#personalTab",caller.phone);
								chck("#publicTab",caller.phone);
//								chck("#recordTab",caller.phone);
								data.push({val:caller.name,text:caller.name+"("+caller.phone+")"});
							}
							//将新加入的邀请人数据添加到选择主持人的下拉列表
							loadHost(data);
							//更新显示数量
							$("#waitmeet").text(cdata.length);
						}else{
							Alert("加入会议失败！");
						}
					},error:function(err){
						console.log(err.responseText);
						alert("操作失败！");
					}
				});
			}
		}else{
			dialog({
				title:"消息",
				content:"当前页面未选中任何联系人",
				okValue:"确定",
				ok:function(){}
			}).showModal();
		}
		
		function chck(id,phone){
			var data = $(id).treegrid("getData");
			for(var i=0;i<data.length;i++){
				var children=data[i].children;
				for(var n=0;n<children.length;n++){
					var child=children[n];
					if(child.phone==phone){
						$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
							//alert($(this).find("div").text());
							if($(this).find("div").text()==phone){
								$(this).next().find("div span").show();
								$(this).next().find("div a").hide();
							}
						});
						//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
					}
				}
			}
		}
	});
	
	//手动添加邀请人
	$("#handadd").click(function(){
		var name=$("#name").val().trim();
		var phone=$("#phone").val().trim();
		var flag=true;
		var isPhone = /^([0-9]{3,4})?[0-9]{7,8}(P[0-9]{4})?$/;
		var isMob=/^1[3|4|5|7|8]\d{9}$/;
		if(phone.trim()==""){
			//showTip("电话号码不能为空！");
			Alert("电话号码不能为空！");
			flag=false;
		}else if(!isMob.test(phone)&&!isPhone.test(phone)){
			Alert("电话号码格式有误！");
			flag=false;
		}
		if(flag){
			var invite=$("#invitTab").datagrid("getRows");
			for(var i=0,len=invite.length;i<len;i++){
				if(invite[i].phone==phone){
					Alert(phone+"已在邀请人中！");
					flag=false;
					break;
				}
			}
			var inmeet=$("#inMeetTab").datagrid("getRows");
			for(var i=0,len=inmeet.length;i<len;i++){
				if(inmeet[i].phone==phone){
					Alert(phone+"已在邀请人中！");
					flag=false;
					break;
				}
			}
			
			if(flag){
				var rid=$("#rid").val();
				//$("#phone").val("");
				var obj={name:phone,phone:phone,rid:rid};
				if(name){
					obj.name=name;
					//将联系人信息存入cookie中
					var contacts = $.cookie("contacts");
					if(contacts){
						contacts=JSON.parse(contacts);
						for(var i=0,len=contacts.length;i<len;i++){
							if(contacts[i].phone==phone){
								flag=false;
								break;
							}
						}
						if(flag){
							contacts.push({name:name,phone:phone});
						}
					}else{
						contacts=[{name:name,phone:phone}];
					}
					contacts=JSON.stringify(contacts);
					$.cookie("contacts",contacts,{path:"/",expires:1});
				}
				//改变添加的联系人的操作状态
				chck("#personalTab",phone);
				chck("#publicTab",phone);
//				chck("#recordTab",phone);
				//invite.push({name:row.name,phone:row.phone,status:"等待呼叫",id:id});
				//alert(row.name+"/"+row.phone+"/"+result.id);
				$("#invitTab").datagrid("appendRow",{
					name:obj.name,
					phone:obj.phone,
					status:"等待呼叫"
				});
				//$("#clearAll").removeClass("l-btn-disabled");
				$("#callAll").removeClass("l-btn-disabled");
				var data=[];
				data.push({val:obj.phone,text:obj.name+"("+obj.phone+")"});
				loadData(data);

				//更新与会人以及邀请人数量
				var num=$("#invitTab").datagrid("getRows").length;
				var num1=$("#inMeetTab").datagrid("getRows").length;
				$("#inMeetNum").text(num1);
				$("#waitmeet").text(num);
				
				var meetId=$("#meetId").val();
				if(meetId){//如果会议已开始，则直接呼叫
//					alert(row.id);
					var callers=[{caller:phone,name:obj.name}];
					joinMeeting(meetId, callers);
				}
				/*$.ajax({
					type:"post",
					url:"/phonemeeting/addInvite",
					dataType:"json",
					data:obj,
					success:function(result){
						if(result.success){
							
						}else{
							Alert("添加邀请人失败！");
						}
					},error:function(err){
						console.log(err.responseText);
						Alert("添加邀请人失败！");
					}
				});*/
				
			}
			
		}
		function chck(id,phone){
			var data = $(id).treegrid("getData");
			for(var i=0;i<data.length;i++){
				var children=data[i].children;
				for(var n=0;n<children.length;n++){
					var child=children[n];
					if(child.phone==phone){
						$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
							//alert($(this).find("div").text());
							if($(this).find("div").text()==phone){
								$(this).next().find("div span").show();
								$(this).next().find("div a").hide();
							}
						});
						//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
					}
				}
			}
		}
	});
	
	
	//控制会议只能输入正整数
	chknum();
	
	
	//开始呼叫，创建会议
	$("#createMeet").click(function(){
		createMeet();
	});
	
	//呼叫全部
	$("#callAll").click(function(){
		var rows=$("#invitTab").datagrid("getRows");
		var callers=[];
		var meetId=$("#meetId").val();
		for(var i=0;i<rows.length;i++){
			callers.push({caller:rows[i].phone,name:rows[i].name});
		}
		if(callers.length>0){
			$("#callAll").addClass("l-btn-disabled");
			joinMeeting(meetId, callers);
		}
	});
	
	//清除全部
	$("#clearAll").click(function(){
		var rows=$("#invitTab").datagrid("getRows");
		var ids="";
		var meetId=$("#meetId").val();
		var callers=[];
		for(var i=0;i<rows.length;i++){
			ids+=rows[i].id+",";
			var status=rows[i].status;
			var phone=rows[i].phone;
			//如果状态处理呼叫中，先挂机
			if(status=="呼叫中..."||status=="振铃中"||status=="处理中"){
				callers.push({caller:phone});
			}
			chck("#personalTab",phone);
			chck("#publicTab",phone);
		}
		ids=ids.substr(0,ids.length-1);
		if(callers.length>0){
			leaveMeeting(meetId, callers);
		}
		$("#invitTab").datagrid("loadData",[]);
		//$("#clearAll").addClass("l-btn-disabled");
		//alert(ids);
		/*$.ajax({
			type:"post",
			url:"/phonemeeting/clearAll",
			dataType:"json",
			data:{ids:ids},
			success:function(data){
				if(data.success){
					//改变联系操作状态，并清空邀请人数据
					for(var a=0;a<rows.length;a++){
						var phone=rows[a].phone;
						
//						chck("#recordTab",phone);
					}
					$("#invitTab").datagrid("loadData",[]);
				}else{
					Alert("清空全部操作失败！");
				}
			},error:function(err){
				console.log(err.responseText);
				Alert("清空全部操作失败！");
			}
		});*/
		
		function chck(id,phone){
			var data = $(id).treegrid("getData");
			for(var i=0;i<data.length;i++){
				var children=data[i].children;
				for(var n=0;n<children.length;n++){
					var child=children[n];
					if(child.phone==phone){
						$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
							//alert($(this).find("div").text());
							if($(this).find("div").text()==phone){
								$(this).next().find("div span").hide();
								$(this).next().find("div a.call").show();
							}
						});
						//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
					}
				}
			}
		}
	});
	
	//全体静音
	$("#slienceBtn").click(function(){
		//alert($(this).text());
		var rows=$("#inMeetTab").datagrid("getRows");
		var callers=[];
		var hostNum=$("#hostNum").text();
		for(var i=0;i<rows.length;i++){
			if(rows[i].phone!=hostNum){//排除掉主持人
				callers.push({caller:rows[i].phone});
			}
			
		}
		var meetId=$("#meetId").val();
		if($(this).text().trim()=="全体静音"){//全体静音
			if(callers.length>0){
				forbidTalk(meetId, callers);
				$(this).find("span.l-btn-text").text("全体发言");
				$("#inMeetTab").prev().find("div.datagrid-body>table tr").each(function(){
					var phone = $(this).find("td[field=phone]").text();
					var hostPhone=$("hostNum").text();
					if(phone!=hostPhone){
						$(this).find("a.mic").attr("class","mic-off");
					}
				});
			}
		}else{
			if(callers.length>0){
				allowTalk(meetId, callers);
				$(this).find("span.l-btn-text").text("全体静音");
				$("#inMeetTab").prev().find("div.datagrid-body>table a.mic-off").attr("class","mic");
			}
		}
	});
	
	//背景音乐
	$("#playBtn").click(function(){
		var cls=$(this).find("i").attr("class");
		var meetId=$("#meetId").val();
		var count=$("#inMeetTab").datagrid("getRows").length;
		if(meetId){
			if(cls=="icon icon-start"&&count>0){//开始播放
				startPlayback(meetId);
				$(this).find("i").attr("class","icon icon-stop");
			}else{//停止播放
				stopPlayback(meetId);
				$(this).find("i").attr("class","icon icon-start");
			}
		}
	});
	
	//录音
	$("#recordBtn").click(function(){
		var meetId=$("#meetId").val();
		var count=$("#inMeetTab").datagrid("getRows").length;
		if(meetId){
			if($(this).text().trim()=="开始录音"||count>0){
				startRecord(meetId);
				$(this).find("i").attr("class","icon icon-stop");
				var rid=$("#rid").val();
				//更新数据库记录
				$.ajax({
					type:"get",
					url:"/phonemeeting/updateRecord/"+rid,
					dataType:"json",
					success:function(data){
						
					},error:function(err){
						console.log(err.responseText);
					}
				});
			}else{
				stopRecord(meetId);
				$(this).find("i").attr("class","icon icon-start");
			}
		}
		
	});
	
	//开始和结束会议
	$("#meetCtrBtn").click(function(){
		if($(this).text().trim()=="开始会议"){
			createMeet();
		}else{//结束会议
			var meetId=$("#meetId").val();
			dialog({
				title:"消息",
				content:"是否结束会议?",
				width:200,
				okValue:"确定",
				ok:function(){
					$("#loading").show();
					closeMeet(meetId);
				},
				cancelValue:"取消",
				cancel:function(){$("#loading").hide()}
			}).showModal();
		}
	});
	//五分钟会议中没有人，则提示关闭会议
//	setTimeout("checkMeet()", 1000);
	
	//创建备忘录
	$("#createRemark").click(function(){
		var rid=$("#rid").val();
		$.ajax({
			type:"get",
			url:"/phonemeeting/getRemark/"+rid,
			dataType:"text",
			success:function(data){
				var content="<textarea id=\"remark\" rows=\"20\" cols=\"80\" placeholder=\"1000字以内\" onkeyup=\"if(this.value.length>1000){this.value=this.value.substr(0,1000);}\">"+data+"</textarea>"
				dialog({
					title:"添加备忘录",
					content:content,
					okValue:"确定",
					ok:function(){
						var text=$("#remark").val();
						$.ajax({
							type:"post",
							url:"/phonemeeting/updateRemark",
							dataType:"json",
							data:{rid:rid,txt:text},
							success:function(data){
								if(data.success){
									showInfo("添加备注成功。")
								}
							},error:function(err){
								console.log(err.responseText);
							}
						});
					},
					cancelValue:"取消",
					cancel:function(){}
				}).showModal();
			},error:function(err){
				console.log(err.responseText);
			}
		});
		
		
	});
	
	//会议锁门
	$("#lockMeet").click(function(){
		$(this).addClass("l-btn-disabled");
		var meetId=$("#meetId").val();
		var isLock;
		if($(this).text()=="会议锁门"){
			isLock=true;
		}else{
			isLock=false;
		}
		lock(meetId,isLock);
	});
});
//全局变量
//邀请的参会人
var invite=new Array();
//选择主持人的下拉列表数据
var cdata=new Array();
//会议中
var inmeet=new Array();
var id=0;//邀请人id
var t={t1:0,t2:60};//会议中没有人的总时间
var meetId;

//根据页面大小调整高度
function showHeight(){
	//计算tabs的高度
	var height1=$("#mytab").parent().height()-$("#contactHead").outerHeight(true)-$("#searchDv").outerHeight(true)-$("#joinDv").outerHeight(true);
	$("#mytab").tabs("resize",{height:height1});
	//设定invitTab的高度
	var height2=$("#invitTab").closest("div#invitDv").height()-$("#invitHead").outerHeight(true)-$("#addDv").outerHeight(true);
	//alert($("#invitTab").closest("div#invitDv").height()+"/"+$("#invitHead").outerHeight(true));
	if($("#selectDv").is(":hidden")){
		height2=height2-$("#operaDv").outerHeight(true);
	}else{
		height2=height2-$("#selectDv").outerHeight(true);
	}
	$("#invitTab").datagrid("resize",{height:height2});
	//设定inMeetTab的高度
	var height3=$("#inMeetTab").closest("div#inMeetDv").height()-$("#inMeetHead").outerHeight(true);
	$("#inMeetTab").datagrid("resize",{height:height3});
	
	//设定消息的高度
	var height4=$("#info").parent().height()-$("#info").prev().outerHeight(true);
	document.getElementById("info").style.height=height4+"px";
}

//五分钟会议中没有人，则提示关闭会议
function checkMeet(){
	var count=$("#inMeetTab").datagrid("getRows").length;//获取会议中的人数
	if(!count&&count==0&&$("#loading").is(":hidden")){
		t.t1++;
		if(t.t1>=180){
			if(!meetId){
				meetId=$("#meetId").val();
			}
			dialog({
				title:"消息",
				content:"会议中没有人，是否关闭会议?<br><span style=\"color:red\">剩下 <span id=\"times\" >60</span>s 自动关闭。</span>",
				okValue:"确定",
				ok:function(){
					closeMeet(meetId);
				},
				cancelValue:"取消",
				cancel:function(){
					t.t1=0;
//					$("#times").text("2");//防止会议结束
					t.t2=60;
					setTimeout("checkMeet()", 1000);
				}
			}).showModal();
			setTimeout("checkTime()", 1000);
		}else{
			setTimeout("checkMeet()", 1000);
			/*if(!meetId){
				meetId=$("#meetId").val();
			}
			closeMeet(meetId);*/
		}
		
	}else{
		t=0;
	}
	
}
function closeMeet(meetId){
	if(meetId){
		stopMeeting(meetId);
	}else{
		var rid=$("#rid").val();
		$.ajax({
			type:"get",
			url:"/phonemeeting/updateMeetStatus/"+rid,
			dataType:"json",
			success:function(data){
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

function checkTime(meetId){
//	var times=parseInt($("#times").text());
//	alert(times);
	if(t.t2>1){
		t.t2--;
		$("#times").text(t.t2);
		setTimeout("checkTime("+meetId+")", 1000);
	}else{
		closeMeet(meetId);
	}
}
//呼叫主持人并开始会议
function createMeet(){
	var phone = $("#selectHost option:selected").val();
	var name=$("#selectHost optiion:selected").text();
	name=name.substring(0,name.indexOf("("));
	if(phone==""){
		Alert("请选择主持人！");
	}else{
		$("#createMeet").addClass("l-btn-disabled");
		$("#hostNum").text(phone);
		$("#hostName").text(name);
		var subject=$("#meetSubject").val();
		if(!subject){
			subject=$("#subject").text();
		}
		//var meetNum=$("#meetnum").val();
		var hostPwd=$("#hostPwd").text();
		var listenerPwd=$("#listenerPwd").text();
		var rid=$("#rid").val();
		var isRecord=$("#isRecord").get(0).checked;
		var obj={hostNum:phone,subject:subject,hostName:name,rid:rid,isRecord:isRecord};
		$.ajax({
			type:"post",
			url:"/phonemeeting/createmeet",
			dataType:"json",
			data:obj,
			success:function(data){
				//alert(data.success);
				if(data.success){
					//判断是否需要录音
					/*if($("#isRecord").get(0).checked){
						$.ajax({
							type:"post",
							url:"/phonemeeting/createmeet",
							dataType:"json",
							data:{meetId:data.meetId},
							success:function(data){
								
							},error:function(err){
								console.log(err.responseText);
							}
						});
					}*/
					$("#isRecord").attr("disabled","disabled");
					$("#meetCtrBtn").find("span.l-btn-text").text("结束会议");
					$("#meetId").val(data.meetId);
					$(".meeting-status strong").text("会议中...");
					$("#selectDv").hide();
					$("#operaDv").show();
					$("#invitTab").prev().find("div.datagrid-body>table a.call").show();
					var data=$("#invitTab").datagrid("getData");
					var index;
					for(var i=0;i<data.length;i++){
						if(phone==data[i].phone){
							index=i;
							break;
						}
					}
					$("#invitTab").prev().find("div.datagrid-body>table tr:eq("+i+") td:last a.call").attr("class","call-end");
					//updateInvite(data);
					initWebsocket();
				}else{
					$("#createMeet").removeClass("l-btn-disabled");
					Alert("呼叫主持人失败！");
				}
			},error:function(err){
				$("#createMeet").removeClass("l-btn-disabled");
				console.log(err.responseText);
				Alert("呼叫主持人失败！");
			}
		});
	}
}
//获取与会人
function updateInvite(data){
	
	$.ajax({
		type:"get",
		url:"/phonemeeting/getAttendees",
		dataType:"json",
		success:function(d){
			for(var i=0;i<d.length;i++){
				for(var n=0;n<data.length;n++){
					if(d[i].phone==data[n].phone){
						$("#invitTab").datagrid("updateRow",{
							index:n,
							row:{
								id:d.id,
								name:d.name,
								phone:d.phone,
								status:"等待呼叫"
							}
						});
					}
				}
			}
		},error:function(err){
			console.log(err.responseText);
		}
	});
}

//初始化websocket
function initWebsocket(){
	var reqId=getReqId();
	var meetId=$("#meetId").val();
	//alert(reqId+"\t"+meetId);
	//目前接口未完善，所以action为空字符
	register(reqId,"",meetId);
}

function getResult(dom){
	var id = $(dom).data("id");
	var index=$("#inMeetTab").datagrid("getRowIndex",id);
	$("#inMeetResult").hide();
	$("#inMeetTab").datagrid("clearSelections");
	$("#inMeetTab").datagrid("selectRow",index);
}

function chknum(){
	var txt = $("#meetnum").next().find("input[type=text]").get(0);
	if(txt){
		txt.onkeyup=function(){
			if(this.value.length==1){
				this.value=this.value.replace(/[^1-9]/g,'');
			}else{
				this.value=this.value.replace(/\D/g,'');
			}
		}
		txt.onafterpaste=function(){
			if(this.value.length==1){
				this.value=this.value.replace(/[^1-9]/g,'');
			}else{
				this.value=this.value.replace(/\D/g,'');
			}
		}
	}
}
//选中获取搜索结果
function searfn(dom){
	 var id=dom.dataset.val;
	 var type=dom.dataset.type;
	 var $tg;
	 switch(type){
	 case "个人通信录":
		 $("#mytab").tabs("select",0);
		 $tg=$("#personalTab");
		 break;
	 case "公共通信录":
		 $("#mytab").tabs("select",1);
		 $tg=$("#publicTab");
		 break;
//	 case "会议历史记录":
//		 $("#mytab").tabs("select",2);
//		 $tg=$("#recordTab");
//		 break;
	 }
	 
	 var parent=$tg.treegrid("getParent",id);
	 $tg.treegrid("expand",parent.id);
	 $tg.treegrid("unselectAll");
	 $tg.treegrid("select",id);
	 
	 $("#contactSearch").hide();
}
//字符串去空
String.prototype.trim=function()
{
     return this.replace(/(^\s*)|(\s*$)/g,"");
}

//搜索联系人
function doSearch(value){
	 
}
//搜索参会者
function search(value){
	//alert(value);
}
//自定义操作列
function formatOper(val,row,index){
	if(!isNaN(val)){
		var mid=$("#meetId").val();
		var display;
		if(mid){
			return "<span style=\"display:none;\">已添加</span><a class=\"joinMeet\" href=\"javascript:;\" style=\"display:none;\" onclick=\"addContact("+val+",this)\"></a><a href=\"javascript:;\" onclick=\"addContact("+val+",this)\" class=\"call\" style=\"display:inline-block\"></a>";
		}else{
			return "<span style=\"display:none;\">已添加</span><a class=\"joinMeet\" href=\"javascript:;\" style=\"display:inline-block;\" onclick=\"addContact("+val+",this)\"></a><a href=\"javascript:;\" onclick=\"addContact("+val+",this)\" class=\"call\" style=\"display:none\"></a>";
		}
		
	}else{
		return "";
	}
}

function formatopar1(val,row,index){
	var mid=$("#meetId").val();
	var display;
	if(mid){
		display="display:inline-block;";
	}else{
		display="display:none;";
	}
	return "<a href=\"javascript:;\" class=\"call\" onclick=\"call("+row.phone+",this)\" style=\""+display+"\"></a>&nbsp;<a href=\"javascript:;\" onclick=\"fndel('"+row.phone+"')\" class=\"delete\"></a>&nbsp;<a href=\"javascript:;\" onclick=\"fnup("+row.phone+",this,'invitTab')\" class=\"up\"></a>";
}

//呼叫
function call(phone,dom){
	var invite=$("#invitTab").datagrid("getRows");
	var index,row;
	for(var i=0,len=invite.length;i<len;i++){
		if(invite[i].phone==phone){
			index=i;
			row=invite[i];
			break;
		}
	}
//	 var index=$("#invitTab").datagrid("getRowIndex",val);
//	 $("#invitTab").datagrid("selectRow",index);
//	 var row=$("#invitTab").datagrid("getSelected");
	 var cls=$(dom).attr("class");
	 var callers=[];
	 callers.push({caller:row.phone,name:row.name});
	 var mid=$("#meetId").val();
	 if(cls=="call"){//呼叫
		 $(dom).attr("class","call-end").closest("td").prev().find("div").text("呼叫中...");
		 joinMeeting(mid, callers);
	 }else{//挂断
		 $(dom).attr("class","call").closest("td").prev().find("div").text("通话结束");
		 leaveMeeting(mid, callers);
	 }
}

function formatopar2(val,row,index){
	return "<a href=\"javascript:;\" class=\"mic\" onclick=\"speak('"+row.phone+"',this)\"></a>&nbsp;<a href=\"javascript:;\" class=\"call-end\" onclick=\"hangup("+row.phone+")\"></a>&nbsp;<a href=\"javascript:;\" class=\"up\" onclick=\"fnup("+row.phone+",this,'inMeetTab')\"></a>";
}

//发言
function speak(phone,dom){
	var cls=$(dom).attr("class");
	var meetId=$("#meetId").val();
	var callers=[{caller:phone}];
	if(cls=="mic"){//静音
		forbidTalk(meetId, callers);
		$(dom).attr("class","mic-off");
	}else{//允许发言
		allowTalk(meetId, callers);
		$(dom).attr("class","mic");
	}
}


//移除出会议
function hangup(phone){
	var inmeet=$("#inMeetTab").datagrid("getRows");
	 var index,row;
	 for(var i=0,len=inmeet.length;i<len;i++){
		 if(phone==inmeet[i].phone){
			 index=i;
			 row=inmeet[i];
			 break;
		 }
	 }
//	 $("#invitTab").datagrid("appendRow",{
//		 name:row.name,
//		 phone:row.phone,
//		 status:"通话结束"
//	 });
	 var callers=[{caller:row.phone}];
//	 $inMeetTab.datagrid("deleteRow",index);
	//更新与会人以及邀请人数量
//	var num=$("#invitTab").datagrid("getRows").length;
//	var num1=$inMeetTab.datagrid("getRows").length;
//	$("#inMeetNum").text(num1);
//	$("#waitmeet").text(num);
	 //
	 var mid=$("#meetId").val();
	 leaveMeeting(mid, callers);
}
//上移
function fnup(phone,dom,tab){
	 var data=$("#"+tab).datagrid("getRows");
	 var index,row;
	 for(var i=0,len=data.length;i<len;i++){
		 if(phone==data[i].phone){
			 index=i;
			 row=data[i];
			 break;
		 }
	 }
//	var index=$("#"+tab).datagrid("getRowIndex",id);
//	$("#"+tab).datagrid("selectRow",index);
//	var row=$("#"+tab).datagrid("getSelected");
	$("#"+tab).datagrid("deleteRow",index);
	var cls=$(dom).attr("class");
	var item=$(dom).parent().find("a:first").attr("class");
	if(cls=="up"){//上升
		$("#"+tab).datagrid("insertRow",{
			index:0,
			row:row
		});
		$("#"+tab).prev().find("div.datagrid-body>table tr:first td:last").find("a.up").attr("class","down").parent().find("a:first").attr("class",item);
		$("#"+tab).prev().find("div.datagrid-body>table tr").find("td:last").each(function(i){
			if(i!=0){
				$(this).find("a:last").attr("class","up");
			}
			
			//$(this).find("a:last").attr("class","up");
		});
	}else{//下降
		var rows=$("#"+tab).datagrid("getRows");
		var n=rows.length;
		$("#"+tab).datagrid("insertRow",{
			index:n,
			row:row
		});
		$("#"+tab).prev().find("div.datagrid-body>table tr:last td:last").find("a:first").attr("class",item);
	}
	
}
//删除
function fndel(phone){
	//var row = $("#invitTab").datagrid("getSelected");
	var invite=$("#invitTab").datagrid("getRows");
	var index,row;
	for(var i=0,len=invite.length;i<len;i++){
		if(phone==invite[i].phone){
			index=i;
			row=invite[i];
		}
	}
//	var index=$("#invitTab").datagrid("getRowIndex",id);
//	$("#invitTab").treegrid("select",id);
//	var row=$("#invitTab").treegrid("getSelected");
	//如果该号码处理呼叫状态，先挂机
	if(row.status=="处理中"||row.status=="振铃中"||row.status=="应答"){
		var mid=$("#meetId").val();
		var callers=[{caller:row.phone}];
		leaveMeeting(mid, callers);
	}
	
	//删除数据
	var options=$("#selectHost option");
	for(var i=0,len=options.length;i<len;i++){
		var val=$(options[i]).val();
		if(val==phone){
			$(options[i]).remove();
		}
	}
	//重新加载combobox的数据
//	$("#selectHost").combobox("loadData",cdata);
	//检查数据，改变联系人表中操作状态
	chck("#personalTab",phone);
	chck("#publicTab",phone);
//	chck("#recordTab",phone);
	//更新显示数量
	//更新与会人以及邀请人数量
	var invite=$("#invitTab").datagrid("getRows");
	for(var i=0,len=invite.length;i<len;i++){
		if(phone==invite[i].phone){
			invite.splice(i,1);
			break;
		}
	}
	$("#invitTab").datagrid("loadData",invite);
	var num=invite.length;
	var num1=$("#inMeetTab").datagrid("getRows").length;
	$("#inMeetNum").text(num1);
	$("#waitmeet").text(num);
	/*if(id){
		$.ajax({
			type:"get",
			url:"/phonemeeting/delInvite/"+id,
			dataType:"json",
			success:function(result){
				if(result.success){
//					var val=$("#selectHost option:selected").val();
//					if(phone==val){
//						$("#selectHost").combobox("setValue","");
//					}
					
				}else{
					Alert("删除操作失败！");
				}
			},error:function(err){
				console.log(err.responseText);
				Alert("删除操作失败！");
			}
		});
	}*/
	
//	$("#invitTab").datagrid("deleteRow",index);
	for(var i=0;i<cdata.length;i++){
		if(cdata[i].val==phone){
			cdata.splice(i,1);
			invite.splice(i,1);
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

//添加联系人
function addContact(val,dom){
	var tab = $("#mytab").tabs("getSelected");//获取选择的tab
	var index=$("#mytab").tabs("getTabIndex",tab);
	var str;
	switch (index) {
	case 0:
		str="#personalTab";
		break;
	case 1:
		str="#publicTab";
		break;
//	case 2:
//		str="#recordTab";
//		break;
	}
	//获取选择行的数据
	$(str).treegrid("select",val);
	var row=$(str).treegrid("getSelected");
	if(row){
		var rid=$("#rid").val();
		//改变添加的联系人的操作状态
		var phone=row.phone;
		chck("#personalTab",phone);
		chck("#publicTab",phone);
//		chck("#recordTab",phone);
		//invite.push({name:row.name,phone:row.phone,status:"等待呼叫",id:id});
		//alert(row.name+"/"+row.phone+"/"+result.id);
		$("#invitTab").datagrid("appendRow",{
			name:row.name,
			phone:row.phone,
			status:"等待呼叫",
		});
		//id++;
		var data=[];
		data.push({val:row.phone,text:row.name+"("+row.phone+")"});
		loadHost(data);
//		$("#selectHost").combobox("loadData",cdata);
		//更新与会人以及邀请人数量
		var num=$("#invitTab").datagrid("getRows").length;
		var num1=$("#inMeetTab").datagrid("getRows").length;
		$("#inMeetNum").text(num1);
		$("#waitmeet").text(num);
		
		var cls=$(dom).attr("class");
		if(cls=="call"){
			var callers=[];
			callers.push({caller:row.phone,name:row.name});
			var mid=$("#meetId").val();
			joinMeeting(mid, callers);
		}
		//$("#clearAll").removeClass("l-btn-disabled");
		$("#callAll").removeClass("l-btn-disabled");
		/*$.ajax({
			type:"post",
			url:"/phonemeeting/addInvite",
			dataType:"json",
			data:{name:row.name,phone:row.phone,rid:rid},
			success:function(result){
				if(result.success){
					
				}else{
					Alert("操作失败！");
				}
			},error:function(err){
				console.log(err.responseText);
				Alert("操作失败！");
			}
		});*/
		
	}
	
	function chck(id,phone){
		var data = $(id).treegrid("getData");
		for(var i=0;i<data.length;i++){
			var children=data[i].children;
			for(var n=0;n<children.length;n++){
				var child=children[n];
				if(child.phone==phone){
					$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
						//alert($(this).find("div").text());
						if($(this).find("div").text()==phone){
							$(this).next().find("div span").show();
							$(this).next().find("div a").hide();
						}
					});
					//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
				}
			}
		}
	}
}

function fnadd(){
	
}
//自定义列
function formatCheckbox1(val,row,index){
//	if(isNaN(val)){
//		return "<input type=\"checkbox\" onclick=\"show(this,'"+row.id+"','#personalTab')\" class=\"check_"+row.id+"\" />"+row.name;
//	}
	return "<input type=\"checkbox\" onclick=\"show(this,'"+row.id+"','#personalTab')\" class=\"check_"+row.id+"\" data-name=\""+row.name+"\" data-phone=\""+row.phone+"\" />"+row.name;
}
function formatCheckbox2(val,row,index){
	return "<input type=\"checkbox\" onclick=\"show(this,'"+row.id+"','#publicTab')\" class=\"check_"+row.id+"\" data-name=\""+row.name+"\" data-phone=\""+row.phone+"\" />"+row.name;
}
function formatCheckbox3(val,row,index){
	return "<input type=\"checkbox\" onclick=\"show(this,'"+row.id+"','#recordTab')\" class=\"check_"+row.id+"\" data-name=\""+row.name+"\" data-phone=\""+row.phone+"\" />"+row.name;
}

function show(dom,checkid,tab){
	var s=dom;
	var $dv=$(dom).closest("div.datagrid-body");
	//alert($table.get(0).TagName);
	//选子节点
	var nodes=$(tab).treegrid("getChildren",checkid);
	for(var i=0;i<nodes.length;i++){
		//alert(nodes[i].id);
		$dv.find(" input.check_"+nodes[i].id).get(0).checked=dom.checked;
	}
	
	//选上级节点
	if(!$(s).get(0).checked){
		var parent=$(tab).treegrid("getParent",checkid);
		if(parent){
			$dv.find("input.check_"+parent.id).get(0).checked=false;
		}
		while(parent){
			parent=$(tab).treegrid("getParent",parent.id);
			$dv.find("input.check_"+parent.id).get(0).checked=false;
		}
	}else{
			var parent=$(tab).treegrid("getParent",checkid);
			if(parent==null){
				return false;
			}
			var flag=true;
			var sons=$(tab).treegrid("getChildren",parent.id);
			//检测所有子节点是否全部选中
			for(var i=0;i<sons.length;i++){
				if(!$dv.find("input.check_"+sons[i].id).get(0).checked){
					flag=false;
					break;
				}
			}
			if(flag){
				$dv.find("input.check_"+parent.id).get(0).checked=true;
			}
			while(flag){
				parent=$(tab).treegrid("getParent",parent.id);
				if(parent){
					sons=$(tab).treegrid("getChildren",parent.id);
					for(var i=0;i<sons.length;i++){
						if(!$dv.find("input.check_"+sons[i].id).get(0).checked){
							flag=false;
							break;
						}
					}
					if(flag){
						$($dv.find("input.check_"+parent.id)).get(0).checked=true;
					}
				}
			}
	}
}
//获取选中的结点
function getSelected(){ 
    var idList = "";  
     $("input:checked").each(function(){
        var id = $(this).attr("id"); 
        
        if(id.indexOf("check_")>-1)
            idList += id.replace("check_",'')+',';
         
     })
    alert(idList);
}

function init(){
	//检测浏览器是否支持WebSocket
    if(!window.WebSocket){
    	dialog({
    		title:"消息",
    		content:"对不起，当前浏览器版本过低，请使用Firefox 4、Chrome 4、Opera 10.70以及Safari 5版本以上的浏览器",
    		okValue:"确定",
    		ok:function(){
    			//关闭当前窗口
    			window.opener=null;
				window.open("", "_self");
				window.close();
    		}
    	}).showModal();
    	return;
    }
     //设置邀请人数据
     var str=$("#inviteData").text();
     invite=JSON.parse(str);
     $("#waitmeet").text(invite.length);//设置人数
     $("#invitTab").datagrid("loadData",invite);
     //设置会议中的数据
     var str1=$("#inMeetdata").text();
     if(str1){
    	 //alert(str1);
    	 inmeet=JSON.parse(str1);
    	 $("#inMeetNum").text(inmeet.length);
         $("#inMeetTab").datagrid("loadData",inmeet);
     }
     
//     for(var i=0;i<invite.length;i++){
//    	 cdata.push({val:invite[i].phone,text:invite[i].name+"("+invite[i].phone+")"});
//     }
//     
//     $("#selectHost").combobox("loadData",cdata);

     //注册监听事件
     document.getElementById("selectHost").onclick=function(){
    	 var index=this.selectedIndex;
    	 var name=this.options[index].text;
    	 var phone=this.options[index].value;
    	 name=name.substring(0,name.indexOf("("));
    	 $("#hostNum").text(phone);
		 $("#hostName").val(name);
     }
//     $("#selectHost").combobox({onSelect:function(record){
//    	 var phone=record.val;
//    	 var name=record.text;
//    	 name=name.substring(0,name.indexOf("("));
//    	 $("#hostNum").text(phone);
//		 $("#hostName").text(name);
//     }});
	 //更新显示数量
	 $("#waitmeet").text(cdata.length);
	 //表格渲染后执行
	 $("#personalTab").treegrid({onLoadSuccess:function(row,data){
		 	//alert("Ok");
		 	//去掉结点前面的文件及文件夹小图标
		 	$(".tree-icon,.tree-file").removeClass("tree-icon tree-file");
		 	$(".tree-icon,.tree-folder").removeClass("tree-icon tree-folder tree-folder-open tree-folder-closed"); 
		 	//检测邀请人
		 	var invite=$("#invitTab").datagrid("getRows");
		 	for(var a=0;a<invite.length;a++){
		 		chck("#personalTab",invite[a].phone);
		 	}
		 	//检测会议中
		 	var inmeet=$("#inMeetTab").datagrid("getRows");
		 	for(var b=0;b<inmeet.length;b++){
		 		chck("#personalTab",inmeet[b].phone);
		 	}
	 	}
	 });
	 $("#publicTab").treegrid({onLoadSuccess:function(row,data){
		 //去掉结点前面的文件及文件夹小图标
	     $(".tree-icon,.tree-file").removeClass("tree-icon tree-file");
	     $(".tree-icon,.tree-folder").removeClass("tree-icon tree-folder tree-folder-open tree-folder-closed"); 
	     //检测邀请人
		 for(var a=0;a<invite.length;a++){
			 chck("#publicTab",invite[a].phone);
		 }
		 //检测会议中
		 for(var b=0;b<inmeet.length;b++){
			 chck("#publicTab",inmeet[b].phone);
		 }
	 }
	 });
	/* $("#recordTab").treegrid({onLoadSuccess:function(row,data){
		//去掉结点前面的文件及文件夹小图标
		$(".tree-icon,.tree-file").removeClass("tree-icon tree-file");
		$(".tree-icon,.tree-folder").removeClass("tree-icon tree-folder tree-folder-open tree-folder-closed"); 
		//检测邀请人
		for(var a=0;a<invite.length;a++){
			chck("#recordTab",invite[a].phone);
		}
		//检测会议中
		for(var b=0;b<inmeet.length;b++){
		    chck("#recordTab",inmeet[b].phone);
		}
	 }
	 });*/
//	 检测邀请人
//	 for(var a=0;a<invite.length;a++){
//		 chck("#personalTab",invite[a].phone);
//		 chck("#publicTab",invite[a].phone);
//		 chck("#recordTab",invite[a].phone);
//	 }
//	 //检测会议中
//	 for(var b=0;b<inmeet.length;b++){
//		 chck("#personalTab",inmeet[b].phone);
//		 chck("#publicTab",inmeet[b].phone);
//		 chck("#recordTab",inmeet[b].phone);
//	 }
	 var mid=$("#meetId").val();
	 if(mid){//如果存在，说明会议以开始，则初始化websocket
		 $("#loading").show();
		 initWebsocket();
	 }else{
		 $("#loading").hide();
	 }
	 
	 function chck(id,phone){
			var data = $(id).treegrid("getData");
			for(var i=0;i<data.length;i++){
				var children=data[i].children;
				for(var n=0;n<children.length;n++){
					var child=children[n];
					
					if(child.phone==phone){
						$(id).prev().find("div.datagrid-body>table").find("td[field=phone]").each(function(){
							if($(this).find("div").text()==phone){
								$(this).next().find("div span").show();
								$(this).next().find("div a").hide();
							}
						});
						//$(id).prev().find("div.datagrid-body>table tr:eq("+i+")").find("table.datagrid-btable tr:eq("+n+")").find("td:last div").text("已添加");
					}
				}
			}
		}
}

//加载选择主持人的下拉列表的数据
function loadHost(data){
	if(data){
		for(var i=0,len=data.length;i<len;i++){
			$("#selectHost").append("<option value=\""+data[i].val+"\">"+data[i].text+"</option>");
		}
	}
}

function showTip(msg){
	dialog({
		title:"消息",
		content:msg,
		width:180,
		okValue:"确定",
		ok:function(){}
	}).showModal();
}
//创建提示
function Alert(msg){
	var msgw,msgh,bgColor;
	msgw=350;
	msgh=25;
	bgColor="#F4C600";
	var msgObj=document.createElement("div");
	msgObj.setAttribute("id", "alertmsgDiv");
	msgObj.style.position = "absolute";
	msgObj.style.bottom="0";
	msgObj.style.textAlign="center";
	msgObj.style.left = "50%";
    msgObj.style.font = "12px/1.6em Verdana, Geneva, Arial, Helvetica, sans-serif";
    msgObj.style.marginLeft = "-125px";
    msgObj.style.width = msgw + "px";
    msgObj.style.height = msgh + "px";
    msgObj.style.lineHeight = "25px";
    msgObj.style.background=bgColor;
    msgObj.style.zIndex = "99";
    msgObj.style.filter = "progid:DXImageTransform.Microsoft.Alpha(startX=20, startY=20, finishX=100, finishY=100,style=1,opacity=75,finishOpacity=100);";
    msgObj.style.opacity = "0.75";
    msgObj.style.color="white";
    msgObj.innerHTML=msg;
    document.body.appendChild(msgObj);
    setTimeout("closewin()", 2000);
}
//关闭提示
function closewin() {
    //document.body.removeChild(document.getElementById("alertbgDiv"));
    //document.getElementById("alertmsgDiv").removeChild(document.getElementById("alertmsgTitle"));
	var msgObj=document.getElementById("alertmsgDiv");
	if(msgObj){
		document.body.removeChild(msgObj);
	}
    
}

//将捕获到的错误信息上传到服务器
function recordError(methodName,error){
	var obj={fileName:"/js/createMeet.js"};
	obj.methodName=methodName;
	obj.type=error.type;
	obj.message=error.message;
	$.ajax({
		type:"post",
		url:"/log",
		data:obj,
		success:function(data){
			
		},error:function(err){
			console.log(err.responseText);
		}
	});
}