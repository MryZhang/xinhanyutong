package com.dbkj.meet.dic;

/**
 * Created by MrQin on 2016/11/4.
 */
public interface Constant {

    String KEY_MAP="RSAKeyMap";

    /**
     * 固定会议转接号码
     */
    String TRANSFER_NUM="transferNum";

    String DOWNLOAD_URL_PREFIX="downloadUrl";

    String VERTIFY_CODE="vertifyCode";

    String CHARGING_INTERVAL="charging_interval";

    String DEFAULT_AMOUNT="defaultAmount";

    //ehcache缓存name
    String CACHE_NAME_ADMDIV="admdiv";
    String CACHE_NAME_PERMANENT="permanent";
    String CACHE_NAME_BILLCACHE="billCache";
    /**
     * 公共联系人缓存键
     */
    String PUBLIC_CONTACT_CACHE_KEY="public_contact_cache";
    /**
     * 个人联系人缓存键
     */
    String PRIVATE_CONTACT_CACHE_KEY="private_contact_cache";

    int DEFAULT_PAGE_SIZE=10;//默认的分页页面显示数据条数
    String PAGE_SIZE_KEY="pageSize";
    String CURRENT_PAGE_KEY="currentPage";//当前页码的键
    String IL8N_LOCALE_TPYE="zh_CN";

    String ENCRYPT_KEY="csfvfasd,.;['%^$";
    String USER_KEY="user_key";
    String DEV_MODE="devMode";
    String JDBC_URL="jdbcUrl";
    String USERNAME="username";
    String PASSWORD="password";
    String DRIVER_CLASS="driverClass";
    String DB_TYPE="dbType";
    /**
     * 数据库连接池初始化大小
     */
    String INITIAL_SIZE="initialSize";
    /**
     * 数据库连接池最大空闲
     */
    String MIN_IDLE="minIdle";
    /**
     * 数据库连接池最大连接
     */
    String MAX_ACTIVE="maxActive";

    String WATING_CALL="0";

    String ACTION_CREATE_CALL = "1000";//发起呼叫
    String ACTION_CREATE_MEET = "1001";//创建会议
    String ACTION_JOIN_MEET = "1002";//加入会议
    String ACTION_LEAVE_MEET = "1003";//离开会议
    String ACTION_MEMBER_SILENT = "1004";//禁止发言
    String ACTION_MEMBER_SPEAK = "1005";//允许发言
    String ACTION_BEGIN_PLAY_VOICE = "1006";//开始会场放音
    String ACTION_STOP_PLAY_VOICE = "1007";//停止会场放音
    String ACTION_BEGIN_RECORD = "1008";//开始会场录音
    String ACTION_STOP_RECORD = "1009";//停止会场录音
    String ACTION_CLOSE_MEET = "1010";//结束会议
    String ACTION_GET_MEET_TIME = "1011";//获取会议进行时长
    String ACTION_GET_MEET_ALL_TIME = "1012";//获取会议中所有通道的总时长
    String ACTION_CALL_STATUS_REPORT = "1013";//会议中的状态变更上报
    String ACTION_GET_MEET_CALLSTATUS = "1014";//获取会议中的与会电话状态
    String ACTION_CALLLOG_REPORT = "1015";//通话记录上报
    String ACTION_MEET_REPORT_NUMBER = "1016";//会场报数
    String ACTION_CREATE_APP_CALL = "1017";//发起APP直呼
    String ACTION_VOICE_CALL = "1018";//发起语音通知
    String ACTION_GET_APP_VERSION = "1019";//获取APP服务端当前版本号
    String ACTION_CREATE_TTS = "1020";//创建TTS

    String ACTION_CREATE_CALLMEET="1023";//创建呼入式会议

    String HANGUP_TYPE_200 = "2000";//主叫正常挂机
    String HANGUP_TYPE_201 = "2001";//被叫正常挂机
    String HANGUP_TYPE_403 = "2500";//无权限拨打号码
    String HANGUP_TYPE_404 = "2501";//被叫为空号
    String HANGUP_TYPE_408 = "2502";//被叫无人接听
    String HANGUP_TYPE_486 = "2503";//被叫正在通话中
    String HANGUP_TYPE_488 = "2504";//被叫无法接通

