package org.epha.mall.elasticsearch.exception;

import lombok.extern.slf4j.Slf4j;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "org.epha.mall.elasticsearch.controller")
public class ElasticsearchExceptionControllerAdvice {
    /**
     * 处理MethodArgumentNotValidException错误，也就是校验错误
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", errorMap);
    }

    /**
     * 处理其他通用的错误，只有具体匹配全都失败时才会用这个
     */
//    @ExceptionHandler(value = Throwable.class)
//    public R handleThrowable(Throwable throwable) {
//        log.error("系统未知异常: {}",throwable.getStackTrace());
//        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
//    }
}
