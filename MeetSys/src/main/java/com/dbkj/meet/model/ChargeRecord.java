package com.dbkj.meet.model;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.base.BaseChargeRecord;
import com.dbkj.meet.utils.SqlUtil;
import com.jfinal.plugin.activerecord.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ChargeRecord extends BaseChargeRecord<ChargeRecord> {
	public static final ChargeRecord dao = new ChargeRecord();

	public static final String BEGIN_TIME="beginTime";
	public static final String END_TIME="endTime";

	/**
	 * 根据条件获取充值记录信息的分页数据
	 * @param paraMap
	 * @return
	 */
	public Page<com.jfinal.plugin.activerecord.Record> getChargeRecordInfoPage(Map<String,Object> paraMap){
		int currentPage=1;
		int pageSize= Constant.DEFAULT_PAGE_SIZE;

		StringBuilder where=new StringBuilder(200);
		List<Object> params=new ArrayList<Object>();

		String createTime="a.gmt_create";

		if(paraMap.containsKey(BEGIN_TIME)&&!paraMap.containsKey(END_TIME)){
			where.append(" AND "+createTime+">=?");
			params.add(paraMap.get(BEGIN_TIME));
		}else if(!paraMap.containsKey(BEGIN_TIME)&&paraMap.containsKey(END_TIME)){
			where.append(" AND "+createTime+"<=?");
			params.add(paraMap.get(END_TIME));
		}else if(paraMap.containsKey(BEGIN_TIME)&&paraMap.containsKey(END_TIME)){
			where.append(" AND "+createTime+" BETWEEN ? AND ?");
			params.add(paraMap.get(BEGIN_TIME));
			params.add(paraMap.get(END_TIME));
		}

		for(Map.Entry<String,Object> entry:paraMap.entrySet()){
			String key=entry.getKey();
			Object value=entry.getValue();
			if(value!=null){
				if(Constant.CURRENT_PAGE_KEY.equals(key)||Constant.PAGE_SIZE_KEY.equals(key)
						||ChargeRecord.BEGIN_TIME.equals(key)||ChargeRecord.END_TIME.equals(key)){
					continue;
				}
				where.append(" AND ");
				where.append(key);
				if("b.name".equals(key)){
					where.append(" LIKE ?");
					params.add("%"+value+"%");
					continue;
				}

				where.append("=?");
				params.add(value);
			}
		}

		Object obj1=paraMap.get(Constant.CURRENT_PAGE_KEY);
		if(obj1!=null){
			currentPage=Integer.parseInt(obj1.toString());
		}

		Object obj2=paraMap.get(Constant.PAGE_SIZE_KEY);
		if(obj2!=null){
			pageSize=Integer.parseInt(obj2.toString());
		}

		where.append(" ORDER BY a.gmt_create DESC");

		return Db.paginate(currentPage,pageSize, SqlUtil.getSql("getChargeRecordInfoPage.select",this),
				SqlUtil.getSql("getChargeRecordInfoPage.sqlExceptSelect",this).concat(where.toString()),
				params.toArray(new Object[params.size()]));
	}
}