    String YES = "3000";//是
    String NO = "3001";//否

    String SUCCESS = "4000";//成功
    String FAIL = "4001";//失败

    String REC_MODE_RING = "5000";//录音模式－彩铃录音
    String REC_MODE_TALK = "5001";//录音模式－通话录音

    String CALL_STATUS_IDLE = "6000";//呼叫状态－空闲
    String CALL_STATUS_TRING = "6001";//呼叫状态－处理中
    String CALL_STATUS_RING = "6002";//呼叫状态－振铃
    String CALL_STATUS_ANSWER = "6003";//呼叫状态－应答
    String CALL_STATUS_BYE = "6004";//呼叫状态－挂机
    String CALL_STATUS_FAIL = "6005";//呼叫状态－失败
    String CALL_STATUS_REJECT = "6006";//呼叫状态－拒接

    String CALL_STATUS_JOIN_MEET = "6007";//呼叫状态－加入会场
    String CALL_STATUS_LEAVE_MEET = "6008";//呼叫状态－离开会场

    String CALL_TYPE_CALLIN = "7000";//呼叫类型－呼入
    String CALL_TYPE_CALLOUT = "7001";//呼叫类型－呼出

    String CALL_MODE_CALL = "8000";//通话类型－普通双呼电话
    String CALL_MODE_MEET = "8001";//通话类型－会议
    String CALL_MODE_APP_CALL = "8002";//通话类型－APP直呼电话
    String CALL_MODE_VOICE_CALL = "8003";//通话类型－语音通知

    String MEET_LEVEL_AUDIENCE = "9000";//会议级别－听众
    String MEET_LEVEL_CHARIMAN = "9001";//会议级别－主席


    //=====================会议接口常用参数字段==================================
    String REQID="reqId";//32 位随机不重复请求 ID
    String COSTNUM1="costNum1";//被叫 1 计费号码
    String SHOWNUM1="showNum1";//被叫号码 1 显示号码
    String CALLER="caller";//主叫用户的手机号码
    String COSTNUM2="costNum2";//被叫2计费号码
    String SHOWNUM2="showNum2";//被叫 2 显示号码
    String CALLED="called";//被叫号码
    String REC="rec";//是否录音或者录音地址
    String RECMODE="recMode";//录音模式
    String ACTION="action";//操作功能

    String STATUS="status";//操作结果
    String CONTENT="content";//

    String COSTNUM="costNum";//计费号码
    String SHOWNUM="showNum";//会议显示号码
    String MEETNUMS="meetNums";//会议方数

    String CALLERS="callers";//要加入会议的号码
    String MEETID="meetId";//会议Id
    String CALLEE="callee";//被叫号码
    String CALLTYPE="callType";//呼叫类型
    String CALLTIME="callTime";//通话时长
    String CALLBEGINTIME="callBeginTime";//通话开始时间
    String CALLANSWERTIME="callAnswerTime";//被叫接听时间
    String HANGUPTIME="hangupTime";//通话结束时间
    String CALLMODE="callMode";//通话类型
    String ISANSWER="isAnswer";//是否接通

    String CHAIRMANPWD="chairmanPwd";//主席密码
    String AUDIENCEPWD="audiencePwd";//听众密码
    String LOCK="lock";//是 锁定 否 不锁定
    String NAME="name";

    String CALL_TYPE="callType";//呼叫类型
    String CALL_TIME="callTime";//通话时长
    String CALL_BEGIN_TIME="callBeginTime";//通话开始时间
    String CALL_ANSWER_TIME="callAnswerTime";//被叫接听时间
    String HANGUP_TIME="hangupTime";//通话结束时间
    String CALL_MODE="callMode";//通话类型
    String IS_ANSWER="isAnswer";//是否接通

    String LEVEL="level";//参会人类别
}
