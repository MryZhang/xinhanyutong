package com.dbkj.meet.dic;

/**
 * 用户类型
 * Created by MrQin on 2016/11/8.
 */
public enum UserType {
    SUPER_ADMIN(1,"超级管理员"),
    ADMIN(2,"管理员"),
    NORMAL_USER(3,"普通用户"),
    SUPER_SUPER_ADMIN(4,"超超级管理员")
    ;

    UserType(int typeCode, String typeText) {
        this.typeCode = typeCode;
        this.typeText = typeText;
    }

    private int typeCode;
    private String typeText;

    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public static UserType valueOf(int typeCode){
        for (UserType userType:values()) {
            if(typeCode==userType.getTypeCode()){
                return userType;
            }
        }
        return null;
    }
}
