/**
 * Created by MrQin on 2016/11/7.
 */
//字符串去空
String.prototype.trim=function()
{
    return this.replace(/(^\s*)|(\s*$)/g,"");
}
//收藏
function addfavorite(){
    try{
        if (document.all){
            window.external.addFavorite(document.location.href,"网络会议");
        } else if (window.sidebar){
            window.sidebar.addPanel("网络会议", document.location.href, "");
        } else {
            alert("抱歉！您的浏览器不支持直接添加收藏夹，请使用Ctrl+D进行添加");
        }
    }catch (e){
        window.alert("因为IE浏览器存在bug，添加收藏失败！\n解决办法：在注册表中查找\n HKEY_CLASSES_ROOT\\TypeLib\\{EAB22AC0-30C1-11CF-A7EB-0000C05BAE0B}\\1.1\\0\\win32 \n将 C:\\WINDOWS\\system32\\shdocvw.dll 改为 C:\\WINDOWS\\system32\\ieframe.dll ");
    }
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
//获取数组中指定元素的下标
Array.prototype.indexOf = function(val) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == val) return i;
    }
    return -1;
};
//删除数组中指定下标的元素
Array.prototype.remove = function(val) {
    var index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};

//公用方法对象
var common={
    //正整数正则表达式
    positiveIntegerRegex:/^[1-9]\d*$/,
    //特殊字符正则表达式
    specilStringRegex:/[`~!@#$%^&*()+=|{}':;',\[\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]/,
    //有理数的正则表达式
    rationalRegex:/^\-?([1-9][0-9]*|0)(\.[0-9]+)?$/,
    emailRegex:/^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/,//邮箱的正则表达式
    //手机号码的正则表达式
    phoneRegex:/^1[3|4|5|7|8]\d{9}$/,
    //座机或分机的正则表达式
    telRegex:/^([0-9]{3,4})?([0-9]{7,9}|[0-9]{5})((p|P|,|-)[0-9]{1,})?$/,
    //邮箱正则表达式
    emailRegex:/^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/,
    //显示模态窗口
    showDialog:function (obj) {
        dialog({
            title:obj.title||"提示",
            content:obj.content||"",
            quickClose:true,
            okValue:"确定",
            ok:function () {
                if(obj.ok&&(typeof obj.ok)=="function"){
                    return obj.ok();
                }
            },
            cancelValue:"取消",
            cancel:function () {

            }
        }).show();
    },
    //判断是闰年
    isLeapYear:function (date) {
        var year = date.getFullYear();
        return (year%4==0&&year%100!=0||year%400==0);
    },
    getContextPath:function () {
        return $("#contextPath").val();
    },
    getQueryString:function (obj) {
        var condition="";
        if(obj){
            for(var p in obj){
                if(condition){
                    condition+="&";
                }else{
                    condition="?";
                }
                condition+=p+"="+obj[p];
            }
        }
        return encodeURI(condition);
    },
    //登陆是否过期
    isLoginTimeout:function (data) {
        if(data.result===undefined){
            common.showDialog({content:"登陆超时，请重新登陆！",ok:function () {
                location.href=this.getContextPath()||""+"/login";
            }});
        }
    },
    //生成指定位数的随机字符串
    generateMixed:function (n) {
        var chars=['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G',
            'H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s',
            't','u','v','w','x','y','z'];
        var res = "";
        for(var i = 0; i < n ; i ++) {
            var index=Math.ceil(Math.random()*61);
            res += chars[index];
        }
        return res;
    }
};

//部门的相关操作
var depart={
    //判断部门名称是否重复
    isRepeat:function () {
        var dom=arguments[0];
        var update=arguments[1];
        var dname=$(dom).val();
        var $departs=$("ul.ul_list li.group");
        var flag=true;
        for(var i=0,length=$departs.length;i<length;i++){
            if(update&&$($departs[i]).is(".current")){
                continue;
            }
            if(dname==$($departs[i]).find("span.lspan").text()){
                flag=false;
                $(dom).parent().next().text("该部门已存在！").show();
                break;
            }
        }
        return flag;
    },
    //显示添加部门
    showAddDepart:function (fn) {
        common.showDialog({
            title:"新建部门",
            content:"<div>部门名称：<input type=\"text\" id=\"dname\" class=\"text\" /></div><div class=\"error\" style=\"display:none;margin-left: 70px;\" ></div>",
            ok:function () {
                var flag=true;
                var dname=$("#dname").val();
                if(dname==""){
                    $("#dname").parent().next().text("部门名称不能为空！").show();
                    flag=false;
                }else{//判断部门是否重复添加
                    flag=depart.isRepeat($("#dname"));
                }
                if(flag){
                    console.log(dname);
                    var data={"d.name":dname};
                    depart.addDepart(data,fn);
                }
                return flag;
            }
        });
    },
    //添加部门
    addDepart:function (obj,fn) {
        $.post(common.getContextPath()+"/department/add",obj,function (data) {
            if(data.result){//添加成功
                fn&&fn(data);
            }else{
                if(data.result===false){//
                    common.showDialog({
                        content:data.msg||"操作失败"
                    })
                }else{//登陆过期
                    location.href=employee.getContextPath+"/login";
                }

            }
        });
    },
    //显示修改部门
    showUpdateDepart:function (name) {
        common.showDialog({
            title:"修改部门",
            content:"<div>部门名称：<input type=\"text\" id=\"dname\" value='"+name+"' class=\"text\" /></div><div class=\"error\" style=\"display:none;margin-left: 70px;\" ></div>",
            ok:function () {
                var flag=true;
                var dname=$("#dname").val();
                if(dname==""){
                    $("#dname").parent().next().text("部门名称不能为空！").show();
                    flag=false;
                }else{//判断部门是否重复添加
                    flag=depart.isRepeat($("#dname"),true);
                }
                if(flag){
                    var did=$("ul.ul_list li.current").children("input[type=hidden]").val();
                    var obj={"d.name":dname,"d.id":did};
                    depart.updateDepart(obj,function (data) {
                        $("ul.ul_list li.current").children("span.lspan").text(dname);
                    });
                }
                return flag;
            }
        });
    },
    updateDepart:function (obj,fn) {
        $.post(common.getContextPath()+"/department/update",obj,function (data) {
            if(data.result){//添加成功
                fn&&fn();
            }else{
                if(data.result===false){
                    common.showDialog({
                        content:data.msg||"操作失败"
                    });
                }else{//登陆过期
                    location.href=common.getContextPath()+"/login";
                }
            }
        });
    },
    //删除部门及相关信息
    deleteDepart:function (did,url) {
        common.showDialog({
            content:"<div>是否删除部门？</div><div style='color:red;'>删除该部门会将该部门下的所有信息一起删除</div>",
            ok:function () {
                $.ajax({
                    type:"get",
                    url:common.getContextPath()+"/department/delete/"+did,
                    dataType:"json",
                    success:function (data) {
                        location.href=common.getContextPath()+url;
                    }
                });
            }
        });
    }
};


