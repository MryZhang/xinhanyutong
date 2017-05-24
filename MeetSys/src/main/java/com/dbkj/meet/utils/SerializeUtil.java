package com.dbkj.meet.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by DELL on 2017/03/28.
 */
public class SerializeUtil {

    private static final Logger logger=Logger.getLogger(SerializeUtil.class);

    /**
     * 将对象序列化为字节数组
     * @param o
     * @return
     */
    public static byte[] serialize(Object o){
        byte[] bytes=null;
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        ObjectOutputStream out=null;
        try {
            out=new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            bytes=bos.toByteArray();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }finally {
            closeOutputStream(bos,out);
        }
        return bytes;
    }

    /**
     * 将字节数组反序列化对象
     * @param bytes
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] bytes){
        T t=null;
        ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
        ObjectInputStream input=null;
        try {
            input=new ObjectInputStream(bis);
            t= (T) input.readObject();
        } catch (IOException|ClassNotFoundException e) {
            logger.error(e.getMessage(),e);
        } finally {
           closeInputStream(bis,input);
        }
        return t;
    }

    private static void closeOutputStream(ByteArrayOutputStream bos,ObjectOutputStream output){
        try {
            if(output!=null) {
                output.close();
            }
            if(bos!=null){
                bos.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }

    private static void closeInputStream(ByteArrayInputStream bis,ObjectInputStream input){
        try{
            if(input!=null){
                input.close();
            }
            if(bis!=null){
                bis.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * 序列化集合对象
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Serializable> byte[] serializeList(List<T> list){
        byte[] bytes=null;
        ByteArrayOutputStream bos=null;
        ObjectOutputStream output=null;
        try {
            bos=new ByteArrayOutputStream();
            output=new ObjectOutputStream(bos);
            for(T t:list){
                output.writeObject(t);
            }
            output.writeObject(null);
            bytes=bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            closeOutputStream(bos,output);
        }
        return bytes;
    }


    public static <T extends Serializable> List<T> deserializeList(byte[] bytes){
        ByteArrayInputStream bis=null;
        ObjectInputStream input=null;
        List<T> list=new ArrayList<>();
        try {
            bis=new ByteArrayInputStream(bytes);
            input=new ObjectInputStream(bis);
            Object obj=null;
            while((obj=input.readObject())!=null){
                T t= (T) obj;
                list.add(t);
            }
        } catch (IOException|ClassNotFoundException e) {
            logger.error(e.getMessage(),e);
        }finally {
            closeInputStream(bis,input);
        }
        return list;
    }

    /**
     * 序列化集合对象
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Serializable> byte[] serializeSet(Set<T> list){
        byte[] bytes=null;
        ByteArrayOutputStream bos=null;
        ObjectOutputStream output=null;
        try {
            bos=new ByteArrayOutputStream();
            output=new ObjectOutputStream(bos);
            for(T t:list){
                output.writeObject(t);
            }
            output.writeObject(null);
            bytes=bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            closeOutputStream(bos,output);
        }
        return bytes;
    }



    public static <T extends Serializable> Set<T> deserializeSet(byte[] bytes){
        ByteArrayInputStream bis=null;
        ObjectInputStream input=null;
        Set<T> list=new HashSet<>();
        try {
            bis=new ByteArrayInputStream(bytes);
            input=new ObjectInputStream(bis);
            Object obj=null;
            while((obj=input.readObject())!=null){
                T t= (T) obj;
                list.add(t);
            }
        } catch (IOException|ClassNotFoundException e) {
            logger.error(e.getMessage(),e);
        }finally {
            closeInputStream(bis,input);
        }
        return list;
    }
}
