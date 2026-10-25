package com.xinghai.exception;

import com.xinghai.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常：Service 层主动抛出，由全局异常处理器统一捕获并转成友好提示
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
