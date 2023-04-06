package org.epha.mall.cart.exception;

import lombok.extern.slf4j.Slf4j;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.common.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
/**
 * @author pangjiping
 */
@Slf4j
@RestControllerAdvice(basePackages = "org.epha.mall.cart.controller")
public class CartExceptionControllerAdvice {
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

        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage())
                .put("data", errorMap);
    }

    /**
     * 处理自定义的异常类BizException
     */
    @ExceptionHandler(value = BizException.class)
    public R handleBizException(BizException e) {
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理其他通用的错误，只有具体匹配全都失败时才会用这个
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleThrowable(Throwable throwable) {
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage())
                .setDate(throwable.getMessage());
    }
}
