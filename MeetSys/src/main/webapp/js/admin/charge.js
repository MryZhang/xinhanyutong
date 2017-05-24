/**
 * Created by DELL on 2017/02/25.
 */
var charge={
    getPageUrl:function () {
        return common.getContextPath()+"/admin/charge";
    },
    page:function (obj) {
        var where="?currentPage="+obj["pageNum"];
        if(obj["companyName"]){
            where+="&companyName="+obj["companyName"];
        }
        location.href=this.getPageUrl()+where;
    },
    getQueryString:function () {
        var currentPage=$("#currentIndex").val();
        var companyName=$("#companyName").val();
        var where="";
        if(currentPage){
            where+="?currentPage="+currentPage;
        }
        if(companyName){
            if(where){
                where+="&";
            }else {
                where+="?";
            }
            where+="companyName="+companyName;
        }
        return where;
    },
    validate:function () {
        var $chargeAmount=$("input[name='chargeVo.chargeAmount']");
        var $actualAmount=$("input[name='chargeVo.actualCharge']");
        var chargeAmount=$chargeAmount.val().trim();
        var actualCharge=$actualAmount.val().trim();
        // alert(chargeAmount+"\t"+actualCharge);
        // alert("chargeAmount:"+common.rationalRegex.test(chargeAmount)+"\tactualCharge:"+common.rationalRegex.test(actualCharge));
        var flag=true;
        if(!common.rationalRegex.test(chargeAmount)){
            $chargeAmount.attr("class","text highlight2").next().text("充值金额格式不正确").attr("class","addConTip error");
            flag=false;
        }
        if(!common.rationalRegex.test(actualCharge)){
            $actualAmount.attr("class","text highlight2").next().text("到账金额格式不正确").attr("class","addConTip error");
            flag=false;
        }
        return flag;
    }
}

var record={
    getPageUrl:function () {
        return common.getContextPath()+"/admin/charge/record";
    },
    page:function (obj) {
        var where="?pageIndex="+obj["pageNum"];
        if(obj["beginTime"]){
            where+="&beginTime="+obj["beginTime"];
        }
        if(obj["endTime"]){
            where+"&endTime="+obj["endTime"];
        }
        if(obj["search"]){
            where+="&search="+obj["search"];
        }
        location.href=this.getPageUrl()+where;
    }
}