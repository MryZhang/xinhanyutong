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
$(document).ready(function () {
    
    //初始化
	init();
    //===========
    //显示或隐藏消息
	$("#slide_btn").click(function () {
		if (local.expanded) {
			$(this).find("span").attr("class", "glyphicon glyphicon-chevron-down");
			$("#info").animate({"height": "150px"}, 100);
			$("#info_list").show();
			local.expanded=false;
		} else {
			$(this).find("span").attr("class", "glyphicon glyphicon-chevron-up");
			$("#info_list").hide();
			$("#info").animate({ "height": "36px" }, 100);
		    local.expanded=true;
		}
	});
    //初始化bootstrap switch
	$("input[name=mycheckbox]").bootstrapSwitch();
    //全选
	$("#contacts_content").on("click",".group input[type=checkbox]", function (e) {
	    var checked = this.checked;
	    var chks = $(this).parent().next().find("input[type=checkbox]");
	    if (chks) {
	        for (var i = 0, len = chks.length; i < len;i++){
	            chks[i].checked = checked;
	            var name = $(chks[i]).parent().text().trim();
	            var phone = $(chks[i]).parent().next().text().trim();
	            if (checked) {
	                addSelected(name,phone);
	            } else {
	                removeSelected(name,phone);
	            }
	        }
	    }
	    window.event ? window.event.cancelBubble = true : e.stopPropagation();
	});

    //单选
	$("#contacts_content").on("click", "table input[type=checkbox]", function () {
	    var name = $(this).parent().text().trim();
	    var phone = $(this).parent().next().text().trim();
	    addSelected(name,phone);
	});

    //从通讯录中批量添加参会人
	$("#addInviteBtn").click(function () {
		var selected=$("body").data("selected");
		var meetId=$("#meetId").val();
		var callers=[];
		if(selected&&selected.length>0){
			for(var i=0,len=selected.length;i<len;i++){
				var name=selected[i].name;
				var phone=selected[i].phone;
				addInvite(selected[i].name,selected[i].phone);
				if(meetId){
					callers.push({name:name,caller:phone});
				}
			}
			if(meetId){//呼叫
				joinMeeting(meetId, callers);
			}
		}
		$("body").data("selected",null);//清空缓存数据
		$("#contacts_content input[type=checkbox]").attr("checked",false);
	});
	
	//从通讯录中单个添加参会人
	$("#contacts_content").on("click","a.add-invite,a.call",function(){
		var $tr = $(this).closest("tr");
		var name=$tr.find("td:first").text().trim();
		var phone=$tr.find("td:eq(1)").text().trim();
		addInvite(name,phone);
		var meetId=$("#meetId").val();
		if(meetId){
			var callers=[{name:name,caller:phone}];
			joinMeeting(meetId, callers);
		}
	});
	

    //展开或收缩联系人组
	$("#contacts_content").on("click","div.group",function () {
		//判断是事件是否是从子元素冒泡上来的
		var event = window.event || arguments.callee.caller.arguments[0];
        var targetObj = event.srcElement || event.target;
        if(targetObj.tagName=="DIV"){
        	var cls = $(this).find("i").attr("class");
    	    if (cls == "icon-caret-down") {
    	        $(this).find("i").attr("class", "icon-caret-right");
    	    } else {
    	        $(this).find("i").attr("class", "icon-caret-down");
    	    }
        }
	    
	});
    //更换主题
	$("#edit_subject").click(function () {
	    var subject = $("#msubject").text();
	    $("#msubject").hide();
	    $("#subject_form").css("display", "inline-block");
	    $("#subject_txt").val(subject);
	    $(this).hide();
	});
    //确认更换主题
	$("#btn_sub").click(function () {
	    var subject = $("#subject_txt").val();
	    $(this).parent().hide();
	    $("#msubject").text(subject).attr("title",subject).css("display", "inline-block");
	    $("#edit_subject").show();
	});
    //更换主持人
	$("#change_host").click(function () {
	    var data=$("body").data("host");
	    if(!data){
	    	data=[];
	    }
	    var content = createHostChoice(data);
	    showModal("选择主持人", content, function () {
	        var host = $("#selectHost li.bg-success").text();
	        $("#mhost").text(host).attr("title",host);
	    });
	});
    //创建会议备忘录
	$("#btn_remark").click(function () {
		var rid=$("#rid").val();
		$.ajax({
			type:"get",
			url:"/meet/getRemark/"+rid,
			dataType:"text",
			success:function(data){
				 showModal("创建备忘录", "<textarea class='form-control' rows='5' id='remark'>"+data+"</textarea>", function () {
				        var remark = $("#remark").val();
				        $.ajax({
				        	type:"post",
				        	url:"/meet/updateRemark/",
				        	dataType:"json",
				        	data:{"rid":rid,"txt":remark},
				        	success:function(data){
				        		
				        	},error:function(err){
				        		console.log(err.responseText);
				        	}
				        });
				    }, true);
			},error:function(err){
				console.log(err.responseText);
			}
		});
	   
	});
    //手动邀请参会者
	$("#add_invite").click(function () {
	    var isPhone = /^([0-9]{3,4})?[0-9]{7,8}(P[0-9]{4})?$/;
	    var isMob = /^1[3|4|5|7|8]\d{9}$/;
	    var name = $("#name").val();
	    var phone = $("#phone").val();
	    var flag = true;
	    if (name != "" && name.indexOf("（") != -1 || name.indexOf("）") != -1) {
	        Alert("名称不能包含'（'或'）'字符");
	        flag = false;
	    }
	    if (flag) {
	        if (phone.trim() == "") {
	            Alert("电话号码不能为空！");
	            flag = false;
	        } else if (!isPhone.test(phone) && !isMob.test(phone)) {
	            Alert("电话号码格式不正确！");
	            flag=false;
	        }
	    }
	    
	    if (flag) {
	    	if(!name){
	    		name=getName(phone);
	    	}
	        addHost(name,phone);
	        flag = addInvite(name, phone);
	        var meetId=$("#meetId").val();
		    if(meetId&&flag){//呼叫
		       var callers=[{name:name,caller:phone}];
		       joinMeeting(meetId, callers);
		    }
		    if(flag){
		    	$("#name").val("");
		    	$("#phone").val("");
		    	changeContactAdded(phone);
		    }else{
		    	Alert("该号码已存在！");
		    }
		    
	        
	    }
	});
	
	//呼叫号码
	$("body").on("click","a.call",function(){
		 var meetId=$("#meetId").val();
		 var $tr=$(this).closest("tr");
		 var name=$tr.find("td:eq(0)").text().trim();
		 var phone=$tr.find("td:eq(1)").text().trim();
		 var cls=$(this).attr("class");
	     if(meetId&&cls.indexOf("disabled")==-1){//呼叫
	    	 $(this).addClass("disabled");
	        var callers=[{name:name,caller:phone}];
	        joinMeeting(meetId, callers);
	     }
	});
	
	//挂断
	$("body").on("click","a.call-end",function(){
		var meetId=$("#meetId").val();
		var $tr=$(this).closest("tr");
		var name=$tr.find("td:eq(0)").text().trim();
		var phone=$tr.find("td:eq(1)").text().trim();
	    if(meetId){//呼叫
	       var callers=[{name:name,caller:phone}];
	       leaveMeeting(meetId, callers);
	    }
	});
	
	//向上
	$("body").on("click","a.push-up",function(){
		var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
		pushUp(this,phone);
	});
	
	//向下
	$("body").on("click","a.push-down",function(){
		var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
		pushDown(this,phone);
	});

    //选中联系人
	$("#contacts_content").on("click", "div.collapse table tr",function () {
	    $("#contacts_content div.collapse table tr").removeClass("success");
	    $(this).addClass("success");
	});
	
	//禁言
	$("#attendee_list").on("click","a.mic",function(){
		var meetId=$("#meetId").val();
		var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
		var name=$(this).closest("tr").find("td:first").text().trim();
		var callers=[{name:name,caller:phone}];
		forbidTalk(meetId, callers);
	});
	
	//允许发言
	$("#attendee_list").on("click","a.mic-off",function(){
		var meetId=$("#meetId").val();
		var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
		var name=$(this).closest("tr").find("td:first").text().trim();
		var callers=[{name:name,caller:phone}];
		allowTalk(meetId, callers);
	});
	
	//======================================
    //通讯录联系人搜索
	$("#search_contacts").keyup(function () {
	    var txt = this.value;
	    setTimeout("searchContacts('"+txt+"')", 500);
	});
	$("#search_contacts").blur(function () {
	    if (!local.isHover) {
	        $("#contactsResult").hide();
	    }
	});
	$("#contactsResult").click(function () {
	    $(this).hide();
	});
	$("#contactsResult").hover(function () { local.isHover = true; }, function () { local.isHover = false; });
    //选中获取的搜索联系人
	$("#contactsResult").on("click","li", function () {
	    var str = $(this).text();
	    var arr = str.split("--");
	    var phone = arr[0];
	    phone = phone.substring(phone.indexOf("(") + 1, phone.indexOf(")"));
	    var type = arr[1];
	    type = type.substring(0, type.indexOf("("));
	    if (type == "个人通讯录") {
	        type = 0;
	    } else {
	        type = 1;
	    }
	    var group = arr[1];
	    group = group.substring(group.indexOf("(") + 1, group.indexOf(")"));
	    //alert(phone+"/"+type+"/"+group);
	    getContactResult(phone,type,group);
	});
	//==========================
	
	//选中邀请人
	$("#invite>table>tbody").on("click","tr",function(){
		$("#invite>table>tbody>tr").removeClass("success");
		$(this).addClass("success");
	});
	
	//===========================
	//邀请人搜索
	$("#search_invite").keyup(function(){
		var txt=this.value;
		setTimeout("searchInvite('"+txt+"')", 500);
	});
	$("#search_invite").blur(function () {
	    if (!local.isHover1) {
	        $("#inviteResult").hide();
	    }
	});
	$("#inviteResult").click(function () {
	    $(this).hide();
	});
	$("#inviteResult").hover(function () { local.isHover1 = true; }, function () { local.isHover1 = false; });
	//选中搜索结果
	$("#inviteResult").on("click","li",function(){
		var phone=$(this).text();
		phone=phone.substring(phone.indexOf("(")+1,phone.indexOf(")"));
		getInviteResult(phone);
	});
	//============================================
	
	//选中参会者
	$("#attendee_list table>tbody>tr").on("click","tr",function(){
		$("#attendee_list>table>tbody>tr").removeClass("success");
		$(this).addClass("success");
	});
	
	//==========会议中参会人搜索=====================
	$("#searchInMeet").keyup(function(){
		var txt=this.value;
		setTimeout("searchInMeet('"+txt+"')", 500);
	});
	$("#searchInMeet").blur(function () {
	    if (!local.isHover2) {
	        $("#inmeetResult").hide();
	    }
	});
	$("#inmeetResult").click(function () {
	    $(this).hide();
	});
	$("#inmeetResult").hover(function () { local.isHover2 = true; }, function () { local.isHover2 = false; });
	//选中搜索结果
	$("#inmeetResult").on("click","li",function(){
		var phone=$(this).text();
		phone=phone.substring(phone.indexOf("(")+1,phone.indexOf(")"));
		getInMeetResult(phone);
	});
	//=================END===================
	
	//移除参会者
	$("#invite").on("click","a.clear",function(){
		var phone=$(this).closest("tr").find("td:eq(1)").text().trim();
		removeInvite(phone);
	});
	
	//开始会议
	$("#startMeetBtn").click(function(){
		var hostNum=getHostNum();
		var rid=$("#rid").val();
		if($(this).text()=="开始会议"){
			var isRecord =$("#isRecord").bootstrapSwitch("state");
			var subject=$("#msubject").text();
			var hostName=getHostName();
			var meetPwd=$("#mpwd").val();
			createMeet(hostName,hostNum,subject,isRecord,rid,meetPwd);
			$("#startMeetBtn").attr("disabled","disabled");
			local.isFirst=true;//表示该会议第一次呼叫
		}else{//结束会议
			$("#loading").show();
			var meetId=$("#meetId").val();
			stopMeeting(meetId);
		}
		
	});
	
	//全体静音或允许发言
	$("#isAllowSpeak").on("switchChange.bootstrapSwitch",function(event,state){
		if(state){
			allSlience();
		}else{
			allSpeak();
		}
	});
	
	//背景音乐
	$("#playback").on("switchChange.bootstrapSwitch",function(event,state){
		var meetId=$("#meetId").val();
		if(state){
			startPlayback(meetId);
		}else{
			stopPlayback(meetId);
		}
	});
	
	//会议锁门
	$("#lockMeet").on("switchChange.bootstrapSwitch",function(event,state){
		var meetId=$("#meetId").val();
		lock(meetId,state);
	});
});



