package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component   // 加入容器管理
@Aspect
@Slf4j
public class AutoFillAspect {
    // 切入点表达式：扫描mapper包下所有类与方法和带有AutoFill注解的方法找到切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    // 前置通知，并加载切入点表达式
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段开始填充：{}", joinPoint);

        // 对指定的增强方法获取参数对象，填充公共字段
        // 1. 通过JoinPoint获取连接点方法签名对象（原始方法的签名对象）
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        // 2. 通过方法签名对象获取方法的@AutoFill注解对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);

        // 3. 获取注解对象的value
        OperationType operationType = autoFill.value();

        // 4. JoinPoint获取原始方法的参数数组
        Object[] args = joinPoint.getArgs();

        // 5. 判断参数数组是否有效，无效结束
        if(args == null || args.length == 0) {
            return;
        }

        // 6. 获取参数数组的第一个元素对象（因为增改mapper参数只有一个, 要填充的对象）
        Object obj = args[0];

        // 7. 准备当前时间和当前用户id
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            Method updateUserMethod = obj.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method updateTimeMethod = obj.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            updateUserMethod.invoke(obj, currentId);
            updateTimeMethod.invoke(obj, now);
            // 8. 赋值填充字段
            if(operationType == OperationType.INSERT){
                // obj.getClass().getMethod("方法名字符串", 参数类型字节码)
                // getMethod获取公共方法，getDeclareMethod获取私有方法和公共方法
                Method createUserMethod = obj.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method createTimeMethod = obj.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                // 执行方法反射语法：method.invoke(方法所属的对象, 数据值)
                createUserMethod.invoke(obj, currentId);
                createTimeMethod.invoke(obj, now);
            }
        } catch (Exception e) {
            log.error("公共字段填充失败：{}", e.getMessage());
        }
    }
}
