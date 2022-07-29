package com.paramresolve.config;

import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.*;

/**
 * @RequestBody获取得请求体如果是json 可以不需要建立model 或者JSON工具 通过getter setter获取
 * 通过该注解可以直接以类似@RequestParam得形式获取
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBodyExtract {

    /**
     * name的别名
     */
    String value() default "";

    /**
     * 从json中获取得参数
     */
    String name() default "";

    /**
     * 参数是否是必须的
     */
    boolean required() default true;

    /**
     * 参数的默认值，如果json中没有获取到，用该值替换，未设置该值则抛出异常
     */
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
