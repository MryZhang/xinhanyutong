package com.dbkj.meet.model;

import com.dbkj.meet.model.base.BaseEmployee;
import com.dbkj.meet.utils.SqlUtil;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Employee extends BaseEmployee<Employee> {
	public static final Employee dao = new Employee();

	public int deleteByCompanyId(long id){
		return Db.update(SqlUtil.getSql("deleteByCompanyId",this),id);
	}

	public int deleteByDepartmentId(long id){
		return Db.update(SqlUtil.getSql("deleteByDepartmentId",this),id);
	}

	public int deleteBatchByUserId(int[] ids){
		if(ids!=null&&ids.length>0){
			StringBuilder sb=new StringBuilder(20);
			sb.append(" (");
			for(int i=0;i<ids.length;i++){
				sb.append(ids[i]);
				if(i!=ids.length-1){
					sb.append(",");
				}
			}
			sb.append("))");
			int count = Db.update(SqlUtil.getSql("deleteBatchByUserId",this)+sb.toString());
			return count;
		}
		return 0;
	}

	/**
	 * 根据用户名（手机号码）获取用户的email
	 * @param plist
	 * @return
	 */
	public List<String> getEmailByUsername(List<String> plist){
		List<String> rlist=new ArrayList<>(plist.size());
		if(plist.size()>0){
			String sql=SqlUtil.getSql("getEmailByUsername.select",this);
			StringBuilder where=new StringBuilder();
			String sqlTmp=SqlUtil.getSql("getEmailByUsername.where",this);
			for(String str:plist){
				if(where.length()>0){
					where.append(" union all ");
				}
				where.append(sqlTmp);
			}
			sql = sql.replace("{}",sqlTmp.toString());
			List<com.jfinal.plugin.activerecord.Record> resultList=Db.find(sql,plist.toArray(new Object[plist.size()]));
			if(resultList!=null){
				for(Record record:resultList){
					rlist.add(record.getStr("email"));
				}
			}
		}
		return rlist;
	}
}
