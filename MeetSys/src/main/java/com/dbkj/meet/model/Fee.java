package com.dbkj.meet.model;

import com.dbkj.meet.dic.CallTypeEnum;
import com.dbkj.meet.model.base.BaseFee;
import com.dbkj.meet.utils.SqlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Fee extends BaseFee<Fee> {
	public static final Fee dao = new Fee();
	public final String TYPE_KEY="type";
	public final String MODE_KEY="mode";

	public List<Fee> getAll(){
		return find(SqlUtil.getSql("getAll",this));
	}

	/**
	 * 获取呼出资费
	 * @return
	 */
	public Fee getCallOutFee(){
		return findFirst(SqlUtil.getSql("getCallOutFee",this), CallTypeEnum.CALL_TYPE_CALLOUT.getCode());
	}

	public List<Fee> getFee(Map<String,Object> paraMap){
		List<Object> params=new ArrayList<>();
		StringBuilder where=new StringBuilder(50);
		for(Map.Entry<String,Object> entry:paraMap.entrySet()){
			if(where.length()==0){
				where.append(" WHERE ");
			}else {
				where.append(" AND ");
			}
			where.append(entry.getKey());
			where.append("=?");
			params.add(entry.getValue());
		}
		return find(SqlUtil.getSql("getAll",this).concat(where.toString()),params.toArray(new Object[params.size()]));
	}
}
