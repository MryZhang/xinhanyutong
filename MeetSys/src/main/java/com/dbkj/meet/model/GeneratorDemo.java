package com.dbkj.meet.model;

import javax.sql.DataSource;

import com.dbkj.meet.dic.Constant;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * GeneratorDemo
 */
public class GeneratorDemo {
	
	public static DataSource getDataSource() {
		Prop p = PropKit.use("config.properties");
		DruidPlugin druidPlugin=new DruidPlugin(PropKit.get(Constant.JDBC_URL),PropKit.get(Constant.USERNAME),
				PropKit.get(Constant.PASSWORD),PropKit.get(Constant.DRIVER_CLASS));
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}
	
	public static void main(String[] args) {
		// base model 所使用的包名
		String baseModelPackageName = "com.dbkj.meet.model.base";
		// base model 文件保存路径
		String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/dbkj/meet/model/base";
		
		// model 所使用的包名 (MappingKit 默认使用的包名)
		String modelPackageName = "com.dbkj.meet.model";
		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";
		
		// 创建生成器
		Generator gernerator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
		// 设置数据库方言
		gernerator.setDialect(new MysqlDialect());
		// 添加不需要生成的表名
		gernerator.addExcludedTable("qrtz_blob_triggers","qrtz_calendars","qrtz_cron_triggers","qrtz_fired_triggers","qrtz_job_details",
				"qrtz_locks","qrtz_paused_trigger_grps","qrtz_scheduler_state","qrtz_simple_triggers","qrtz_simprop_triggers","qrtz_triggers","view_meet_bill");
		// 设置是否在 Model 中生成 dao 对象
		gernerator.setGenerateDaoInModel(true);
		// 设置是否生成字典文件
		gernerator.setGenerateDataDictionary(false);
		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
		gernerator.setRemovedTableNamePrefixes("t_");
		// 生成
		gernerator.generate();
	}
}




