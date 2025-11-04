package io.sauce.tradingbot.exception;

import io.sauce.tradingbot.domain.CommonResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BusinessExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BizException.class)
    public CommonResult<String> handleBizException(BizException ex) {
        return new CommonResult<>(ex.getResultType());
    }
}