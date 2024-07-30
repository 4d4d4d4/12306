package com.train.common.aspect.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 全局注解</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/26 下午3:29</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
// 全局注解 用于标识方法是否需要某些验证行为
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Mapping
@Documented
public @interface GlobalAnnotation {
    /**
     * 登录校验
     */
    boolean checkLogin() default false;

    /**
     * 权限校验
     */
    boolean checkAdmin() default false;
}
