/**
 * Created by DELL on 2017/03/02.
 */
var bill={
    //初始化Grid
    initGrid:function () {
        var colM = [
            {title: "", minWidth:27,width:27,type: "detail", resizeable: false, editable: false},
            {title: "开始时间",width:150,dataIndx:"startTime",editable: false},
            {title: "会议主题",width:150,dataIndx:"subject",editable: false},
            {title: "消费总额（元）",width:100,dataIndx:"fee",editable: false},
            {title:"通话时长（分）",width:90,dataIndx:"call_time",editable:false},
            {title:"短信费（元）",width:50,dataIndx:"msg_fee",editable:false},
            {title:"短信（条）",width:50,dataIndx:"msg_count",editable:false},
            {title: "会议安排者",width:100,dataIndx:"name",editable: false},
            {title: "人数",width:50,dataIndx:"count",editable: false}
        ];
        var dataModel={
            location:"remote",
            dataType:"JSON",
            method:"GET",
            url:common.getContextPath()+"/account/billList",
            postData:bill.getQueryData(),
            getData:function (dataJSON) {
                return {curPage:dataJSON.curPage,totalRecords:dataJSON.totalRecords,data:dataJSON.data};
            }
        };
        var $grid=$("#bill_grid").pqGrid({
            collapsible: { collapsed : false },
            dataModel:dataModel,
            width:790,
            height:460,
            numberCell:{show:false},
            colModel:colM,
            pageModel:{type:"remote",rPP:15, rPPOptions: [],strRpp:""},
            sortable: false,
            wrap: false, hwrap: false,
            showTitle:false,
            editable: false,
            detailModel: {
                cache: true,
                collapseIcon: "ui-icon-plus",
                expandIcon: "ui-icon-minus",
                init: function (ui) {

                    var rowData = ui.rowData;

                    var $grid,detailobj;
                    try{
                        detailobj = gridDetailModel( $(this), rowData ); //get a copy of gridDetailModel
                        $grid = $("<div></div>").pqGrid( detailobj ); //init the detail grid.
                    }catch (e){
                        common.showDialog({content:"操作失败！"});
                    }

                    $grid.pqGrid("option", $.paramquery.pqGrid.regional["zh"]);
                    $grid.find(".pq-pager").pqPager("option", $.paramquery.pqPager.regional["zh"]);
                    return $grid;
                }
            }
        });
        $grid.pqGrid("option", $.paramquery.pqGrid.regional["zh"]);
        $grid.find(".pq-pager").pqPager("option", $.paramquery.pqPager.regional["zh"]);

        var gridDetailModel=function ($gridMain, rowData) {
            return {
                dataModel: {
                    location: "remote",
                    sorting: "local",
                    dataType: "text",
                    url: common.getContextPath()+"/account/billDetail/" + rowData.id,
                    getData:function( data, textStatus, jqXHR ) {
                        var dataJSON;
                        try{
                            dataJSON=eval('(' + data + ')');
                        }catch (e){
                            common.showDialog({content:"操作失败！"});
                            return {curPage: 1, totalRecords: 0, data: []}
                        }
                        return {curPage: dataJSON.curPage, totalRecords: dataJSON.totalRecords, data: dataJSON.data};
                    }
                },
                colModel:[
                    {title:"通讯类型",dataIndx:"callType",width:60},
                    {title:"姓名",dataIndx:"name",width:85},
                    {title:"参会号码",dataIndx:"callee",width:85},
                    {title:"消费",dataIndx:"fee",width:60},
                    {title:"时长（分钟）",dataIndx:"callTime",width:60},
                    {title:"费率",dataIndx:"rate",width:85},
                    {title:"呼叫时间",dataIndx:"startTime",width:85},
                    {title:"应答时间",dataIndx:"answerTime",width:85},
                    {title:"结束时间",dataIndx:"endTime",width:85}
                ],
                pageModel:{type:"remote",rPP:5, rPPOptions: [],strRpp:""},
                wrap: false, hwrap: false,
                showTitle:false,
                editable: false,
                height:200
            }
        }
    },
    //获取查询条件的对象
    getQueryData:function () {
        var beginTime=$("#searchBeginId").val().trim();
        var endTime=$("#searchEndId").val().trim();
        var search=$("#searchTxtId").val().trim();
        var obj={};
        if(beginTime){
            obj.beginTime=beginTime;
        }
        if(endTime){
            obj.endTime=endTime;
        }
        if(search){
            obj.search=search;
        }
        return obj;
    },
    //获取查询条件字符串
    getQueryStr:function () {
        var beginTime=$("#searchBeginId").val().trim();
        var endTime=$("#searchEndId").val().trim();
        var search=$("#searchTxtId").val().trim();
        var queryStr="";
        if(beginTime){
            if(queryStr){
                queryStr+="&";
            }else{
                queryStr+="?";
            }
            queryStr+="beginTime="+beginTime;
        }
        if(endTime){
            if(queryStr){
                queryStr+="&";
            }else{
                queryStr+="?";
            }
            queryStr+="endTime="+endTime;
        }
        if(search){
            if(queryStr){
                queryStr+="&";
            }else{
                queryStr+="?";
            }
            queryStr+="search="+search;
        }
        return queryStr;
    }
};