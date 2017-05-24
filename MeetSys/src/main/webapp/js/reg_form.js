//全局变量，判断账号输入框是否是第一次获取焦点
var temp=true;
$(document).ready(function(){
	$(".i-help").each(function(){
		getHelpTip(this);
	});
	$("input").focus(function(){
		getFocus(this);
	});
	$("input").blur(function(){
		validate(this);
	});
	$("#regAccountBtnId").click(function(){
		var flag=true;
		$("input[type=text]").each(function(){
			if(flag){
				flag=validate(this);
			}else{
				validate(this);
			}
		});
		if(flag){
			var phone=$("#accountName").val();
			var code=$("#vcodeid").val();
			$.ajax({
				type:"get",
				url:"/register/validate/"+phone+"-"+code,
				dataType:"json",
				success:function(data){
					if(data.result){
						location.href="/register/showMsgVertify/"+phone;
					}else{
						if(data.vphoneMsg){//手机号码有误
							$("#accountName").attr("class","regtext highline2");
							$("#accountName_tip").attr("class","error").text(data.vphoneMsg);
						}
						if(data.vcodeMsg){//验证码有误
							$("#vcodeid").attr("class","inputtext regauthtext highline2");
							$("#vcodeMsg").attr("class","error").text(data.vcodeMsg);
							$("#codeImg").attr("src","/register/vertifyCode/"+new Date());
						}
					}
				},
				error:function(err){
					alert(err.responseText);
				}
			});
		}
	});
	
	$("#createGroupBtn").click(function(){
		var flag=true;
		$("input.text").each(function(){
			if(flag){
				flag=validate(this);
			}else{
				validate(this);
			}
		});
		if(flag){
			var phone=$("#phone").text();
			var password=$("#regPassword").val();
			var chkPwd=$("#regchkPwd").val();
			var name=$("#regUserName").val();
			var compName=$("#regCompName").val();
			var d={"phone":phone,"password":password,"chkPwd":chkPwd,"name":name,"compName":compName};
			$.ajax({
				type:"post",
				url:"/register/addUserData",
				dataType:"json",
				data:d,
				success:function(data){
					if(data.result){
						location.href="/register/step3/"+name+"-"+compName+"-"+phone;
					}else{
						if(data.pwdMsg){
							$("#regPassword").attr("class","text highlight2");
							$("#regPassword").next().next().attr("class","error").text(data.pwdMsg);
						}
						if(data.chkPwdMsg){
							$("#regchkPwd").attr("class","text highlight2");
							$("#regchkPwd").next().next().attr("class","error").text(data.chkPwdMsg);
						}
						if(data.nameMsg){
							$("#regUserName").attr("class","text highlight2");
							$("#regUserName").next().next().attr("class","error").text(data.nameMsg);
						}
						if(data.compNameMsg){
							$("#regCompName").attr("class","text highlight2");
							$("#regCompName").next().next().attr("class","error").text(data.compNameMsg);
						}
					}
				},
				error:function(err){
					alert(err.responseText);
				}
			});
		}
	});
});

function getFocus(dom){
	var name=$(dom).attr("name");
	switch(name){
	case "accountName":
		if(temp){
			$(dom).val("");
			temp=false;
		}
		$(dom).attr("class","regtext highline1");
		$(dom).next().next().attr("class","addConTip").text("请填写手机号");
		$(dom).next().attr("class","");
		break;
	case "authCode":
		$(dom).attr("class","regtext highline1");
		$(dom).attr("class","inputtext regauthtext highline1")
		$("#vcodeMsg").text("").attr("class","addConTip");
		break;
	case "password":
		$(dom).attr("class","text highlight1");
		$(dom).next().removeAttr("class");
		$(dom).next().next().attr("class","addConTip").text("6-16位字符，可使用字母、数字或符号的组合");
		break;
	case "regchkPwd":
		$(dom).attr("class","text highlight1");
		$(dom).next().removeAttr("class");
		$(dom).next().next().attr("class","addConTip").text("请再次输入密码");
		break;
	case "userName":
		$(dom).attr("class","text highlight1");
		$(dom).next().removeAttr("class");
		$(dom).next().next().attr("class","addConTip").text("仅支持中文、字母、数字、下划线或减号");
		break;
	case "compName":
		$(dom).attr("class","text highlight1");
		$(dom).next().removeAttr("class");
		$(dom).next().next().attr("class","addConTip").text("请填写您所在的企业名");
		break;
	}
}

function getHelpTip(dom){
	var name=$(dom).attr("name");
	$(dom).removeClass("highline1").addClass("i-help");
	if(name){
		switch(name){
		case "accountName":
			$(dom).val("请填写手机号");
			$(dom).next().next().text("");
			break;
		}
	}
}
//验证
function validate(dom){
	var name=$(dom).attr("name");
	var txt=$(dom).val().trim();
	var flag=true;
	switch(name){
	case "accountName":
		if(txt==""){
			$(dom).attr("class","regtext highline2");
			$("#accountName_tip").attr("class","error").text("请填写手机号");
			flag=false;
		}else if(!/^((13[0-9])|(15[^4,\D])|(18[0,5-9]))\d{8}$/.test(txt)){
			$(dom).attr("class","regtext highline2");
			$("#accountName_tip").attr("class","error").text("手机号输入有误");
			flag=false;
		}else{
			$(dom).attr("class","regtext");
			$(dom).next().attr("class","icon_orderly");
		}
		break;
	case "authCode":
		if(txt==""||txt.length!=4){
			$(dom).attr("class","inputtext regauthtext highline2");
			$("#vcodeMsg").attr("class","error").text("请填写右边的字符");
			flag=false;
		}else{
			$(dom).attr("class","inputtext regauthtext");
		}
		break;
	case "password":
		if(txt==""){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("请填写密码");
			flag=false;
		}else if(txt.length<6||txt.length>16){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("密码长度只能在6-16位字符之间");
			flag=false;
		}else{
			$(dom).attr("class","text");
			$(dom).next().attr("class","icon_orderly");
			$(dom).next().next().text("");
		}
		break;
	case "regchkPwd":
		if(txt==""){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("请再次填写密码");
			flag=false;
		}else if(txt.length<6||txt.length>16){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("密码长度只能在6-16位字符之间");
			flag=false;
		}else if($("#regPassword").val().trim()!=txt){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("两次输入的密码不一致，请核实后再输！");
			flag=false;
		}else{
			$(dom).attr("class","text");
			$(dom).next().attr("class","icon_orderly");
			$(dom).next().next().text("");
		}
		break;
	case "userName":
		if(txt==""){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("请填写用户名");
			flag=false;
		}else if(!/^[\u4e00-\u9fa5]*[a-zA-Z0-9_-]*$/.test(txt)){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("仅支持中文、字母、数字、下划线或减号");
			flag=false;
		}else{
			$(dom).attr("class","text");
			$(dom).next().attr("class","icon_orderly");
			$(dom).next().next().text("");
		}
		break;
	case "compName":
		if(txt==""){
			$(dom).attr("class","text highlight2");
			$(dom).next().next().attr("class","error").text("请填写企业名称");
			flag=false;
		}else{
			$(dom).attr("class","text");
			$(dom).next().attr("class","icon_orderly");
			$(dom).next().next().text("");
		}
		break;
	}
	return flag;
}

String.prototype.trim=function()
{
     return this.replace(/(^\s*)|(\s*$)/g,"");
}