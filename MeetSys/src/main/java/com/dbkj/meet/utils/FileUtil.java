package com.dbkj.meet.utils;

import java.io.File;

/**
 *
 * Created by MrQin on 2016/11/22.
 */
public class FileUtil {

    /**
     * 通过文件夹路径删除文件下面的所有内容不包括文件夹
     * @param path
     */
    public static void deleteFiles(String path){
        File file=new File(path);
        deleteFiles(file);
    }

    /**
     * 删除文件下的所有内容，不包括文件夹
     * @param file
     */
    public static void deleteFiles(File file){
        if(file!=null&&file.exists()&&file.isDirectory()){
            File[] files=file.listFiles();
            for(File f:files){
                deleteDirectory(f);
            }
        }
    }

    /**
     * 通过文件路径删除文件及文件夹下所有文件
     * @param path
     */
    public static void deleteDirectory(String path){
        File file=new File(path);
        deleteDirectory(file);
    }


    /**
     * 删除目录及目录下的所有文件
     * @param file
     */
    public static void deleteDirectory(File file){
        if(file!=null&&file.exists()){
            //如果file是文件或者是一个空文件夹，则直接删除
            if(file.isFile()||file.list().length==0){
                file.delete();
            }else{//否则就列出目录下的所有文件和文件夹并递归删除
                File[] files=file.listFiles();
                for(int i=0,len=files.length;i<len;i++){
                    deleteDirectory(files[i]);
                }
            }

        }
    }
}
