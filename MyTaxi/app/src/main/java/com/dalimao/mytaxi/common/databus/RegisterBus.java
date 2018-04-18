package com.dalimao.mytaxi.common.databus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解 , 用于标记观察者的方法
 * Created by lsh on 2018/4/9.
 */

//该注解作用与方法
@Target(ElementType.METHOD)
//该注解运行时可用
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterBus {

}
