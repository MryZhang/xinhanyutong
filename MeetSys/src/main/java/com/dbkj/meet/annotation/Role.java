package com.dbkj.meet.annotation;

import com.dbkj.meet.dic.UserType;

import java.lang.annotation.*;

/**
 * Created by DELL on 2017/04/01.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Role {

    UserType[] value() default {};
}
