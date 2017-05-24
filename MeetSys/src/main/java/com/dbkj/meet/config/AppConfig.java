package com.dbkj.meet.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.interceptors.AmountInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.interceptors.LoginInterceptor;
import com.dbkj.meet.model._MappingKit;
import com.dbkj.meet.services.ChargingServiceImpl;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.i18n.I18nInterceptor;
import com.jfinal.json.JacksonFactory;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by MrQin on 2016/11/4.
 */
public class AppConfig extends JFinalConfig {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    public void configConstant(Constants constants) {
        PropKit.use("config.properties");
        constants.setDevMode(PropKit.getBoolean(Constant.DEV_MODE,false));
        constants.setI18nDefaultBaseName("il8n");
//        constants.setJsonFactory(new JacksonFactory());
        constants.setError404View("/pages/other/404.html");
        constants.setError500View("/pages/other/500.html");
    }

    public void configRoute(Routes routes) {
        routes.add(new AppRoutes());
    }

    public void configPlugin(Plugins plugins) {
        logger.info("配置Druid数据库连接池");

        String username=PropKit.get(Constant.USERNAME);
        String password=PropKit.get(Constant.PASSWORD);
        String url=PropKit.get(Constant.JDBC_URL);
        String driverClass=PropKit.get(Constant.DRIVER_CLASS);
        logger.info("username:{};password:{};url:{}",username,password,url);

        DruidPlugin druidPlugin=new DruidPlugin(url,username, password,driverClass);
        druidPlugin.set(PropKit.getInt(Constant.INITIAL_SIZE),PropKit.getInt(Constant.MIN_IDLE),PropKit.getInt(Constant.MAX_ACTIVE));
        logger.info("configPlugin 配置Druid数据库连接池过滤器配制");

        druidPlugin.addFilter(new StatFilter());
        WallFilter wallFilter=new WallFilter();
        wallFilter.setDbType(PropKit.get(Constant.DB_TYPE));
        WallConfig wallConfig=new WallConfig();
        wallConfig.setFunctionCheck(false);//支持数据库函数
        wallFilter.setConfig(wallConfig);
        druidPlugin.addFilter(wallFilter);

        ActiveRecordPlugin arp=new ActiveRecordPlugin(druidPlugin);
        arp.setDevMode(PropKit.getBoolean(Constant.DEV_MODE,false));
        arp.setShowSql(PropKit.getBoolean(Constant.DEV_MODE,false));
        arp.setDialect(new MysqlDialect());

        plugins.add(druidPlugin);
        plugins.add(arp);

        _MappingKit.mapping(arp);

        plugins.add(new EhCachePlugin());//ehcache缓存插件

        //Redis配置
        RedisPlugin redis=new RedisPlugin("meet",
                PropKit.get("redisHost","127.0.0.1"),
                PropKit.getInt("redisPort",6379));
        plugins.add(redis);

    }

    public void configInterceptor(Interceptors interceptors) {
        interceptors.add(new LoginInterceptor());
        //interceptors.add(new I18nInterceptor());//国际化
        interceptors.add(new InfoInterceptor());
        interceptors.add(new AmountInterceptor());
    }

    public void configHandler(Handlers handlers) {
        handlers.add(new ContextPathHandler("ctx"));
    }

    @Override
    public void afterJFinalStart() {
        //完成quartz的初始化，方便在服务器重启后定时任务能继续执行
        try {
            Scheduler scheduler= StdSchedulerFactory.getDefaultScheduler();
            if(!scheduler.isStarted()){
                scheduler.start();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(),e);
        }
        //扣除包月费用的定时任务
        ScheduledExecutorService executorService= Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new ChargingServiceImpl().chargeMonthly();
            }
        },0,24, TimeUnit.HOURS);
    }

    public static void main(String[] args){
        JFinal.start("/src/main/webapp",8080,"/",5);
    }
}
