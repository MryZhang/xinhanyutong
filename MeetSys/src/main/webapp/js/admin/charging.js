/**
 * Created by DELL on 2017/03/14.
 */
var charging={
    getPageUrl:function () {
        return common.getContextPath()+"/admin/charging";
    },
    page:function (obj) {
        location.href=this.getPageUrl()+charging.getQueryString(obj);
    },
    getQueryString:function (obj) {
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
        return where;
    }
};