package com.train.common.resp.exception;

import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import io.lettuce.core.RedisException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.net.BindException;
import java.util.List;

/**
 * @Classname GlobalExceptionController
 * @Description 全局异常错误类
 * @Date 2024/7/17 下午5:09
 * @Created by 憧憬
 */
@RestControllerAdvice
public class GlobalExceptionController {
    private Logger logger = LoggerFactory.getLogger(GlobalExceptionController.class);

    // 无请求处理器异常
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Result> Exception(Exception e, HttpServletRequest request, HttpServletResponse response) {
        Result error = Result.error();
        e.printStackTrace();
        if (e instanceof NoHandlerFoundException) {
            // 路径参数错误
            error.setCode(ResultStatusEnum.CODE_404.getCode());
            error.setMessage(ResultStatusEnum.CODE_404.getDescription());
        } else if (e instanceof RedisException) {
            error.setMessage("服务器组件异常");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } else if (e instanceof BusinessException biz) {
            // 业务异常
            Integer code = biz.getCode() == null ? ResultStatusEnum.CODE_600.getCode() : biz.getCode();
            String message = biz.getMessage() == null ? ResultStatusEnum.CODE_600.getDescription() : biz.getMessage();
            error.setCode(code);
            error.setMessage(message);
        } else if (e instanceof MethodArgumentNotValidException mav) {
            // 参数格式异常
            BindingResult bindingResult = mav.getBindingResult();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            String message = "";
            for (FieldError fieldError : fieldErrors) {
                message = fieldError.getDefaultMessage() + ";\n";
            }
            message = message.isEmpty() ? ResultStatusEnum.CODE_602.getDescription() : message;
            error.setCode(ResultStatusEnum.CODE_602.getCode());
            error.setMessage(message);
        } else if (e instanceof BindException) {
            // 参数格式异常
            error.setMessage(ResultStatusEnum.CODE_602.getDescription());
            error.setCode(ResultStatusEnum.CODE_602.getCode());
        } else if (e instanceof IllegalStateException) {
            // 参数错误
            error.setCode(ResultStatusEnum.CODE_600.getCode());
            error.setMessage(ResultStatusEnum.CODE_600.getDescription());
        } else if (e instanceof DuplicateKeyException) {
            // 主键冲突异常
            error.setCode(ResultStatusEnum.CODE_601.getCode());
            error.setMessage(ResultStatusEnum.CODE_600.getDescription());
        } else {
            // 未知异常
            error.setCode(ResultStatusEnum.CODE_500.getCode());
            error.setMessage(ResultStatusEnum.CODE_500.getDescription());
        }

        if (response.getContentType() != null && response.getContentType().equals("image/jpeg")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        logger.error("请求异常,请求地址：{},错误码:{},错误信息:{}", request.getRequestURL(), error.getCode(), error.getMessage());

        return new ResponseEntity<>(error, HttpStatus.OK);
    }

}
