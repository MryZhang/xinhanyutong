var recordPage={
    //获取翻页条件
    getPageCondition:function () {
        var obj={};
        var pageIndex=$("#currentPage").val();
        if(pageIndex){
            obj.pageIndex=pageIndex;
        }
        var search=$("#searchStr").val();
        if(search){
            obj.search=search;
        }
        var beginTime=$("#beginTime").val();
        if(beginTime){
            obj.beginTime=beginTime;
        }
        var endTime=$("#endTime").val();
        if(endTime){
            obj.endTime=endTime;
        }
        return obj;
    },
    getQueryString:function () {
        return common.getQueryString(recordPage.getPageCondition());
    },
    getPage:function (obj) {
        location.href=common.getContextPath()+"/record"+common.getQueryString(obj);
    }
};