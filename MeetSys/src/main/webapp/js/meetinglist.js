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
	//取消会议
	$("a.cancel").click(function(){
		var $dom=$(this);
		dialog({
			title:"消息",
			width:200,
			content:"是否取消本次预约会议？<div style=\"color:red\"><label><input type=\"checkbox\" id=\"cancelAll\"/>取消整个会议计划</label></div>",
			okValue:"确定",
			ok:function(){
				var cancelAll=$("#cancelAll").get(0).checked;
				var oid=$dom.closest("tr").find("input.recordId").val();
				var date=$dom.closest("tr").find("td:first").text();
				$.ajax({
					type:"post",
					url:"/meetinglist/cancelMeet",
					dataType:"json",
					data:{"cancelAll":cancelAll,"oid":oid,"date":date},
					success:function(data){
						if(data.success){
							$dom.parent().text("已取消");
						}
					},error:function(err){
						console.log(err.responseText);
						alert("取消会议失败！");
					}
				});
			},
			cancelValue:"取消",
			cancel:function(){}
		}).showModal();
	});
	
	//会议中的详细信息
	$("a.detail").click(function(){
		var isCanceled=$(this).parent().find("input.isCanceled").val();
		var obj={};
		if(isCanceled){
			obj.mtype=2;//会议类型，2代表为预约会议
			obj.stime=$(this).closest("tr").find("td:eq(0)").text();
			obj.isCanceled=isCanceled;
		}else{
			obj.mid=$(this).closest("tr").find("input.recordMid").val();
			obj.mtype=1;//会议类型，1代表为非预约会议
		}
		obj.id=$(this).closest("tr").find("input.recordId").val();
		var $pdv=$(this).closest("div.parentDv");
		obj.date=$pdv.find("input.dateCls").val();
		obj.type=$pdv.find("input.typeCls").val();
		$("#loading").show();
		$.ajax({
			type:"post",
			url:"/meetinglist/getDetail/",
			dataType:"html",
			data:obj,
			success:function(data){
				$("#maincontent").html(data);
			},
			error:function(err){
				alert(err.responseText);
			}
		}).done(function(){
			$("#loading").hide();
		});
	});
	//进入会议
	$("a.enterMeet").click(function(){
		/*var $tr=$(this).closest("tr");
		//var rid = $tr.find("input.recordId").val();
		var mid = $tr.find("input.recordMid").val();
		$("#loading").show();
		//alert(mid);
		$.ajax({
			type:"get",
			url:"/meetingmanage/"+mid,
			dataType:"html",
			success:function(data){
				$("#maincontent").html(data);
			},
			error:function(err){
				alert(err.responseText);
			}
		}).done(function(){
			$("#loading").hide();
		});*/
	});
	//结束会议
	$("a.finishMeet").click(function(){
		var $tr=$(this).closest("tr");
		var rid = $tr.find("input.recordId").val();
		dialog({
			title:"消息",
			content:"确定要结束会议吗？",
			okValue:"确定",
			ok:function(){
				$.ajax({
					type:"get",
					url:"/meetinglist/stopMeeting/"+rid,
					dataType:"json",
					success:function(data){
						if(data.success){
							$("#loading").show();
							$.ajax({
								type:"get",
								url:"/meetinglist",
								dataType:"html",
								success:function(data){
									$("#maincontent").html(data);
								},
								error:function(err){
									alert(err.responseText);
								}
							}).done(function(){
								$("#loading").hide();
							});
						}else{
							dialog({
								title:"消息",
								content:"操作失败！",
								width:200,
								okValue:"确定",
								ok:function(){}
							}).showModal();
						}
					},
					error:function(err){
						alert(err.responseText);
					}
				});
			},
			cancelValue:"取消",
			cancel:function(){}
		}).showModal();
	});
});