$(document).ready(function(){
	$("input[type=text],input[type=password]").focus(function(){
		getFocus(this);
	});
	$("input[type=text],input[type=password]").blur(function(){
		validate(this);
	});
	$("#firstStep").click(function(){
		var flag=true;
		$("input[type=text]").each(function(){
			if(flag){
				flag=validate(this);
			}else{
				validate(this);
			}
		});
		if(flag){
			var phone=$("#emailid").val();
			var code=$("#vcodeid").val();
			$.ajax({
				type:"post",
				url:"/retrieve/getAccount",
				dataType:"json",
				data:{"phone":phone,"code":code},
				success:function(data){
					if(data.result){
						location.href="/retrieve/step2/"+phone;
					}else{
						if(data.phoneMsg){
							$("#emailid").addClass("highlight2");
							$("#emailid").next().removeClass("addConTip").addClass("error").text(data.phoneMsg);
						}
						if(data.codeMsg){
							$("#vcodeid").addClass("highlight2");
							$("#vcodeid").next().removeClass("addConTip").addClass("error").text(data.codeMsg);
							$("#authCodeImg").attr("src","/register/vertifyCode/"+new Date());
						}
					}
				},
				error:function(err){
					alert(err.responseText);
				}
			});
		}
	});
	
	$("#secondStep").click(function(){
		if(validate($("#mobileRegAuthCodeId"))){
			location.href="/retrieve/step3/"+$("#accountId").val();
		}
	});
	
	$("#submibbuttonid").click(function(){
		var flag=true;
		$("input[type=password]").each(function(){
			if(flag){
				flag=validate(this);
			}else{
				validate(this);
			}
		});
		if(flag){
			var phone=$("#accountId").val().trim();
			var password=$("#passwordid").val().trim();
			var chkpwd=$("#chkpwdid").val().trim();
			$.ajax({
				type:"post",
				url:"/retrieve/resetPwd",
				dataType:"json",
				data:{"phone":phone,"password":password,"chkPwd":chkpwd},
				success:function(data){
					if(data.result){
						location.href="/retrieve/step4";
					}else{
						if(data.pwdMsg){
							$("#passwordid").next().attr("class","error").text(data.pwdMsg);
							$("#passwordid").addClass("highlight2");
						}
						if(data.chkPwdMsg){
							$("#chkpwdid").next().attr("class","error").text(data.chkPwdMsg);
							$("#chkpwdid").addClass("highlight2");
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
	var id=$(dom).attr("id");
	$(dom).removeClass("highlight2").addClass("highlight1");
	switch(id){
	case "emailid":
		$(dom).next().text("请输入与该账号绑定的手机或邮箱");
		$(dom).next().removeClass("error").addClass("addConTip");
		break;
	case "vcodeid":
		$(dom).next().text("");
		break;
	case "mobileRegAuthCodeId":
		$(dom).next().attr("class","addConTip").text("手机验证码是6位数字");
		break;
	case "passwordid":
		$(dom).next().attr("class","addConTip").text("6-16位字符，可使用字母、数字或符号的组合");
		break;
	case "chkpwdid":
		$(dom).next().attr("class","addConTip").text("请再次输入您的密码");
		break;
	}
}
//验证
function validate(dom){
	var id=$(dom).attr("id");
	$(dom).removeClass("highlight1");
	var txt=$(dom).val().trim();
	var flag=true;
	switch(id){
	case "emailid":
		if(txt==""){
			$(dom).addClass("highlight2");
			$(dom).next().removeClass("addConTip").addClass("error").text("账户名不能为空");
			flag=false;
		}else if(!/^((13[0-9])|(15[^4,\D])|(18[0,5-9]))\d{8}$/.test(txt)){
			$(dom).addClass("highlight2");
			$(dom).next().removeClass("addConTip").addClass("error").text("账户名格式不正确，请填写您的手机号码");
			flag=false;
		}else{
			$(dom).next().text("");
		}
		break;
	case "vcodeid":
		if(txt==""||txt.length!=4){
			$(dom).next().text("请输入正确的验证码");
			$(dom).addClass("highlight2");
			flag=false;
		}
		break;
	case "mobileRegAuthCodeId":
		if(txt==""||!/^\d{6}$/.test(txt)){
			$(dom).next().attr("class","error").text("手机验证码是6位数字。");
			$(dom).addClass("highlight2");
			flag=false;
		}
		break;
	case "passwordid":
		if(txt==""){
			$(dom).next().attr("class","error").text("请填写您的密码");
			$(dom).addClass("highlight2");
			flag=false;
		}else if(txt.length<6||txt.length>16){
			$(dom).next().attr("class","error").text("密码为6-16位字符");
			$(dom).addClass("highlight2");
			flag=false;
		}else{
			$(dom).attr("class","text");
			$(dom).next().text("");
		}
		break;
	case "chkpwdid":
		if(txt==""){
			$(dom).next().attr("class","error").text("请再次填写您的密码");
			$(dom).addClass("highlight2");
			flag=false;
		}else if(txt!=$("#passwordid").val().trim()){
			$(dom).next().attr("class","error").text("两次填写的密码不一致,请核实后再输");
			$(dom).addClass("highlight2");
			flag=false;
		}else{
			$(dom).attr("class","text");
			$(dom).next().text("");
		}
		break;
	}
	return flag;
}

String.prototype.trim=function()
{
     return this.replace(/(^\s*)|(\s*$)/g,"");
}