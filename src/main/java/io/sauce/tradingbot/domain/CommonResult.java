package io.sauce.tradingbot.domain;

import io.sauce.tradingbot.domain.enums.ResultType;
import lombok.Data;

@Data
public class CommonResult<T> {

    private Integer code;

    private String msg;

    private T data;

    public CommonResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CommonResult(ResultType resultType) {
        this.code = resultType.getCode();
        this.msg = resultType.getMessage();
    }

    public CommonResult() {
        this.code = 0;
        this.msg = "";
    }

    public CommonResult(T data) {
        this.code = 0;
        this.msg = "";
        this.data = data;
    }

}
