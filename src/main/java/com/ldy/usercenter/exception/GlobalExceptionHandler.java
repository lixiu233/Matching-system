package com.ldy.usercenter.exception;

import com.ldy.usercenter.common.BaseResponse;
import com.ldy.usercenter.common.ErrorCode;
import com.ldy.usercenter.common.ResulUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("BusinessException" + e.getMessage(), e);
        return ResulUtils.error(e.getCode(), e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException", e);
        return ResulUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(),"");
    }
}
