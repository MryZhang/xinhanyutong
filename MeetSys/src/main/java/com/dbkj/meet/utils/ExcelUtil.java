package com.dbkj.meet.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelUtil {

	public static String writeExcel(List<Record> list,String[] heads,String[] columns){
		return writeExcel(list,null,heads,columns);
	}
	
	/**
	 * 输出Excel
	 * @param list 要输出的数据
	 * @param directory 要输出的目录
	 * @param heads 表头
	 * @param columns 数据列名
	 */
	public static <T> String writeExcel(List<T> list,String directory,String[] heads,String[] columns){
		WritableWorkbook workbook=null;
		String path = PathKit.getWebRootPath()+"/download/"+(directory!=null?directory:"");
		File dir=new File(path);
		if(!dir.exists()){
			dir.mkdir();
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		String name=sdf.format(new Date())+".xls";
		try {
			workbook=Workbook.createWorkbook(new File(dir, name));
			//创建Excel工作表，制定名称和位置
			WritableSheet sheet=workbook.createSheet("sheet1", 0);
			//添加表头
			createHead(sheet, heads);
			//添加内容
			WritableFont font=new WritableFont(WritableFont.ARIAL,12);
			WritableCellFormat format=new WritableCellFormat(font);
			try {
				format.setAlignment(Alignment.CENTRE);//居中显示
			} catch (WriteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Iterator<T> itr=list.iterator();
			int rows=1;
			while(itr.hasNext()){
				T t=itr.next();
				if(t instanceof Record){
					Record r= (Record) t;
					for(int i=0;i<columns.length;i++){
						Object o = r.get(columns[i]);
						Label lbl=new Label(i,rows,o==null?"":o.toString(),format);
						sheet.addCell(lbl);
					}
				}else{
					Model r= (Model) t;
					for(int i=0;i<columns.length;i++){
						Object o = r.get(columns[i]);
						Label lbl=new Label(i,rows,o==null?"":o.toString(),format);
						sheet.addCell(lbl);
					}
				}

				rows++;
			}
			workbook.write();//写入
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(workbook!=null){
				try {
					workbook.close();
				} catch (WriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return name;
	}
	
	/**
	 * 创建表头
	 * @param sheet
	 * @param heads
	 */
	private static void createHead(WritableSheet sheet,String[] heads){
		WritableFont font=new WritableFont(WritableFont.ARIAL,14,WritableFont.BOLD);
		WritableCellFormat format=new WritableCellFormat(font);
		try {
			format.setAlignment(Alignment.CENTRE);//居中显示
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(int i=0;i<heads.length;i++){
			try {
				Label lbl=new Label(i,0,heads[i],format);
				sheet.addCell(lbl);
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 读取excel
	 * @param file excel文件
	 * @param list 读取出来的数据
	 * @param columns 数据字段名
	 */
	public void readExcel(File file,List<Record> list,String[] columns){
		InputStream is=null;
		Workbook workbook=null;
		try {
			is=new FileInputStream(file);
			workbook=Workbook.getWorkbook(is);
			Sheet sheet=workbook.getSheet(0);
			int cols=columns.length;
			int rows=sheet.getRows();
			for(int i=1;i<rows;i++){//行
				Record record=new Record();
				for(int n=0;n<cols;n++){//列
					Cell cell=sheet.getCell(n, i);
					String content=cell.getContents();
					content=content!=null?content.trim():"";
					record.set(columns[n], content);
				}
				list.add(record);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(workbook!=null){
				workbook.close();
			}
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * 验证电话号码
	 * @param phone
	 * @return
	 */
	public boolean validatePhone(String phone){
		Pattern pattern=Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher matcher=pattern.matcher(phone);
		return matcher.matches();
	}
	
	
	/**
	 * 验证email
	 * @param email
	 * @return
	 */
	public boolean validateEmail(String email){
		Pattern pattern=Pattern.compile("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$");
		Matcher matcher=pattern.matcher(email);
		return matcher.matches();
	}
}
