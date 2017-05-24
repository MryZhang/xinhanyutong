package com.dbkj.meet.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MrQin on 2016/11/7.
 */
public class ByteGroup {

    private List<Byte> byteContainer=new ArrayList<Byte>();

    public byte[] toBytes(){
        byte[] bytes=new byte[byteContainer.size()];
        for(int i=0,length=byteContainer.size();i<length;i++){
            bytes[i]=byteContainer.get(i);
        }
        return bytes;
    }

    public ByteGroup addBytes(byte[] bytes){
        for (byte b: bytes) {
            byteContainer.add(b);
        }
        return this;
    }

    public int size(){
        return byteContainer.size();
    }
}
