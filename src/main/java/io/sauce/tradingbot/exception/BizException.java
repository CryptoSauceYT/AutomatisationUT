package io.sauce.tradingbot.exception;

import io.sauce.tradingbot.domain.enums.ResultType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ResultType resultType;

    public BizException(ResultType resultType) {
        super(resultType.getMessage());
        this.resultType = resultType;
    }

}
