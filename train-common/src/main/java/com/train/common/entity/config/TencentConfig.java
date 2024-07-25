package com.train.common.entity.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 腾讯云配置类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/23 下午3:39</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
@Component
@Getter
@ToString
public class TencentConfig {
    private String secretId;
    private String secretKey;
    private String smsSdkAppId;
    private String templateId;
    private String signName;

    // 初始化配置文件 处于安全考虑在txt中加载

    /** 文件格式如下
     * SecretId:xxxxx
     * SecretKey:xxxx
     * SmsSdkAppId:xxx
     * TemplateId:xxxx
     * SignName:xxxx
     */
    @PostConstruct
    public void initConfig(){
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("config/tencentcloud.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try {
            String strLine = null;
            while((strLine = bufferedReader.readLine()) != null) {
                Field[] declaredFields = TencentConfig.class.getDeclaredFields();
                for (Field field : declaredFields){
                    if(field.get(this) == null){
                        field.setAccessible(true);
                        String name = field.getName();
                        String[] split = strLine.split(":");
                        if(split[0].equalsIgnoreCase(name)){
                            field.set(this, split[1]);
                        }
                    }
                }
            }
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                inputStreamReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
