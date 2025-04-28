package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)           // 注解在哪使用：方法
@Retention(RetentionPolicy.RUNTIME)   // 注解保留在字节码class文件中，并且运行时可获取
public @interface AutoFill {

    OperationType value();
}
