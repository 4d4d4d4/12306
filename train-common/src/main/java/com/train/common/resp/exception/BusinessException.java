package com.train.common.resp.exception;

import com.train.common.resp.enmus.ResultStatusEnum;
import lombok.Getter;

/**
 * @Classname BusinessException
 * @Description 已知业务非受监异常
 * @Date 2024/7/17 下午5:05
 * @Created by 憧憬
 */
@Getter
public class BusinessException extends RuntimeException {
    private ResultStatusEnum resultStatusEnum; // 异常
    private Integer code;
    private String message;

    public BusinessException() {
    }

    public BusinessException(ResultStatusEnum resultStatusEnum) {
        this.resultStatusEnum = resultStatusEnum;
        this.code = resultStatusEnum.getCode();
        this.message = resultStatusEnum.getDescription();
    }

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultStatusEnum resultStatusEnum, Integer code, String message) {
        this.resultStatusEnum = resultStatusEnum;
            this.code = code == null ? resultStatusEnum.getCode() : code;
        this.message = message == null ? resultStatusEnum.getDescription() : message;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
